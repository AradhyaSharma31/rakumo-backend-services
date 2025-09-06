package com.Rakumo.object.grpc;

import com.Rakumo.object.download.FileChunkMessage;
import com.Rakumo.object.download.FileRangeRequestMessage;
import com.Rakumo.object.dto.DownloadRequest;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import com.Rakumo.object.download.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import com.Rakumo.object.dto.DownloadResponse;

import com.Rakumo.object.exception.ObjectNotFoundException;
import com.Rakumo.object.model.LocalObjectReference;
import com.Rakumo.object.service.DownloadManagerService;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedStream;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@GrpcService
@RequiredArgsConstructor
public class DownloadManagerGrpc extends DownloadManagerServiceProtoGrpc.DownloadManagerServiceProtoImplBase {

    private static final int STREAM_CHUNK_SIZE = 64 * 1024;

    // Netty buffer allocation for efficient memory management
    // Essential for streaming large files
    private static final ByteBufAllocator ALLOC = UnpooledByteBufAllocator.DEFAULT;

    private final DownloadManagerService downloadService;

    @Override
    public void retrieveFile(DownloadRequestMessage request, StreamObserver<DownloadResponseMessage> responseObserver) {
        try {
            DownloadRequest dtoReq = DownloadRequest.builder()
                    .bucketName(request.getBucketName())
                    .objectKey(request.getObjectKey())
                    .versionId(emptyToNull(request.getVersionId()))
                    .build();

            DownloadResponse dtoResp = downloadService.retrieveFile(dtoReq);

            responseObserver.onNext(toDownloadResponseMessage(dtoResp));
            responseObserver.onCompleted();

        } catch (ObjectNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IOException e) {
            responseObserver.onError(Status.INTERNAL.withDescription("I/O error: " + e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void generatePresignedUrl(DownloadRequestWithExpiryMessage request,
                                     StreamObserver<PresignedUrlResponseMessage> responseObserver) {
        try {
            DownloadRequestMessage inner = request.getRequest();
            Duration expiry = toJavaDuration(request.getExpiry());

            if (expiry.isNegative()) {
                throw new IllegalArgumentException("Expiry duration cannot be negative");
            }

            DownloadRequest dtoReq = DownloadRequest.builder()
                    .bucketName(inner.getBucketName())
                    .objectKey(inner.getObjectKey())
                    .versionId(emptyToNull(inner.getVersionId()))
                    .build();

            String url = downloadService.generatePresignedUrl(dtoReq, expiry);

            responseObserver.onNext(PresignedUrlResponseMessage.newBuilder().setUrl(url).build());
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to generate URL: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void streamFileRange(FileRangeRequestMessage request, StreamObserver<FileChunkMessage> responseObserver) {
        try {
            LocalObjectReference ref = LocalObjectReference.builder()
                    .bucketName(request.getRef().getBucketName())
                    .objectKey(request.getRef().getObjectKey())
                    .versionId(emptyToNull(request.getRef().getVersionId()))
                    .checksum(emptyToNull(request.getRef().getChecksum()))
                    .build();

            long start = Math.max(0L, request.getStart());
            long end = request.getEnd();

            if (end < start) {
                throw new IllegalArgumentException("End cannot be less than start");
            }

            ChunkedStream chunked = downloadService.streamFileRange(ref, start, end);
            ChunkedInput<ByteBuf> input = (ChunkedInput<ByteBuf>) chunked;

            ByteBuf buf;
            int chunkCount = 0;
            final int MAX_CHUNKS = 1000000; // Safety limit

            while (!input.isEndOfInput() && (buf = input.readChunk(ALLOC)) != null && chunkCount++ < MAX_CHUNKS) {
                try {
                    ByteString data = ByteString.copyFrom(ByteBufUtil.getBytes(buf));
                    responseObserver.onNext(FileChunkMessage.newBuilder()
                            .setData(data)
                            .build());
                } finally {
                    buf.release();
                }
            }

            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (ObjectNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IOException e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Stream error: " + e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // ================== Helper Methods ==================
    private DownloadResponseMessage toDownloadResponseMessage(DownloadResponse dtoResp) {
        DownloadResponseMessage.Builder builder = DownloadResponseMessage.newBuilder()
                .setBucketName(dtoResp.getBucketName())
                .setObjectKey(dtoResp.getObjectKey())
                .setVersionId(nullToEmpty(dtoResp.getVersionId()))
                .setContentLength(dtoResp.getContentLength())
                .setLastModified(toProtoTimestamp(dtoResp.getLastModified()));

        if (dtoResp.getContentType() != null) {
            builder.setContentType(dtoResp.getContentType());
        }
        if (dtoResp.getChecksum() != null) {
            builder.setChecksum(dtoResp.getChecksum());
        }

        return builder.build();
    }

    private Timestamp toProtoTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static Duration toJavaDuration(com.google.protobuf.Duration d) {
        if (d == null) {
            throw new IllegalArgumentException("Duration cannot be null");
        }
        if (d.getSeconds() < 0) {
            throw new IllegalArgumentException("Duration seconds cannot be negative");
        }
        return Duration.ofSeconds(d.getSeconds(), d.getNanos());
    }
}