package com.Rakumo.object.service.implementation;

import com.Rakumo.object.exception.InvalidChunkException;
import com.Rakumo.object.entity.FileChunkInfo;
import com.Rakumo.object.entity.LocalObjectReference;
import com.Rakumo.object.service.FileChunkService;
import com.Rakumo.object.util.ChecksumUtils;
import com.Rakumo.object.util.FilePathUtils;
import com.Rakumo.object.util.FileUtils;
import com.Rakumo.object.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileChunkServiceImpl implements FileChunkService {

    @Value("${storage.root}/.tmp")
    private String tempRootPath;

    private Path tempRoot;

    @PostConstruct
    public void init() {
        this.tempRoot = Paths.get(tempRootPath);
        try {
            FileUtils.createDirectory(tempRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create temp root", e);
        }
    }

    @Override
    public String initiateMultipartUpload(LocalObjectReference ref) {
        String uploadId = UUID.randomUUID().toString();
        String safeUploadId = FilePathUtils.sanitize(uploadId);
        Path uploadDir = tempRoot.resolve(safeUploadId);

        try {
            FileUtils.createDirectory(uploadDir);
            Path metadataFile = uploadDir.resolve("metadata.json");

            Map<String, String> metadata = new HashMap<>();
            metadata.put("uploadId", uploadId);
            metadata.put("bucketName", ref.getBucketName());
            metadata.put("objectKey", ref.getObjectKey());
            metadata.put("startedAt", Instant.now().toString());
            metadata.put("lastActivity", Instant.now().toString());

            JsonUtils.write(metadataFile, metadata);
            log.info("Initiated multipart upload: {}", uploadId);
            return uploadId;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create upload directory", e);
        }
    }

    @Override
    public void validateChunk(FileChunkInfo chunk) throws InvalidChunkException, IOException {
        // 1. Validate upload exists
        Path metadataFile = getMetadataPath(chunk.getUploadId());
        if (!Files.exists(metadataFile)) {
            throw new InvalidChunkException("Invalid upload ID: " + chunk.getUploadId());
        }

        // 2. Update activity timestamp
        updateLastActivity(chunk.getUploadId());

        // 3. Validate checksum if provided
        if (chunk.getChecksum() != null) {
            Path chunkPath = getChunkPath(chunk.getUploadId(), chunk.getChunkIndex());
            if (Files.exists(chunkPath) && !ChecksumUtils.verify(chunkPath, chunk.getChecksum())) {
                throw new InvalidChunkException("Chunk checksum mismatch");
            }
        }
    }

    private void updateLastActivity(String uploadId) {
        try {
            Path metadataFile = getMetadataPath(uploadId);
            if (Files.exists(metadataFile)) {
                Map<String, String> metadata = JsonUtils.readValue(metadataFile,
                        new TypeReference<Map<String, String>>(){});
                metadata.put("lastActivity", Instant.now().toString());
                JsonUtils.write(metadataFile, metadata);
            }
        } catch (IOException e) {
            log.warn("Failed to update lastActivity for upload {}: {}", uploadId, e.getMessage());
        }
    }

    @Override
    public List<FileChunkInfo> listChunks(String uploadId) {
        Path chunksFile = getChunksDataPath(uploadId);
        if (!Files.exists(chunksFile)) return Collections.emptyList();

        try {
            return JsonUtils.readList(chunksFile, FileChunkInfo.class).stream()
                    .sorted(Comparator.comparingInt(FileChunkInfo::getChunkIndex))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to read chunks for upload {}: {}", uploadId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void cleanupStaleChunks(Duration olderThan) {
        Instant cutoff = Instant.now().minus(olderThan);
        try {
            if (!Files.exists(tempRoot)) return;

            Files.list(tempRoot).forEach(uploadDir -> {
                try {
                    Path metadataFile = uploadDir.resolve("metadata.json");
                    if (Files.exists(metadataFile)) {
                        Map<String, String> metadata = JsonUtils.readValue(metadataFile,
                                new TypeReference<Map<String, String>>(){});
                        Instant lastActivity = Instant.parse(metadata.get("lastActivity"));
                        if (lastActivity.isBefore(cutoff)) {
                            FileUtils.deleteDirectory(uploadDir);
                            log.info("Cleaned up stale upload: {}", uploadDir.getFileName());
                        }
                    }
                } catch (IOException e) {
                    log.warn("Failed to process upload directory {}: {}", uploadDir, e.getMessage());
                }
            });
        } catch (IOException e) {
            log.warn("Failed to list temp uploads: {}", e.getMessage());
        }
    }

    @Override
    public void cleanupUpload(String uploadId) throws IOException {
        String safeUploadId = FilePathUtils.sanitize(uploadId);
        Path uploadDir = tempRoot.resolve(safeUploadId);
        if (Files.exists(uploadDir)) {
            FileUtils.deleteDirectory(uploadDir);
            log.info("Cleaned up upload: {}", uploadId);
        }
    }

    // Helper Methods
    private Path getMetadataPath(String uploadId) {
        return tempRoot.resolve(FilePathUtils.sanitize(uploadId)).resolve("metadata.json");
    }

    private Path getChunksDataPath(String uploadId) {
        return tempRoot.resolve(FilePathUtils.sanitize(uploadId)).resolve("chunks.json");
    }

    public Path getChunkPath(String uploadId, int chunkIndex) throws IOException {
        Path chunkPath = tempRoot.resolve(FilePathUtils.sanitize(uploadId))
                .resolve(chunkIndex + "_chunk_" + ".part");
        FileUtils.ensureDirectoryExists(chunkPath.getParent());
        return chunkPath;
    }

    public void saveChunkMetadata(String uploadId, List<FileChunkInfo> chunks) throws IOException {
        Path chunksFile = getChunksDataPath(uploadId);
        JsonUtils.write(chunksFile, chunks);
    }

    public void addChunkMetadata(String uploadId, FileChunkInfo chunk) throws IOException {
        List<FileChunkInfo> chunks = new ArrayList<>(listChunks(uploadId));  // mutable copy
        chunks.add(chunk);
        saveChunkMetadata(uploadId, chunks);
    }
}
