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

import com.google.protobuf.Timestamp;
import com.rakumo.metadata.dto.ObjectVersionDto;
import com.rakumo.metadata.entity.ObjectMetadata;
import com.rakumo.metadata.entity.ObjectVersion;
import com.rakumo.metadata.exceptions.ObjectVersionNotFoundException;
import com.rakumo.metadata.object.version.CreateVersionRequest;
import com.rakumo.metadata.object.version.CustomMetadata;
import com.rakumo.metadata.object.version.DeleteResponse;
import com.rakumo.metadata.object.version.DeleteVersionRequest;
import com.rakumo.metadata.object.version.GetObjectVersionsRequest;
import com.rakumo.metadata.object.version.GetVersionRequest;
import com.rakumo.metadata.object.version.ObjectVersionServiceGrpc;
import com.rakumo.metadata.object.version.UpdateVersionRequest;
import com.rakumo.metadata.object.version.VersionListResponse;
import com.rakumo.metadata.object.version.VersionResponse;
import com.rakumo.metadata.services.ObjectVersionService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * GRPC service implementation for managing object versions.
 */
@GrpcService
@RequiredArgsConstructor
public class ObjectVersionGrpcService
        extends ObjectVersionServiceGrpc.ObjectVersionServiceImplBase {

  private final ObjectVersionService versionService;

  @Override
  public void createVersion(CreateVersionRequest request,
                            StreamObserver<VersionResponse> responseObserver) {
    try {
      ObjectMetadata objRef = new ObjectMetadata();
      objRef.setId(UUID.fromString(request.getObjectId()));

      ObjectVersion objectVersion = new ObjectVersion();
      objectVersion.setObject(objRef);
      objectVersion.setEtag(request.getEtag());
      objectVersion.setStorageLocation(request.getStorageLocation());
      objectVersion.setSize(request.getSize());
      objectVersion.setContentType(request.getContentType());
      objectVersion.setIsDeleteMarker(request.getIsDeleteMarker());
      objectVersion.setContentType(request.getContentType());

      ObjectVersionDto dto = versionService.createVersion(objectVersion);
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
      ObjectVersionDto dto = versionService.getVersion(
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
      List<ObjectVersionDto> versions = versionService.getObjectVersions(
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
      ObjectMetadata objRef = new ObjectMetadata();
      objRef.setId(UUID.fromString(request.getObjectId()));

      ObjectVersion objectVersion = new ObjectVersion();
      objectVersion.setVersionId(UUID.fromString(request.getVersionId()));
      objectVersion.setObject(objRef);
      objectVersion.setEtag(request.getEtag());
      objectVersion.setStorageLocation(request.getStorageLocation());
      objectVersion.setSize(request.getSize());
      objectVersion.setContentType(request.getContentType());
      objectVersion.setIsDeleteMarker(request.getIsDeleteMarker());
      objectVersion.setStorageClass(request.getStorageClass());

      ObjectVersionDto dto = versionService.updateVersion(objectVersion);
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

  private static VersionResponse toVersionResponse(ObjectVersionDto dto) {
    VersionResponse.Builder builder = VersionResponse.newBuilder()
            .setVersionId(dto.getVersionId().toString())
            .setObjectId(dto.getObjectId().toString())
            .setEtag(dto.getEtag())
            .setStorageLocation(dto.getStorageLocation())
            .setSize(dto.getSize())
            .setContentType(dto.getContentType())
            .setCreatedAt(toTimestamp(dto.getCreatedAt()))
            .setIsDeleteMarker(dto.getIsDeleteMarker())
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

  private static Timestamp toTimestamp(Instant instant) {
    return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }
}
