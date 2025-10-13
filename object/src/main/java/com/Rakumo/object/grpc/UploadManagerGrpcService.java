package com.Rakumo.object.grpc;

import com.Rakumo.object.upload.*;
import com.Rakumo.object.dto.UploadRequest;
import com.Rakumo.object.dto.UploadResponse;
import com.Rakumo.object.service.UploadManagerService;
import com.Rakumo.object.exception.MetadataServiceException;
import com.Rakumo.object.util.ContentTypeResolver;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@GrpcService
public class UploadManagerGrpcService extends UploadManagerServiceProtoGrpc.UploadManagerServiceProtoImplBase {

    private final UploadManagerService uploadManagerService;

    @Override
    public void handleRegularUpload(UploadFileRequestMessage request,
                                    StreamObserver<UploadResponseMessage> responseObserver) {
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

            // Convert to DTO
            UploadRequest uploadRequest = UploadRequest.builder()
                    .bucketName(request.getBucketName())
                    .objectKey(request.getObjectKey())
                    .ownerId(request.getOwnerId())
                    .contentType(request.getContentType().isEmpty() ? "application/octet-stream" : request.getContentType())
                    .build();

            // Process upload
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(request.getFileData().toByteArray())) {
                UploadResponse response = uploadManagerService.handleRegularUpload(uploadRequest, inputStream);

                responseObserver.onNext(toProto(response));
                responseObserver.onCompleted();
            }

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (IOException | MetadataServiceException e) {
            log.error("Regular upload failed", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Upload failed: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in regular upload", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void initiateMultipartUpload(InitiateMultipartRequestMessage request,
                                        StreamObserver<InitiateMultipartResponseMessage> responseObserver) {
        try {
            // Validate request
            if (request.getBucketName().isEmpty() || request.getObjectKey().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Bucket name and object key are required")
                        .asRuntimeException());
                return;
            }

            // Convert to DTO
            UploadRequest uploadRequest = UploadRequest.builder()
                    .bucketName(request.getBucketName())
                    .objectKey(request.getObjectKey())
                    .ownerId(request.getOwnerId().isEmpty() ? "system" : request.getOwnerId())
                    .contentType(request.getContentType().isEmpty() ? ContentTypeResolver.resolveFromFilename(request.getObjectKey()) : request.getContentType())
                    .build();

            // Initiate multipart upload
            String uploadId = uploadManagerService.initiateMultipartUpload(uploadRequest);

            InitiateMultipartResponseMessage response = InitiateMultipartResponseMessage.newBuilder()
                    .setUploadId(uploadId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Multipart upload initiation failed", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to initiate upload: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void uploadChunk(UploadChunkRequestMessage request,
                            StreamObserver<UploadChunkResponseMessage> responseObserver) {

        try {
            // Validate request
            if (request.getUploadId().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Upload ID is required")
                        .asRuntimeException());
                return;
            }

            if (request.getChunkData().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Chunk data cannot be empty")
                        .asRuntimeException());
                return;
            }

            // Upload chunk
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(request.getChunkData().toByteArray())) {
                uploadManagerService.uploadChunk(
                        request.getUploadId(),
                        request.getChunkIndex(),
                        inputStream
                );

                UploadChunkResponseMessage response = UploadChunkResponseMessage.newBuilder()
                        .setSuccess(true)
                        .setChunkIndex(request.getChunkIndex())
                        .setUploadId(request.getUploadId())
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (IOException e) {
            log.error("Chunk upload failed", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Chunk upload failed: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in chunk upload", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void completeMultipartUpload(CompleteMultipartRequestMessage request,
                                        StreamObserver<UploadResponseMessage> responseObserver) {
        try {
            // Validate request
            if (request.getUploadId().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Upload ID is required")
                        .asRuntimeException());
                return;
            }

            // Complete multipart upload
            UploadResponse response = uploadManagerService.completeMultipartUpload(request.getUploadId());

            responseObserver.onNext(toProto(response));
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (IOException | MetadataServiceException e) {
            log.error("Multipart upload completion failed", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Upload completion failed: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in upload completion", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void abortMultipartUpload(AbortMultipartRequestMessage request,
                                     StreamObserver<AbortMultipartResponseMessage> responseObserver) {
        try {
            // Validate request
            if (request.getUploadId().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Upload ID is required")
                        .asRuntimeException());
                return;
            }

            // Abort multipart upload
            uploadManagerService.abortMultipartUpload(request.getUploadId());

            AbortMultipartResponseMessage response = AbortMultipartResponseMessage.newBuilder()
                    .setSuccess(true)
                    .setUploadId(request.getUploadId())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (IOException e) {
            log.error("Multipart upload abort failed", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Upload abort failed: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in upload abort", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private UploadResponseMessage toProto(UploadResponse response) {
        return UploadResponseMessage.newBuilder()
                .setBucketName(response.getBucketName())
                .setObjectKey(response.getObjectKey())
                .setVersionId(response.getVersionId())
                .setChecksum(response.getChecksum())
                .setSizeBytes(response.getSizeBytes())
                .setUploadedAt(Timestamp.newBuilder()
                        .setSeconds(response.getUploadedAt().getEpochSecond())
                        .setNanos(response.getUploadedAt().getNano())
                        .build())
                .build();
    }
}
