package com.Rakumo.metadata.gRPC;

import com.Rakumo.metadata.DTO.BucketDTO;
import com.Rakumo.metadata.Exceptions.BucketNotFoundException;
import com.Rakumo.metadata.Services.BucketService;
import com.google.protobuf.Timestamp;
import com.rakumo.metadata.bucket.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class BucketGrpcService extends BucketServiceGrpc.BucketServiceImplBase {

    private final BucketService bucketService;

    @Override
    public void createBucket(CreateBucketRequest request,
                             StreamObserver<BucketResponse> responseObserver) {
        try {
            BucketDTO dto = bucketService.createBucket(
                    UUID.fromString(request.getOwnerId()),
                    request.getName(),
                    request.getVersioningEnabled(),
                    request.getRegion()
            );
            responseObserver.onNext(toBucketResponse(dto));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Create failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getBucket(GetBucketRequest request,
                          StreamObserver<BucketResponse> responseObserver) {
        try {
            BucketDTO dto = bucketService.getBucket(
                    UUID.fromString(request.getOwnerId()),
                    UUID.fromString(request.getBucketId())
            );
            responseObserver.onNext(toBucketResponse(dto));
            responseObserver.onCompleted();
        } catch (BucketNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Get failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateBucket(UpdateBucketRequest request,
                             StreamObserver<BucketResponse> responseObserver) {
        try {
            BucketDTO dto = bucketService.updateBucket(
                    UUID.fromString(request.getBucketId()),
                    request.getName(),
                    request.getVersioningEnabled(),
                    request.getRegion()
            );
            responseObserver.onNext(toBucketResponse(dto));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Update failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteBucket(DeleteBucketRequest request,
                             StreamObserver<DeleteResponse> responseObserver) {
        try {
            bucketService.deleteBucket(
                    UUID.fromString(request.getBucketId()),
                    UUID.fromString(request.getOwnerId())
            );

            responseObserver.onNext(DeleteResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Bucket deleted successfully")
                    .build());
            responseObserver.onCompleted();
        } catch (BucketNotFoundException e) {
            responseObserver.onNext(DeleteResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Delete failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    // make it private after testing is done
    public BucketResponse toBucketResponse(BucketDTO dto) {
        return BucketResponse.newBuilder()
                .setBucketId(dto.getBucketId().toString())
                .setOwnerId(dto.getOwnerId().toString())
                .setName(dto.getName())
                .setVersioningEnabled(dto.isVersioningEnabled())
                .setCreatedAt(toTimestamp(dto.getCreatedAt()))
                .setUpdatedAt(dto.getUpdatedAt() != null ?
                        toTimestamp(dto.getUpdatedAt()) : Timestamp.getDefaultInstance())
                .build();
    }

    public Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

}
