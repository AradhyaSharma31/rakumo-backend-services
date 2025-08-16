package com.Rakumo.object.service.implementation;

import com.Rakumo.object.exception.InvalidChunkException;
import com.Rakumo.object.model.FileChunkInfo;
import com.Rakumo.object.model.LocalObjectReference;
import com.Rakumo.object.service.FileChunkService;
import com.Rakumo.object.util.FileUtils;
import com.Rakumo.object.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileChunkServiceImpl implements FileChunkService {

    @Value("${storage.temp.root:/tmp/uploads}")
    private Path tempRoot;

    // In-memory tracking of active uploads
    private final Map<String, Instant> activeUploads = new ConcurrentHashMap<>();

    @Override
    public String initiateMultipartUpload(LocalObjectReference ref) {
        String uploadId = UUID.randomUUID().toString();
        Path uploadDir = tempRoot.resolve(uploadId);

        try {
            FileUtils.createDirectory(uploadDir);
            activeUploads.put(uploadId, Instant.now());
            log.info("Initiated multipart upload: {}", uploadId);
            return uploadId;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create upload directory", e);
        }
    }

    @Override
    public void validateChunk(FileChunkInfo chunk) throws InvalidChunkException {
        // 1. Verify upload session exists
        if (!activeUploads.containsKey(chunk.getUploadId())) {
            throw new InvalidChunkException("Invalid upload ID: " + chunk.getUploadId());
        }

        // 2. Verify chunk sequence
        List<FileChunkInfo> existingChunks = listChunks(chunk.getUploadId());
        if (!existingChunks.isEmpty()) {
            int lastIndex = existingChunks.get(existingChunks.size() - 1).getChunkIndex();
            if (chunk.getChunkIndex() != lastIndex + 1) {
                throw new InvalidChunkException("Out-of-order chunk. Expected: " + (lastIndex + 1) +
                        ", Received: " + chunk.getChunkIndex());
            }
        } else if (chunk.getChunkIndex() != 0) {
            throw new InvalidChunkException("First chunk must have index 0");
        }
    }

    @Override
    public List<FileChunkInfo> listChunks(String uploadId) {
        Path metadataFile = getMetadataPath(uploadId);
        if (!Files.exists(metadataFile)) {
            return Collections.emptyList();
        }

        try {
            return JsonUtils.readList(metadataFile, FileChunkInfo.class).stream()
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

        activeUploads.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(cutoff)) {
                try {
                    Path uploadDir = tempRoot.resolve(entry.getKey());
                    FileUtils.deleteDirectory(uploadDir);
                    log.info("Cleaned up stale upload: {}", entry.getKey());
                    return true;
                } catch (IOException e) {
                    log.warn("Failed to cleanup upload {}: {}", entry.getKey(), e.getMessage());
                    return false;
                }
            }
            return false;
        });
    }

    // ================ Helper Methods ================
    private Path getMetadataPath(String uploadId) {
        return tempRoot.resolve(uploadId).resolve("metadata.json");
    }

    private Path getChunkPath(String uploadId, int chunkIndex) {
        return tempRoot.resolve(uploadId).resolve(chunkIndex + ".chunk");
    }
}
