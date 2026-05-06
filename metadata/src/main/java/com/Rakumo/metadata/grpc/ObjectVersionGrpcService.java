package com.Rakumo.metadata.gRPC;

import com.Rakumo.metadata.DTO.ObjectVersionDTO;
import com.Rakumo.metadata.Exceptions.ObjectVersionNotFoundException;
import com.Rakumo.metadata.Services.ObjectVersionService;
import com.google.protobuf.Timestamp;
import com.Rakumo.metadata.object.version.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class ObjectVersionGrpcService extends ObjectVersionServiceGrpc.ObjectVersionServiceImplBase {

    private final ObjectVersionService versionService;

    @Override
    public void createVersion(CreateVersionRequest request,
                              StreamObserver<VersionResponse> responseObserver) {
        try {
            ObjectVersionDTO dto = versionService.createVersion(
                    UUID.fromString(request.getObjectId()),
                    request.getEtag(),
                    request.getStorageLocation(),
                    request.getSize(),
                    request.getContentType(),
                    request.getIsDeleteMarker(),
                    request.getStorageClass()
            );
            responseObserver.onNext(toVersionResponse(dto));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Version creation failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getVersion(GetVersionRequest request,
                           StreamObserver<VersionResponse> responseObserver) {
        try {
            ObjectVersionDTO dto = versionService.getVersion(
                    UUID.fromString(request.getObjectId()),
                    UUID.fromString(request.getVersionId())
            );
            responseObserver.onNext(toVersionResponse(dto));
            responseObserver.onCompleted();
        } catch (ObjectVersionNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Version fetch failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getObjectVersions(GetObjectVersionsRequest request,
                                  StreamObserver<VersionListResponse> responseObserver) {
        try {
            List<ObjectVersionDTO> versions = versionService.getObjectVersions(
                    UUID.fromString(request.getObjectId())
            );

            VersionListResponse.Builder responseBuilder = VersionListResponse.newBuilder();
            versions.forEach(version -> responseBuilder.addVersions(toVersionResponse(version)));

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Version listing failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateVersion(UpdateVersionRequest request,
                              StreamObserver<VersionResponse> responseObserver) {
        try {
            ObjectVersionDTO dto = versionService.updateVersion(
                    UUID.fromString(request.getVersionId()),
                    UUID.fromString(request.getObjectId()),
                    request.getEtag(),
                    request.getStorageLocation(),
                    request.getSize(),
                    request.getContentType(),
                    request.getIsDeleteMarker(),
                    request.getStorageClass()
            );
            responseObserver.onNext(toVersionResponse(dto));
            responseObserver.onCompleted();
        } catch (ObjectVersionNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Version update failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteVersion(DeleteVersionRequest request,
                              StreamObserver<DeleteResponse> responseObserver) {
        try {
            versionService.deleteVersion(
                    UUID.fromString(request.getObjectId()),
                    UUID.fromString(request.getVersionId())
            );

            responseObserver.onNext(DeleteResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Version deleted successfully")
                    .build());
            responseObserver.onCompleted();
        } catch (ObjectVersionNotFoundException e) {
            responseObserver.onNext(DeleteResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Version deletion failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private VersionResponse toVersionResponse(ObjectVersionDTO dto) {
        VersionResponse.Builder builder = VersionResponse.newBuilder()
                .setVersionId(dto.getVersionId().toString())
                .setObjectId(dto.getObjectId().toString())
                .setEtag(dto.getEtag())
                .setStorageLocation(dto.getStorageLocation())
                .setSize(dto.getSize())
                .setContentType(dto.getContentType())
                .setCreatedAt(toTimestamp(dto.getCreatedAt()))
                .setIsDeleteMarker(dto.isDeleteMarker())
                .setStorageClass(dto.getStorageClass());

        // Add custom metadata if present
        if (dto.getCustomMetadata() != null) {
            dto.getCustomMetadata().forEach(metadata ->
                    builder.addCustomMetadata(CustomMetadata.newBuilder()
                            .setKey(metadata.getKey())
                            .setValue(metadata.getValue())
                            .build()));
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