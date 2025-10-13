package com.Rakumo.object.grpc;

import com.Rakumo.object.download.*;
import com.Rakumo.object.dto.DownloadRequest;
import com.Rakumo.object.dto.DownloadResponse;
import com.Rakumo.object.service.DownloadManagerService;
import com.Rakumo.object.exception.ObjectNotFoundException;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@GrpcService
public class DownloadManagerGrpcService extends DownloadManagerServiceProtoGrpc.DownloadManagerServiceProtoImplBase {

    private final DownloadManagerService downloadManagerService;

    @Override
    public void retrieveFile(DownloadRequestMessage request,
                             StreamObserver<DownloadResponseMessage> responseObserver) {
        try {
            // Validate request
            if (request.getBucketName().isEmpty() || request.getObjectKey().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Bucket name and object key are required")
                        .asRuntimeException());
                return;
            }

            // Convert to DTO
            DownloadRequest downloadRequest = DownloadRequest.builder()
                    .bucketName(request.getBucketName())
                    .objectKey(request.getObjectKey())
                    .versionId(request.getVersionId().isEmpty() ? null : request.getVersionId())
                    .build();

            // Process download
            DownloadResponse response = downloadManagerService.retrieveFile(downloadRequest);

            // Convert file stream to bytes (for small files)
            // For large files, consider using streaming RPC
            byte[] fileData = response.getDataStream().readAllBytes();

            DownloadResponseMessage responseMessage = DownloadResponseMessage.newBuilder()
                    .setBucketName(response.getBucketName())
                    .setObjectKey(response.getObjectKey())
                    .setVersionId(response.getVersionId() != null ? response.getVersionId() : "")
                    .setContentLength(response.getContentLength())
                    .setContentType(response.getContentType() != null ? response.getContentType() : "")
                    .setChecksum(response.getChecksum() != null ? response.getChecksum() : "")
                    .setLastModified(Timestamp.newBuilder()
                            .setSeconds(response.getLastModified().getEpochSecond())
                            .setNanos(response.getLastModified().getNano())
                            .build())
                    .setFileData(ByteString.copyFrom(fileData))
                    .build();

            responseObserver.onNext(responseMessage);
            responseObserver.onCompleted();

        } catch (ObjectNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("File not found: " + e.getMessage())
                    .asRuntimeException());
        } catch (IOException e) {
            log.error("Download failed", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Download failed: " + e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in file download", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
