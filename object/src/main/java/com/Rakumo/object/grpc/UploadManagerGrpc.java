package com.Rakumo.object.grpc;

import com.Rakumo.object.dto.UploadResponse;
import com.Rakumo.object.exception.*;
import com.google.protobuf.Timestamp;
import com.Rakumo.object.model.FileChunkInfo;
import com.Rakumo.object.model.LocalObjectReference;
import com.Rakumo.object.service.UploadManagerService;
import com.Rakumo.object.upload.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadManagerGrpc extends UploadManagerServiceProtoGrpc.UploadManagerServiceProtoImplBase {

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

            // Use streaming approach to avoid memory issues
            try (InputStream inputStream = request.getFileData().newInput()) {

                com.Rakumo.object.dto.UploadFileRequest dto = new com.Rakumo.object.dto.UploadFileRequest(
                        request.getBucketName(),
                        request.getObjectKey(),
                        inputStream,
                        !request.getContentType().isEmpty() ? request.getContentType() : "application/octet-stream"
                );

                UploadResponse resp = uploadManagerService.handleRegularUpload(dto);
                responseObserver.onNext(toProto(resp));
                responseObserver.onCompleted();
            } catch (MetadataSyncException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException | MetadataServiceException e) {
            log.error("Regular upload failed: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void initiateMultipartUpload(UploadFileRequestMessage request,
                                        StreamObserver<UploadIdResponseMessage> responseObserver) {
        try {
            // Validate request
            if (request.getBucketName().isEmpty() || request.getObjectKey().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Bucket name and object key are required")
                        .asRuntimeException());
                return;
            }

            // Multipart initiation doesn't need file data!
            com.Rakumo.object.dto.UploadFileRequest dto = new com.Rakumo.object.dto.UploadFileRequest(
                    request.getBucketName(),
                    request.getObjectKey(),
                    null,  // No file data needed for initiation
                    !request.getContentType().isEmpty() ? request.getContentType() : "application/octet-stream"
            );

            String uploadId = uploadManagerService.initiateMultipartUpload(dto);

            responseObserver.onNext(UploadIdResponseMessage.newBuilder().setUploadId(uploadId).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Multipart upload initiation failed: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void processChunk(com.Rakumo.object.upload.FileChunkInfoMessage request,
                             StreamObserver<ChunkProcessingResponseMessage> responseObserver) {
        try {
            // Validate chunk
            if (request.getUploadId().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Upload ID is required")
                        .asRuntimeException());
                return;
            }

            FileChunkInfo dto = FileChunkInfo.builder()
                    .uploadId(request.getUploadId())
                    .chunkIndex(request.getChunkIndex())
                    .content(request.getData().toByteArray())
                    .checksum(request.getChecksum())
                    .bucketName(request.getBucketName())
                    .objectKey(request.getObjectKey())
                    .chunkSize(request.getChunkSize())
                    .build();

            uploadManagerService.processChunk(dto);

            // Enhanced response
            responseObserver.onNext(ChunkProcessingResponseMessage.newBuilder()
                    .setSuccess(true)
                    .setChunkIndex(request.getChunkIndex())
                    .setUploadId(request.getUploadId())
                    .build());
            responseObserver.onCompleted();

        } catch (InvalidChunkException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (ChecksumMismatchException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (IOException e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void completeMultipartUpload(UploadIdRequestMessage request,
                                        StreamObserver<UploadResponseMessage> responseObserver) {
        try {
            if (request.getUploadId().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Upload ID is required")
                        .asRuntimeException());
                return;
            }

            UploadResponse resp = uploadManagerService.completeMultipartUpload(request.getUploadId());
            responseObserver.onNext(toProto(resp));
            responseObserver.onCompleted();

        } catch (IncompleteUploadException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (ObjectNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IOException | MetadataServiceException | ChecksumMismatchException e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteObject(com.Rakumo.object.upload.LocalObjectReferenceMessage request,
                             StreamObserver<DeleteResponseMessage> responseObserver) {
        try {
            if (request.getBucketName().isEmpty() || request.getObjectKey().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Bucket name and object key are required")
                        .asRuntimeException());
                return;
            }

            LocalObjectReference ref = LocalObjectReference.builder()
                    .bucketName(request.getBucketName())
                    .objectKey(request.getObjectKey())
                    .versionId(request.getVersionId())
                    .checksum(request.getChecksum())
                    .build();

            com.Rakumo.object.dto.DeleteRequest deleted = uploadManagerService.deleteObject(ref);

            responseObserver.onNext(DeleteResponseMessage.newBuilder()
                    .setSuccess(true)
                    .setBucketName(deleted.getBucketName())
                    .setObjectKey(deleted.getObjectKey())
                    .setVersionId(deleted.getVersionId() != null ? deleted.getVersionId() : "")
                    .build());
            responseObserver.onCompleted();

        } catch (ObjectNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IOException | MetadataServiceException e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // Helper to convert DTO → protobuf
    private UploadResponseMessage toProto(UploadResponse resp) {
        UploadResponseMessage.Builder builder = UploadResponseMessage.newBuilder()
                .setBucketName(resp.getBucketName())
                .setObjectKey(resp.getObjectKey())
                .setVersionId(resp.getVersionId())
                .setChecksum(resp.getChecksum())
                .setSizeBytes(resp.getSizeBytes())  // ADDED!
                .setUploadedAt(Timestamp.newBuilder()
                        .setSeconds(resp.getUploadedAt().getEpochSecond())
                        .setNanos(resp.getUploadedAt().getNano())
                        .build());

        return builder.build();
    }
}
