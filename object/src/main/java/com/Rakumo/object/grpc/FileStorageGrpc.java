package com.Rakumo.object.grpc;

import com.Rakumo.object.storage.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.Rakumo.object.exception.ChecksumMismatchException;
import com.Rakumo.object.exception.IncompleteUploadException;
import com.Rakumo.object.exception.ObjectNotFoundException;
import com.Rakumo.object.model.FileChunkInfo;
import com.Rakumo.object.model.LocalObjectReference;
import com.Rakumo.object.service.FileStorageService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageGrpc extends FileStorageServiceProtoGrpc.FileStorageServiceProtoImplBase {

    private final FileStorageService fileStorageService;

    @Override
    public void storeCompleteFile(StoreFileRequestMessage request, StreamObserver<StoreFileResponseMessage> responseObserver) {
        try {
            // Validate request
            if (request.getData().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Empty data not allowed")
                        .asRuntimeException());
                return;
            }

            LocalObjectReference ref = toModel(request.getRef());

            try (InputStream in = request.getData().newInput()) {
                fileStorageService.storeCompleteFile(ref, in);
            }

            // Enhanced response with metadata
            responseObserver.onNext(StoreFileResponseMessage.newBuilder()
                    .setSuccess(true)
                    .setChecksum(ref.getChecksum() != null ? ref.getChecksum() : "")
                    .setSize(ref.getSize())
                    .build());
            responseObserver.onCompleted();

        } catch (IOException | ChecksumMismatchException e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Storage failed: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void storeChunk(FileChunkInfoMessage request, StreamObserver<StoreChunkResponseMessage> responseObserver) {
        try {
            // Validate chunk
            if (request.getData().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Empty chunk data")
                        .asRuntimeException());
                return;
            }

            FileChunkInfo chunk = FileChunkInfo.builder()
                    .uploadId(request.getUploadId())
                    .chunkIndex(request.getChunkIndex())
                    .content(request.getData().toByteArray())
                    .checksum(request.getChecksum())
                    .bucketName(request.getBucketName())
                    .objectKey(request.getObjectKey())
                    .chunkSize(request.getChunkSize())
                    .build();

            fileStorageService.storeChunk(chunk);

            responseObserver.onNext(StoreChunkResponseMessage.newBuilder()
                    .setSuccess(true)
                    .setChunkIndex(request.getChunkIndex())
                    .build());
            responseObserver.onCompleted();

        } catch (IOException | ChecksumMismatchException e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Chunk storage failed: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void assembleChunks(AssembleRequestMessage request, StreamObserver<AssembleResponseMessage> responseObserver) {
        try {
            fileStorageService.assembleChunks(
                    request.getUploadId(),
                    toModel(request.getFinalRef())
            );

            responseObserver.onNext(AssembleResponseMessage.newBuilder()
                    .setSuccess(true)
                    .setUploadId(request.getUploadId())
                    .build());
            responseObserver.onCompleted();

        } catch (IOException | IncompleteUploadException | ChecksumMismatchException e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Assembly failed: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void retrieveFile(LocalObjectReferenceMessage request, StreamObserver<FileChunkMessage> responseObserver) {
        try (InputStream stream = fileStorageService.retrieveFile(toModel(request))) {
            byte[] buffer = new byte[64 * 1024]; // 64KB chunks
            int bytesRead;

            while ((bytesRead = stream.read(buffer)) != -1) {
                responseObserver.onNext(FileChunkMessage.newBuilder()
                        .setData(ByteString.copyFrom(buffer, 0, bytesRead))
                        .build());
            }

            responseObserver.onCompleted();

        } catch (ObjectNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("File not found: " + e.getMessage())
                    .asRuntimeException());
        } catch (IOException e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Retrieval failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteFile(LocalObjectReferenceMessage request, StreamObserver<DeleteResponseMessage> responseObserver) {
        try {
            fileStorageService.deleteFile(toModel(request));

            responseObserver.onNext(DeleteResponseMessage.newBuilder()
                    .setSuccess(true)
                    .build());
            responseObserver.onCompleted();

        } catch (IOException e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Deletion failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    // ===== Converters =====
    private LocalObjectReference toModel(LocalObjectReferenceMessage proto) {
        return LocalObjectReference.builder()
                .bucketName(proto.getBucketName())
                .objectKey(proto.getObjectKey())
                .versionId(proto.getVersionId())
                .checksum(proto.getChecksum())
                .size(proto.getSize())
                .build();
    }
}