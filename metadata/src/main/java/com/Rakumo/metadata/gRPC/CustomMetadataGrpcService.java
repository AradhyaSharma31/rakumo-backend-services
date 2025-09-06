package com.Rakumo.metadata.gRPC;

import com.Rakumo.metadata.DTO.CustomMetadataDTO;
import com.Rakumo.metadata.Exceptions.ObjectVersionNotFoundException;
import com.Rakumo.metadata.Mapper.CustomMetadataMapper;
import com.Rakumo.metadata.Services.CustomMetadataService;
import com.Rakumo.metadata.object.version.custom.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class CustomMetadataGrpcService extends CustomMetadataServiceGrpc.CustomMetadataServiceImplBase {

    private final CustomMetadataService metadataService;
    private final CustomMetadataMapper metadataMapper;

    @Override
    public void addMetadata(CustomMetadataRequest request,
                            StreamObserver<CustomMetadataResponse> responseObserver) {
        try {
            List<CustomMetadataDTO> metadata = metadataService.addMetadata(
                    UUID.fromString(request.getVersionId()),
                    request.getKey(),
                    request.getValue()
            );

            responseObserver.onNext(buildMetadataResponse(request.getVersionId(), metadata));
            responseObserver.onCompleted();
        } catch (ObjectVersionNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to add metadata: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getMetadata(GetMetadataRequest request,
                            StreamObserver<CustomMetadataResponse> responseObserver) {
        try {
            List<CustomMetadataDTO> metadata = metadataService.getMetadata(
                    UUID.fromString(request.getVersionId())
            );

            responseObserver.onNext(buildMetadataResponse(request.getVersionId(), metadata));
            responseObserver.onCompleted();
        } catch (ObjectVersionNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to get metadata: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void removeMetadata(RemoveMetadataRequest request,
                               StreamObserver<CustomMetadataResponse> responseObserver) {
        try {
            List<CustomMetadataDTO> metadata = metadataService.removeMetadata(
                    UUID.fromString(request.getVersionId()),
                    request.getKey()
            );

            responseObserver.onNext(buildMetadataResponse(request.getVersionId(), metadata));
            responseObserver.onCompleted();
        } catch (ObjectVersionNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to remove metadata: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private CustomMetadataResponse buildMetadataResponse(String versionId, List<CustomMetadataDTO> metadataList) {
        CustomMetadataResponse.Builder responseBuilder = CustomMetadataResponse.newBuilder()
                .setVersionId(versionId);

        metadataList.forEach(metadataDTO ->
                responseBuilder.addMetadata(metadataMapper.toProto(metadataDTO))
        );

        return responseBuilder.build();
    }
}