package com.Rakumo.metadata.gRPC;

import com.Rakumo.metadata.DTO.ObjectMetadataDTO;
import com.Rakumo.metadata.Exceptions.ObjectNotFoundException;
import com.Rakumo.metadata.Services.ObjectMetadataService;
import com.google.protobuf.Timestamp;
import com.Rakumo.metadata.object.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class ObjectMetadataGrpcService extends ObjectServiceGrpc.ObjectServiceImplBase {

    private final ObjectMetadataService objectMetadataService;

    @Override
    public void createObject(CreateObjectRequest request, StreamObserver<ObjectResponse> responseObserver) {
        try {
            ObjectMetadataDTO dto = objectMetadataService.createObjectMetadata(
              UUID.fromString(request.getBucketId()),
                    request.getObjectKey(),
                    request.getLatestVersionId(),
                    request.getLatestEtag(),
                    request.getLatestSize()
            );
            responseObserver.onNext(toObjectResponse(dto));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Create failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getObject(GetObjectRequest request,
                          StreamObserver<ObjectResponse> responseObserver) {
        try {
            ObjectMetadataDTO dto = objectMetadataService.getObject(
                    UUID.fromString(request.getBucketId()),
                    UUID.fromString(request.getObjectId())
            );
            responseObserver.onNext(toObjectResponse(dto));
            responseObserver.onCompleted();
        } catch (ObjectNotFoundException e) {
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
    public void getBucketObjects(GetBucketObjectsRequest request,
                                 StreamObserver<ObjectListResponse> responseObserver) {
        try {
            List<ObjectMetadataDTO> dtos = objectMetadataService.getBucketObject(
                    UUID.fromString(request.getBucketId())
            );

            ObjectListResponse.Builder responseBuilder = ObjectListResponse.newBuilder();
            dtos.forEach(dto -> responseBuilder.addObjects(toObjectResponse(dto)));

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("List objects failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateObject(UpdateObjectRequest request,
                             StreamObserver<ObjectResponse> responseObserver) {
        try {
            ObjectMetadataDTO dto = objectMetadataService.updateObject(
                    UUID.fromString(request.getObjectId()),
                    UUID.fromString(request.getBucketId()),
                    request.getLatestVersionId(),
                    request.getLatestEtag(),
                    request.getLatestSize(),
                    request.getIsDeleted()
            );
            responseObserver.onNext(toObjectResponse(dto));
            responseObserver.onCompleted();
        } catch (ObjectNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Update failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteObject(DeleteObjectRequest request,
                             StreamObserver<DeleteResponse> responseObserver) {
        try {
            objectMetadataService.deleteObject(
                    UUID.fromString(request.getBucketId()),
                    UUID.fromString(request.getObjectId())
            );

            responseObserver.onNext(DeleteResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Object deleted successfully")
                    .build());
            responseObserver.onCompleted();
        } catch (ObjectNotFoundException e) {
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

    private ObjectResponse toObjectResponse(ObjectMetadataDTO dto) {
        ObjectResponse.Builder builder = ObjectResponse.newBuilder()
                .setId(dto.getId().toString())
                .setBucketId(dto.getBucketId().toString())
                .setObjectKey(dto.getObjectKey())
                .setLatestVersionId(dto.getLatestVersionId())
                .setLatestEtag(dto.getLatestEtag())
                .setLatestSize(dto.getLatestSize())
                .setCreatedAt(toTimestamp(dto.getCreatedAt()))
                .setIsDeleted(dto.isDeleted());

        if (dto.getUpdatedAt() != null) {
            builder.setUpdatedAt(toTimestamp(dto.getUpdatedAt()));
        }

        return builder.build();
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

}
