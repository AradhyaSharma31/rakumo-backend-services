package com.Rakumo.object.grpc;

import com.Rakumo.object.storage.*;
import com.google.protobuf.ByteString;
import com.Rakumo.object.service.FileStorageService;
import com.Rakumo.object.entity.RegularObjectEntity;
import com.Rakumo.object.exception.ChecksumMismatchException;
import com.Rakumo.object.exception.ObjectNotFoundException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
@GrpcService
public class FileStorageGrpcService extends FileStorageServiceProtoGrpc.FileStorageServiceProtoImplBase {

    private final FileStorageService fileStorageService;
    private static final int CHUNK_SIZE = 64 * 1024; // 64KB chunks

    @Override
    public void storeFile(StoreFileRequestMessage request, StreamObserver<StoreFileResponseMessage> responseObserver) {
        try {
            // Validate request
            if (request.getBucketName().isEmpty() || request.getObjectKey().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Bucket name and object key are required")
                        .asRuntimeException());
                return;
            }

            if (request.getFileData().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("File data cannot be empty")
                        .asRuntimeException());
                return;
            }

            // Convert to service call - NOW INCLUDING OWNER_ID
            try (InputStream inputStream = new ByteArrayInputStream(request.getFileData().toByteArray())) {
                RegularObjectEntity entity = fileStorageService.storeFile(
                        request.getOwnerId(),
                        request.getBucketName(),
                        request.getObjectKey(),
                        inputStream,
                        request.getContentType().isEmpty() ? null : request.getContentType(),
                        request.getExpectedChecksum().isEmpty() ? null : request.getExpectedChecksum()
                );

                // Build response
                StoreFileResponseMessage response = StoreFileResponseMessage.newBuilder()
                        .setBucketName(entity.getBucketName())
                        .setObjectKey(entity.getObjectKey())
                        .setVersionId(entity.getVersionId())
                        .setChecksum(entity.getChecksum())
                        .setSizeBytes(entity.getSizeBytes())
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }

        } catch (ChecksumMismatchException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Checksum mismatch: " + e.getMessage())
                    .asRuntimeException());
        } catch (IOException e) {
            log.error("File storage failed", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Storage failed: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in file storage", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void retrieveFileStream(RetrieveFileRequestMessage request,
                                   StreamObserver<FileChunkMessage> responseObserver) {
        try {
            // Validate request
            if (request.getBucketName().isEmpty() || request.getObjectKey().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Bucket name and object key are required")
                        .asRuntimeException());
                return;
            }

            // Get file from storage service
            Resource resource = fileStorageService.retrieveFile(
                    request.getBucketName(),
                    request.getObjectKey(),
                    request.getVersionId().isEmpty() ? null : request.getVersionId()
            );

            // Stream file in chunks
            try (InputStream inputStream = resource.getInputStream()) {
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    boolean isLastChunk = inputStream.available() == 0;

                    FileChunkMessage chunk = FileChunkMessage.newBuilder()
                            .setData(ByteString.copyFrom(buffer, 0, bytesRead))
                            .setIsLastChunk(isLastChunk)
                            .build();

                    responseObserver.onNext(chunk);
                }

                responseObserver.onCompleted();
            }

        } catch (ObjectNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("File not found: " + e.getMessage())
                    .asRuntimeException());
        } catch (IOException e) {
            log.error("File retrieval failed", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Retrieval failed: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in file retrieval", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteFile(DeleteFileRequestMessage request,
                                     StreamObserver<DeleteFileResponseMessage> responseObserver) {
        try {
            // Validate request
            if (request.getBucketName().isEmpty() || request.getObjectKey().isEmpty() ||
                    request.getFileId().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Bucket name, object key, and file hash are required")
                        .asRuntimeException());
                return;
            }

            // Delete file using checksum-based path
            fileStorageService.deleteFile(
                    request.getOwnerId(),
                    request.getBucketName(),
                    request.getObjectKey(),
                    request.getFileId()
            );

            // Build response
            DeleteFileResponseMessage response = DeleteFileResponseMessage.newBuilder()
                    .setSuccess(true)
                    .setMessage("File deleted successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (ObjectNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("File not found: " + e.getMessage())
                    .asRuntimeException());
        } catch (IOException e) {
            log.error("File deletion by checksum failed", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Deletion failed: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in file deletion by checksum", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteObjectsInBucket(DeleteObjectsInBucketRequest request,
                                      StreamObserver<DeleteObjectsInBucketResponse> responseObserver) {

        // Validate request
        if (request.getOwnerId().isEmpty() || request.getBucketId().isEmpty() ||
                request.getFileIdList().isEmpty() || request.getObjectKeysList().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Bucket name, object key, and file hash are required")
                    .asRuntimeException());
            return;
        }

        // Validate lists have same size
        if (request.getFileIdList().size() != request.getObjectKeysList().size()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("file_ids and object_keys lists must have same size")
                    .asRuntimeException());
            return;
        }

        int deletedCount = 0;
        List<String> failedFiles = new ArrayList<>();
        List<String> failedReasons = new ArrayList<>();  // Track why each failed

        try {
            for (int i = 0; i < request.getFileIdList().size(); i++) {
                String objectKey = request.getObjectKeysList().get(i);
                String fileId = request.getFileIdList().get(i);

                try {
                    // Delete file
                    fileStorageService.deleteFile(request.getOwnerId(), request.getBucketId(), objectKey, fileId);
                    deletedCount++;
                    log.debug("Successfully deleted file: {} from bucket: {}", objectKey, request.getBucketId());

                } catch (ObjectNotFoundException e) {
                    log.warn("File not found: {}/{}", request.getBucketId(), objectKey);
                    failedFiles.add(objectKey);
                    failedReasons.add("NOT_FOUND: " + e.getMessage());

                } catch (IOException e) {
                    log.error("IO error deleting file: {}/{}", request.getBucketId(), objectKey, e);
                    failedFiles.add(objectKey);
                    failedReasons.add("IO_ERROR: " + e.getMessage());

                } catch (Exception e) {
                    log.error("Unexpected error deleting file: {}/{}", request.getBucketId(), objectKey, e);
                    failedFiles.add(objectKey);
                    failedReasons.add("INTERNAL: " + e.getMessage());
                }
            }

            // Build response
            DeleteObjectsInBucketResponse.Builder responseBuilder = DeleteObjectsInBucketResponse.newBuilder()
                    .setDeletedCount(deletedCount)
                    .addAllFailedDeletions(failedFiles);

            // error message if any failures
            if (!failedFiles.isEmpty()) {
                log.warn("{} deletions failed. First error: {}", failedFiles.size(), failedReasons.get(0));
            }

            // Send response
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            // Catch any unexpected errors in the overall process
            log.error("Unexpected error in batch delete operation", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Batch delete failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
