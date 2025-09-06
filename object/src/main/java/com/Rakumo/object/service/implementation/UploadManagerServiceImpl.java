package com.Rakumo.object.service.implementation;

import com.Rakumo.object.dto.*;
import com.Rakumo.object.exception.*;
import com.Rakumo.object.grpc.MetadataGrpcClient;
import com.Rakumo.object.model.*;
import com.Rakumo.object.service.FileChunkService;
import com.Rakumo.object.service.FileStorageService;
import com.Rakumo.object.service.UploadManagerService;
import com.Rakumo.object.util.ChecksumUtils;
import com.Rakumo.object.util.ContentTypeResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadManagerServiceImpl implements UploadManagerService {

    private static final long MEMORY_UPLOAD_THRESHOLD = 10 * 1024 * 1024; // 10MB

    private final FileStorageService fileStorageService;
    private final FileChunkService fileChunkService;
    private final ChecksumUtils checksumUtils;
    private final MetadataGrpcClient metadataClient;

    @Value("${upload.memory-threshold:10485760}") // 10MB default
    private long memoryUploadThreshold;

    @Override
    public UploadResponse handleRegularUpload(UploadFileRequest request)
            throws IOException, MetadataServiceException {

        validateUploadRequest(request);

        // For small files: read into memory for efficiency
        byte[] bytes = request.getFileData().readAllBytes();
        long size = bytes.length;

        if (size > memoryUploadThreshold) {
            log.warn("Large file upload detected: {} bytes. Consider using multipart upload for better performance.", size);
        }

        try (InputStream checksumStream = new ByteArrayInputStream(bytes);
             InputStream storageStream = new ByteArrayInputStream(bytes)) {

            LocalObjectReference ref = createObjectReference(request);

            // Calculate checksum
            String checksum = checksumUtils.sha256(checksumStream);
            ref.setChecksum(checksum);

            // Detect content type
            String contentType = ContentTypeResolver.resolveFromFilename(ref.getObjectKey());

            // Store file
            fileStorageService.storeCompleteFile(ref, storageStream);

            // Record metadata
            metadataClient.createObject(
                    ref.getBucketName(),
                    ref.getObjectKey(),
                    ref.getVersionId(),
                    ref.getChecksum(),
                    size
            );

            log.info("Successfully uploaded file: {}/{} ({} bytes)",
                    ref.getBucketName(), ref.getObjectKey(), size);

            return UploadResponse.builder()
                    .bucketName(ref.getBucketName())
                    .objectKey(ref.getObjectKey())
                    .versionId(ref.getVersionId())
                    .checksum(ref.getChecksum())
                    .sizeBytes(size)
                    .uploadedAt(Instant.now())
                    .build();

        } catch (ChecksumMismatchException e) {
            throw new IOException("Upload checksum verification failed", e);
        }
    }

    @Override
    public String initiateMultipartUpload(UploadFileRequest request) {
        validateUploadRequest(request);

        LocalObjectReference ref = createObjectReference(request);
        String uploadId = fileChunkService.initiateMultipartUpload(ref);

        log.info("Initiated multipart upload: {} for {}/{}",
                uploadId, ref.getBucketName(), ref.getObjectKey());

        return uploadId;
    }

    @Override
    public void processChunk(FileChunkInfo chunk)
            throws InvalidChunkException, IOException, ChecksumMismatchException {

        validateChunk(chunk);

        fileChunkService.validateChunk(chunk);

        // Verify chunk checksum
        if (chunk.getChecksum() != null) {
            String actualChecksum = checksumUtils.sha256(chunk.getInputStream());
            if (!actualChecksum.equals(chunk.getChecksum())) {
                throw new ChecksumMismatchException("Chunk checksum mismatch for chunk " + chunk.getChunkIndex());
            }
        }

        fileStorageService.storeChunk(chunk);

        log.debug("Processed chunk {} for upload {}", chunk.getChunkIndex(), chunk.getUploadId());
    }

    @Override
    public UploadResponse completeMultipartUpload(String uploadId)
            throws IOException, IncompleteUploadException, ObjectNotFoundException,
            MetadataServiceException, ChecksumMismatchException {

        if (uploadId == null || uploadId.trim().isEmpty()) {
            throw new IllegalArgumentException("Upload ID cannot be null or empty");
        }

        List<FileChunkInfo> chunks = fileChunkService.listChunks(uploadId);
        if (chunks.isEmpty()) {
            throw new IncompleteUploadException("No chunks found for upload: " + uploadId);
        }

        FileChunkInfo firstChunk = chunks.get(0);
        LocalObjectReference ref = LocalObjectReference.builder()
                .bucketName(firstChunk.getBucketName())
                .objectKey(firstChunk.getObjectKey())
                .versionId(UUID.randomUUID().toString())
                .build();

        // Assemble file
        fileStorageService.assembleChunks(uploadId, ref);

        // Calculate final checksum
        String finalChecksum;
        try (InputStream finalFileStream = fileStorageService.retrieveFile(ref)) {
            finalChecksum = checksumUtils.sha256(finalFileStream);
            ref.setChecksum(finalChecksum);
        }

        long totalSize = chunks.stream().mapToLong(FileChunkInfo::getChunkSize).sum();
        String contentType = ContentTypeResolver.resolveFromFilename(ref.getObjectKey());

        // Record metadata
        metadataClient.createObject(
                ref.getBucketName(),
                ref.getObjectKey(),
                ref.getVersionId(),
                ref.getChecksum(),
                totalSize
        );

        // Cleanup this upload
        fileChunkService.cleanupUpload(uploadId);

        log.info("Completed multipart upload {} with {} chunks, total size: {} bytes",
                uploadId, chunks.size(), totalSize);

        return UploadResponse.builder()
                .bucketName(ref.getBucketName())
                .objectKey(ref.getObjectKey())
                .versionId(ref.getVersionId())
                .checksum(ref.getChecksum())
                .sizeBytes(totalSize)
                .uploadedAt(Instant.now())
                .build();
    }

    @Override
    public DeleteRequest deleteObject(LocalObjectReference ref)
            throws IOException, MetadataServiceException {

        if (ref == null) {
            throw new IllegalArgumentException("Object reference cannot be null");
        }

        fileStorageService.deleteFile(ref);
        metadataClient.deleteObject(ref.getBucketName(), ref.getObjectKey());

        log.info("Deleted object: {}/{}", ref.getBucketName(), ref.getObjectKey());

        return new DeleteRequest(ref.getBucketName(), ref.getObjectKey(), ref.getVersionId());
    }

    // ================ Validation Methods ================
    private void validateUploadRequest(UploadFileRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Upload request cannot be null");
        }
        if (request.getBucketName() == null || request.getBucketName().trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        if (request.getObjectKey() == null || request.getObjectKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Object key cannot be null or empty");
        }
        if (request.getFileData() == null) {
            throw new IllegalArgumentException("File data cannot be null");
        }
    }

    private void validateChunk(FileChunkInfo chunk) {
        if (chunk == null) {
            throw new IllegalArgumentException("Chunk cannot be null");
        }
        if (chunk.getUploadId() == null || chunk.getUploadId().trim().isEmpty()) {
            throw new IllegalArgumentException("Upload ID cannot be null or empty");
        }
        if (chunk.getChunkIndex() < 0) {
            throw new IllegalArgumentException("Chunk index cannot be negative");
        }
        if (chunk.getBucketName() == null || chunk.getBucketName().trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }
        if (chunk.getObjectKey() == null || chunk.getObjectKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Object key cannot be null or empty");
        }
    }

    private LocalObjectReference createObjectReference(UploadFileRequest request) {
        return LocalObjectReference.builder()
                .bucketName(request.getBucketName())
                .objectKey(request.getObjectKey())
                .versionId(UUID.randomUUID().toString())
                .build();
    }
}