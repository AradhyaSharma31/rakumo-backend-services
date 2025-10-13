package com.Rakumo.object.service.implementation;

import com.Rakumo.object.dto.UploadRequest;
import com.Rakumo.object.dto.UploadResponse;
import com.Rakumo.object.entity.FileChunkInfo;
import com.Rakumo.object.entity.MultipartUploadEntity;
import com.Rakumo.object.entity.RegularObjectEntity;
import com.Rakumo.object.enumeration.UploadStatus;
import com.Rakumo.object.exception.ChecksumMismatchException;
import com.Rakumo.object.exception.MetadataServiceException;
import com.Rakumo.object.repository.MultipartUploadRepository;
import com.Rakumo.object.service.FileChunkService;
import com.Rakumo.object.service.FileStorageService;
import com.Rakumo.object.service.UploadManagerService;
import com.Rakumo.object.util.ChecksumUtils;
import com.Rakumo.object.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadManagerServiceImpl implements UploadManagerService {

    private final FileStorageService fileStorageService;
    private final FileChunkService fileChunkService;
    private final FileChunkServiceImpl fileChunkServiceImpl;
    private final MultipartUploadRepository multipartUploadRepository;
    private final ChecksumUtils checksumUtils;

    private static final long MEMORY_THRESHOLD = 10 * 1024 * 1024; // 10MB

    @Override
    @Transactional
    public UploadResponse handleRegularUpload(UploadRequest request, InputStream fileData)
            throws IOException, MetadataServiceException {
        validateUploadRequest(request);

        try {
            // For small files, read into memory for efficiency
            byte[] fileBytes = fileData.readAllBytes();
            String checksum = checksumUtils.sha256(new ByteArrayInputStream(fileBytes));

            if (fileBytes.length > MEMORY_THRESHOLD) {
                log.warn("Large file upload detected: {} bytes. Consider multipart upload.", fileBytes.length);
            }

            // Store file using FileStorageService
            RegularObjectEntity entity = fileStorageService.storeFile(
                    request.getOwnerId(),
                    request.getBucketName(),
                    request.getObjectKey(),
                    new ByteArrayInputStream(fileBytes),
                    request.getContentType(),
                    checksum
            );

            // Convert entity to response DTO
            return UploadResponse.builder()
                    .bucketName(entity.getBucketName())
                    .objectKey(entity.getObjectKey())
                    .versionId(entity.getVersionId())
                    .checksum(entity.getChecksum())
                    .sizeBytes(entity.getSizeBytes())
                    .uploadedAt(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("Regular upload failed for {}/{}", request.getBucketName(), request.getObjectKey(), e);
            throw new IOException("Upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public String initiateMultipartUpload(UploadRequest request) {
        validateUploadRequest(request);
        String uploadId = UUID.randomUUID().toString();

        MultipartUploadEntity upload = MultipartUploadEntity.builder()
                .uploadId(uploadId)
                .userId(request.getOwnerId())
                .bucketName(request.getBucketName())
                .objectKey(request.getObjectKey())
                .finalFilename(extractFilename(request.getObjectKey()))
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(24 * 60 * 60)) // 24 hours
                .status(UploadStatus.IN_PROGRESS)
                .build();

        multipartUploadRepository.save(upload);
        log.info("Initiated multipart upload: {}", uploadId);
        return uploadId;
    }

    @Override
    @Transactional
    public void uploadChunk(String uploadId, int chunkIndex, InputStream chunkData) throws IOException {
        MultipartUploadEntity upload = multipartUploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found: " + uploadId));

        if (upload.getStatus() != UploadStatus.IN_PROGRESS) {
            throw new IllegalStateException("Upload is not in progress: " + uploadId);
        }

        // Read chunk data
        byte[] chunkBytes = chunkData.readAllBytes();

        // Calculate checksum
        String actualChecksum = checksumUtils.sha256(new ByteArrayInputStream(chunkBytes));

        // Store chunk to filesystem
        Path chunkPath = fileChunkServiceImpl.getChunkPath(uploadId, chunkIndex);
        Files.write(chunkPath, chunkBytes);

        // Create chunk metadata
        FileChunkInfo chunkInfo = FileChunkInfo.builder()
                .uploadId(uploadId)
                .chunkIndex(chunkIndex)
                .chunkSize(chunkBytes.length)
                .checksum(actualChecksum)
                .bucketName(upload.getBucketName())
                .objectKey(upload.getObjectKey())
                .filePath(chunkPath.toString())
                .uploadedAt(Instant.now())
                .build();

        // Add to chunk metadata
        fileChunkServiceImpl.addChunkMetadata(uploadId, chunkInfo);
        log.debug("Uploaded chunk {} for upload {}", chunkIndex, uploadId);
    }

    @Override
    @Transactional
    public UploadResponse completeMultipartUpload(String uploadId)
            throws IOException, MetadataServiceException, ChecksumMismatchException {
        MultipartUploadEntity upload = multipartUploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found: " + uploadId));

        // Get all chunks in order
        List<FileChunkInfo> chunks = fileChunkService.listChunks(uploadId);
        if (chunks.isEmpty()) {
            throw new IOException("No chunks found for upload: " + uploadId);
        }

        // Assemble chunks into single input stream
        InputStream assembledStream = assembleChunks(chunks);

        try {
            // Store assembled file using FileStorageService
            RegularObjectEntity entity = fileStorageService.storeFile(
                    upload.getUserId(),
                    upload.getBucketName(),
                    upload.getObjectKey(),
                    assembledStream,
                    null, // Content type will be auto-detected
                    null  // Checksum will be calculated
            );

            // Update upload status
            upload.setStatus(UploadStatus.COMPLETED);
            multipartUploadRepository.save(upload);

            // Cleanup temp files
            fileChunkService.cleanupUpload(uploadId);

            log.info("Completed multipart upload: {}", uploadId);
            return UploadResponse.builder()
                    .bucketName(entity.getBucketName())
                    .objectKey(entity.getObjectKey())
                    .versionId(entity.getVersionId())
                    .checksum(entity.getChecksum())
                    .sizeBytes(entity.getSizeBytes())
                    .uploadedAt(Instant.now())
                    .build();
        } finally {
            assembledStream.close();
        }
    }

    @Override
    @Transactional
    public void abortMultipartUpload(String uploadId) throws IOException {
        MultipartUploadEntity upload = multipartUploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found: " + uploadId));

        // Clean up stored chunks
        fileChunkService.cleanupUpload(uploadId);
        multipartUploadRepository.delete(upload);
        log.info("Aborted multipart upload: {}", uploadId);
    }

    private InputStream assembleChunks(List<FileChunkInfo> chunks) throws IOException {
        if (chunks.isEmpty()) {
            return new ByteArrayInputStream(new byte[0]);
        }

        // Create sequence of input streams from chunk files
        List<InputStream> chunkStreams = chunks.stream()
                .map(chunk -> {
                    try {
                        return Files.newInputStream(Path.of(chunk.getFilePath()));
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read chunk: " + chunk.getFilePath(), e);
                    }
                })
                .toList();

        return new SequenceInputStream(Collections.enumeration(chunkStreams));
    }

    private void validateUploadRequest(UploadRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Upload request cannot be null");
        }
        if (request.getBucketName() == null || request.getBucketName().trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name is required");
        }
        if (request.getObjectKey() == null || request.getObjectKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Object key is required");
        }
    }

    private String extractFilename(String objectKey) {
        return java.nio.file.Paths.get(objectKey).getFileName().toString();
    }
}
