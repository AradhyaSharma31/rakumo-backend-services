package com.Rakumo.object.service.impl;

import com.Rakumo.object.exception.*;
import com.Rakumo.object.model.*;
import com.Rakumo.object.service.FileStorageService;
import com.Rakumo.object.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${storage.root:/data}")
    private Path storageRoot;
    private final Path tempRoot;

    public FileStorageServiceImpl() {
        this.tempRoot = storageRoot.resolve(".tmp");
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
            ensureParentExists(finalPath);
            Files.move(tempPath, finalPath, StandardCopyOption.ATOMIC_MOVE);
            log.info("Successfully stored file at: {}", finalPath);

        } finally {
            // 4. Cleanup
            silentDelete(tempPath);
        }
    }

    @Override
    public void storeChunk(FileChunkInfo chunk) throws IOException {
        Path chunkPath = resolveChunkPath(chunk);
        ensureParentExists(chunkPath);

        log.debug("Storing chunk {} for upload {}", chunk.getChunkIndex(), chunk.getUploadId());

        // 1. Store chunk data
        try (InputStream is = chunk.getData()) {
            Files.copy(is, chunkPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 2. Update metadata
        updateChunkMetadata(chunk, chunkPath);
    }

    @Override
    public void assembleChunks(String uploadId, LocalObjectReference finalRef)
            throws IOException, IncompleteUploadException {

        Path finalPath = resolveFinalPath(finalRef);
        ensureParentExists(finalPath);
        Path assemblyTempPath = createTempFile("assembly-");

        try {
            log.info("Assembling chunks for upload {}", uploadId);

            // 1. Load metadata
            List<ChunkMetadata> chunks = loadChunkMetadata(uploadId);
            verifyChunkSequence(chunks);

            // 2. Concatenate chunks
            try (OutputStream out = Files.newOutputStream(assemblyTempPath)) {
                for (ChunkMetadata chunk : chunks) {
                    Files.copy(chunk.path(), out);
                    log.trace("Appended chunk {} ({} bytes)", chunk.index(), Files.size(chunk.path()));
                }
            }

            // 3. Verify final checksum
            if (finalRef.getChecksum() != null) {
                String actualChecksum = ChecksumUtils.sha256(assemblyTempPath);
                if (!actualChecksum.equals(finalRef.getChecksum())) {
                    log.error("Final checksum mismatch for upload {}", uploadId);
                    throw new ChecksumMismatchException("Final checksum mismatch");
                }
            }

            // 4. Atomic move
            Files.move(assemblyTempPath, finalPath, StandardCopyOption.ATOMIC_MOVE);
            log.info("Successfully assembled file at: {}", finalPath);

        } finally {
            silentDelete(assemblyTempPath);
            cleanupUpload(uploadId);
        }
    }

    @Override
    public InputStream retrieveFile(LocalObjectReference ref) throws ObjectNotFoundException {
        Path filePath = resolveFinalPath(ref);
        if (!Files.exists(filePath)) {
            log.error("File not found: {}", filePath);
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
        }
    }

    // ================ Metadata Helpers ================
    private record ChunkMetadata(int index, Path path, String checksum) {}

    private Path getMetadataPath(String uploadId) {
        return tempRoot.resolve(uploadId).resolve("metadata.json");
    }

    private void updateChunkMetadata(FileChunkInfo chunk, Path chunkPath) throws IOException {
        Path metadataPath = getMetadataPath(chunk.getUploadId());
        List<ChunkMetadata> chunks = new ArrayList<>();

        // Load existing metadata if exists
        if (Files.exists(metadataPath)) {
            chunks = JsonUtils.readList(metadataPath, ChunkMetadata.class);
        }

        // Add new chunk
        chunks.add(new ChunkMetadata(
                chunk.getChunkIndex(),
                chunkPath,
                chunk.getChecksum()
        ));

        // Write back atomically
        Path tempMetaPath = createTempFile("meta-");
        try {
            JsonUtils.write(tempMetaPath, chunks);
            ensureParentExists(metadataPath);
            Files.move(tempMetaPath, metadataPath, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            silentDelete(tempMetaPath);
        }
    }

    private List<ChunkMetadata> loadChunkMetadata(String uploadId)
            throws IOException, IncompleteUploadException {
        Path metadataPath = getMetadataPath(uploadId);
        if (!Files.exists(metadataPath)) {
            throw new IncompleteUploadException("No metadata found for upload: " + uploadId);
        }
        return JsonUtils.readList(metadataPath, ChunkMetadata.class);
    }

    // ================ Path Resolution ================
    private Path resolveFinalPath(LocalObjectReference ref) {
        return storageRoot.resolve(
                String.format("%s/%s/%s/data",
                        ref.getBucketName(),
                        ref.getObjectKey(),
                        ref.getVersionId() != null ? ref.getVersionId() : "latest"
                )
        );
    }

    private Path resolveChunkPath(FileChunkInfo chunk) {
        return tempRoot.resolve(
                String.format("%s/%d.chunk",
                        chunk.getUploadId(),
                        chunk.getChunkIndex()
                )
        );
    }

    // ================ Safety Helpers ================
    private void verifyChunkSequence(List<ChunkMetadata> chunks) throws IncompleteUploadException {
        Set<Integer> indices = chunks.stream()
                .map(ChunkMetadata::index)
                .collect(Collectors.toSet());

        int expectedCount = Collections.max(indices) + 1;
        if (indices.size() != expectedCount) {
            throw new IncompleteUploadException("Missing chunks. Expected: " +
                    expectedCount + ", found: " + indices.size());
        }
    }

    private void ensureParentExists(Path path) throws IOException {
        Path parent = path.getParent();
        if (!Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    private Path createTempFile(String prefix) throws IOException {
        return Files.createTempFile(tempRoot, prefix, ".tmp");
    }

    private void cleanupUpload(String uploadId) {
        try {
            Path uploadDir = tempRoot.resolve(uploadId);
            if (Files.exists(uploadDir)) {
                FileUtils.deleteDirectory(uploadDir);
                log.debug("Cleaned up upload directory: {}", uploadDir);
            }
        } catch (IOException e) {
            log.warn("Failed to cleanup upload {}: {}", uploadId, e.getMessage());
        }
    }

    private void cleanupEmptyParents(Path path) throws IOException {
        Path parent = path.getParent();
        while (!parent.equals(storageRoot)) {
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