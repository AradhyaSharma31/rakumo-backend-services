/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.rakumo.metadata.grpc;

import com.rakumo.metadata.dto.CustomMetadataDto;
import com.rakumo.metadata.exceptions.ObjectVersionNotFoundException;
import com.rakumo.metadata.mapper.CustomMetadataMapper;
import com.rakumo.metadata.object.version.custom.CustomMetadataRequest;
import com.rakumo.metadata.object.version.custom.CustomMetadataResponse;
import com.rakumo.metadata.object.version.custom.CustomMetadataServiceGrpc;
import com.rakumo.metadata.object.version.custom.GetMetadataRequest;
import com.rakumo.metadata.object.version.custom.RemoveMetadataRequest;
import com.rakumo.metadata.services.CustomMetadataService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * GRPC service implementation for managing custom metadata associated with object versions.
 */
@GrpcService
@RequiredArgsConstructor
public class CustomMetadataGrpcService
        extends CustomMetadataServiceGrpc.CustomMetadataServiceImplBase {

  private final CustomMetadataService metadataService;
  private final CustomMetadataMapper metadataMapper;

  @Override
  public void addMetadata(CustomMetadataRequest request,
                          StreamObserver<CustomMetadataResponse> responseObserver) {
    try {
      List<CustomMetadataDto> metadata = metadataService.addMetadata(
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
      List<CustomMetadataDto> metadata = metadataService.getMetadata(
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
      List<CustomMetadataDto> metadata = metadataService.removeMetadata(
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

  private CustomMetadataResponse buildMetadataResponse(
          String versionId, List<CustomMetadataDto> metadataList) {
    CustomMetadataResponse.Builder responseBuilder = CustomMetadataResponse.newBuilder()
            .setVersionId(versionId);

    metadataList.forEach(metadataDTO ->
            responseBuilder.addMetadata(metadataMapper.toProto(metadataDTO))
    );

    return responseBuilder.build();
  }
}
