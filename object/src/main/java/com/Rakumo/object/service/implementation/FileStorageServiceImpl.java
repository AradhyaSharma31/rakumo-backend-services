package com.Rakumo.object.service.implementation;

import com.Rakumo.object.exception.*;
import com.Rakumo.object.model.*;
import com.Rakumo.object.service.FileStorageService;
import com.Rakumo.object.util.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${storage.root:./storage}")
    private String storageRootPath;

    private Path storageRoot;
    private Path tempRoot;
    private final Map<String, List<ChunkMetadata>> uploadMetadataCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        this.storageRoot = Paths.get(storageRootPath);
        this.tempRoot = storageRoot.resolve(".tmp");

        try {
            FileUtils.createDirectory(storageRoot);
            FileUtils.createDirectory(tempRoot);
            log.info("Storage initialized at: {}", storageRoot);
            log.info("Temp storage at: {}", tempRoot);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directories", e);
        }
    }

    @Override
    public void storeCompleteFile(LocalObjectReference ref, InputStream data)
            throws IOException, ChecksumMismatchException {

        Path tempPath = createTempFile("upload-");
        try {
            log.info("Storing new file: bucket={}, key={}", ref.getBucketName(), ref.getObjectKey());

            // 1. Stream data to temp file
            Files.copy(data, tempPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Stored temporary file at: {}", tempPath);

            // 2. Verify checksum
            if (ref.getChecksum() != null) {
                String actualChecksum = ChecksumUtils.sha256(tempPath);
                if (!actualChecksum.equals(ref.getChecksum())) {
                    log.error("Checksum mismatch for {} (expected: {}, actual: {})",
                            ref.getObjectKey(), ref.getChecksum(), actualChecksum);
                    throw new ChecksumMismatchException("Checksum verification failed");
                }
            }

            // 3. Atomic move to final location
            Path finalPath = resolveFinalPath(ref);
            FileUtils.moveAtomic(tempPath, finalPath);
            log.info("Successfully stored file at: {}", finalPath);

            // 4. Store content-type metadata
            storeContentTypeMetadata(ref, finalPath);

        } finally {
            silentDelete(tempPath);
        }
    }

    @Override
    public void storeChunk(FileChunkInfo chunk) throws IOException, ChecksumMismatchException {
        Path chunkPath = resolveChunkPath(chunk);

        try (InputStream chunkData = chunk.getInputStream()) {
            Path tempPath = createTempFile("chunk-");
            try {
                Files.copy(chunkData, tempPath);

                if (chunk.getChecksum() != null && !ChecksumUtils.verify(tempPath, chunk.getChecksum())) {
                    throw new ChecksumMismatchException("Chunk checksum mismatch");
                }

                FileUtils.moveAtomic(tempPath, chunkPath);
            } finally {
                silentDelete(tempPath);
            }
        }

        updateChunkMetadata(chunk, chunkPath);
    }

    @Override
    public void assembleChunks(String uploadId, LocalObjectReference finalRef)
            throws IOException, IncompleteUploadException, ChecksumMismatchException {

        Path finalPath = resolveFinalPath(finalRef);
        Path assemblyTempPath = createTempFile("assembly-");

        try {
            log.info("Assembling chunks for upload {}", uploadId);

            // 1. Load and verify chunks
            List<ChunkMetadata> chunks = loadChunkMetadata(uploadId);
            verifyChunkSequence(chunks);
            validateAllChunks(chunks);

            // 2. Stream concatenation (memory efficient)
            try (OutputStream out = Files.newOutputStream(assemblyTempPath)) {
                for (ChunkMetadata chunk : chunks) {
                    try (InputStream chunkStream = Files.newInputStream(chunk.path())) {
                        chunkStream.transferTo(out);
                    }
                    log.trace("Appended chunk {} ({} bytes)", chunk.index(), Files.size(chunk.path()));
                }
            }

            // 3. Verify final checksum
            if (finalRef.getChecksum() != null) {
                String actualChecksum = ChecksumUtils.sha256(assemblyTempPath);
                if (!actualChecksum.equals(finalRef.getChecksum())) {
                    throw new ChecksumMismatchException("Final checksum mismatch for upload " + uploadId);
                }
            }

            // 4. Atomic move and metadata
            FileUtils.moveAtomic(assemblyTempPath, finalPath);
            storeContentTypeMetadata(finalRef, finalPath);
            log.info("Successfully assembled file at: {}", finalPath);

        } finally {
            silentDelete(assemblyTempPath);
            cleanupUpload(uploadId);
        }
    }

    @Override
    public InputStream retrieveFile(LocalObjectReference ref) throws ObjectNotFoundException, IOException {
        Path filePath = resolveFinalPath(ref);
        if (!Files.exists(filePath)) {
            throw new ObjectNotFoundException("File not found: " + ref.getObjectKey());
        }
        log.debug("Retrieving file: {}", filePath);
        return new BufferedInputStream(Files.newInputStream(filePath));
    }

    @Override
    public void deleteFile(LocalObjectReference ref) throws IOException {
        Path filePath = resolveFinalPath(ref);
        if (Files.exists(filePath)) {
            log.info("Deleting file: {}", filePath);
            Files.delete(filePath);
            cleanupEmptyParents(filePath);
            deleteContentTypeMetadata(ref);
        }
    }

    // ================ Metadata Management ================
    private record ChunkMetadata(int index, Path path, String checksum) {}

    private void storeContentTypeMetadata(LocalObjectReference ref, Path filePath) throws IOException {
        String contentType = ContentTypeResolver.resolve(filePath);
        Path metaPath = getMetadataPath(ref).resolveSibling("content-type");
        Files.writeString(metaPath, contentType);
    }

    private String getContentTypeMetadata(LocalObjectReference ref) throws IOException {
        Path metaPath = getMetadataPath(ref).resolveSibling("content-type");
        if (Files.exists(metaPath)) {
            return Files.readString(metaPath);
        }
        return ContentTypeResolver.resolveFromFilename(ref.getObjectKey());
    }

    private void deleteContentTypeMetadata(LocalObjectReference ref) {
        try {
            Path metaPath = getMetadataPath(ref).resolveSibling("content-type");
            Files.deleteIfExists(metaPath);
        } catch (IOException e) {
            log.warn("Failed to delete content-type metadata: {}", e.getMessage());
        }
    }

    private Path getMetadataPath(String uploadId) {
        return tempRoot.resolve(uploadId).resolve("metadata.json");
    }

    private Path getMetadataPath(LocalObjectReference ref) throws IOException {
        return resolveFinalPath(ref).getParent();
    }

    private void updateChunkMetadata(FileChunkInfo chunk, Path chunkPath) throws IOException {
        String uploadId = chunk.getUploadId();
        List<ChunkMetadata> chunks = uploadMetadataCache.computeIfAbsent(uploadId, k -> new ArrayList<>());

        chunks.add(new ChunkMetadata(chunk.getChunkIndex(), chunkPath, chunk.getChecksum()));

        // Persist to disk asynchronously or periodically
        persistChunkMetadata(uploadId, chunks);
    }

    private void persistChunkMetadata(String uploadId, List<ChunkMetadata> chunks) throws IOException {
        Path metadataPath = getMetadataPath(uploadId);
        Path tempMetaPath = createTempFile("meta-");
        try {
            JsonUtils.write(tempMetaPath, chunks);
            ensureParentExists(metadataPath);
            FileUtils.moveAtomic(tempMetaPath, metadataPath);
        } finally {
            silentDelete(tempMetaPath);
        }
    }

    private List<ChunkMetadata> loadChunkMetadata(String uploadId) throws IOException, IncompleteUploadException {
        // Check cache first
        List<ChunkMetadata> cached = uploadMetadataCache.get(uploadId);
        if (cached != null) {
            return cached;
        }

        // Fallback to disk
        Path metadataPath = getMetadataPath(uploadId);
        if (!Files.exists(metadataPath)) {
            throw new IncompleteUploadException("No metadata found for upload: " + uploadId);
        }

        List<ChunkMetadata> chunks = JsonUtils.readList(metadataPath, ChunkMetadata.class);
        uploadMetadataCache.put(uploadId, chunks);
        return chunks;
    }

    // ================ Path Resolution ================
    private Path resolveFinalPath(LocalObjectReference ref) throws IOException {
        String safeBucket = FilePathUtils.sanitize(ref.getBucketName());
        String safeKey = FilePathUtils.sanitize(ref.getObjectKey());
        String safeVersion = FilePathUtils.sanitize(
                ref.getVersionId() != null ? ref.getVersionId() : "latest"
        );

        Path finalPath = FilePathUtils.resolvePath(storageRoot, safeBucket, safeKey, safeVersion)
                .resolve("data");

        ensureParentExists(finalPath);
        return finalPath;
    }

    private Path resolveChunkPath(FileChunkInfo chunk) throws IOException {
        String safeUploadId = FilePathUtils.sanitize(chunk.getUploadId());
        Path chunkPath = tempRoot.resolve(safeUploadId)
                .resolve(chunk.getChunkIndex() + ".chunk");

        ensureParentExists(chunkPath);
        return chunkPath;
    }

    // ================ Validation & Safety ================
    private void validateAllChunks(List<ChunkMetadata> chunks) throws IOException, ChecksumMismatchException {
        for (ChunkMetadata chunk : chunks) {
            if (chunk.checksum() != null && !ChecksumUtils.verify(chunk.path(), chunk.checksum())) {
                throw new ChecksumMismatchException("Chunk " + chunk.index() + " checksum mismatch");
            }
        }
    }

    private void verifyChunkSequence(List<ChunkMetadata> chunks) throws IncompleteUploadException {
        Set<Integer> indices = chunks.stream()
                .map(ChunkMetadata::index)
                .collect(Collectors.toSet());

        int expectedCount = indices.stream().max(Integer::compare).orElse(-1) + 1;
        if (indices.size() != expectedCount) {
            throw new IncompleteUploadException("Missing chunks. Expected: " +
                    expectedCount + ", found: " + indices.size());
        }
    }

    private void ensureParentExists(Path path) throws IOException {
        FileUtils.createDirectory(path.getParent());
    }

    private Path createTempFile(String prefix) throws IOException {
        return Files.createTempFile(tempRoot, prefix, ".tmp");
    }

    private void cleanupUpload(String uploadId) {
        try {
            Path uploadDir = tempRoot.resolve(uploadId);
            if (Files.exists(uploadDir)) {
                FileUtils.deleteDirectory(uploadDir);
                uploadMetadataCache.remove(uploadId);
                log.debug("Cleaned up upload directory: {}", uploadDir);
            }
        } catch (IOException e) {
            log.warn("Failed to cleanup upload {}: {}", uploadId, e.getMessage());
        }
    }

    private void cleanupEmptyParents(Path path) throws IOException {
        Path parent = path.getParent();
        while (!parent.equals(storageRoot) && !parent.equals(tempRoot)) {
            if (Files.list(parent).count() == 0) {
                Files.delete(parent);
                parent = parent.getParent();
            } else {
                break;
            }
        }
    }

    private void silentDelete(Path path) {
        try {
            if (path != null) Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete temp file {}: {}", path, e.getMessage());
        }
    }
}