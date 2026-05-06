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
import com.rakumo.metadata.dto.ObjectMetadataDto;
import com.rakumo.metadata.entity.ObjectMetadata;
import com.rakumo.metadata.exceptions.ObjectNotFoundException;
import com.rakumo.metadata.object.CreateObjectRequest;
import com.rakumo.metadata.object.DeleteObjectRequest;
import com.rakumo.metadata.object.DeleteResponse;
import com.rakumo.metadata.object.GetBucketObjectsRequest;
import com.rakumo.metadata.object.GetObjectRequest;
import com.rakumo.metadata.object.ObjectListResponse;
import com.rakumo.metadata.object.ObjectResponse;
import com.rakumo.metadata.object.ObjectServiceGrpc;
import com.rakumo.metadata.object.UpdateObjectRequest;
import com.rakumo.metadata.services.ObjectMetadataService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * GRPC service implementation for object metadata management.
 */
@GrpcService
@RequiredArgsConstructor
public class ObjectMetadataGrpcService extends ObjectServiceGrpc.ObjectServiceImplBase {

  private final ObjectMetadataService objectMetadataService;

  @Override
  public void createObject(CreateObjectRequest request, StreamObserver<ObjectResponse> responseObserver) {
    try {
      ObjectMetadata objectMetadata = new ObjectMetadata();
      objectMetadata.setObjectKey(request.getObjectKey());
      objectMetadata.setLatestVersionId(request.getLatestVersionId());
      objectMetadata.setLatestEtag(request.getLatestEtag());
      objectMetadata.setLatestSize(request.getLatestSize());

      ObjectMetadataDto dto = objectMetadataService.createObjectMetadata(
              UUID.fromString(request.getId()),
              UUID.fromString(request.getBucketId()),
              objectMetadata
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
      ObjectMetadataDto dto = objectMetadataService.getObject(
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
      List<ObjectMetadataDto> dtos = objectMetadataService.getBucketObject(
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
      ObjectMetadata objectMetadata = new ObjectMetadata();
      objectMetadata.setLatestVersionId(request.getLatestVersionId());
      objectMetadata.setLatestEtag(request.getLatestEtag());
      objectMetadata.setLatestSize(request.getLatestSize());
      objectMetadata.setIsDeleted(request.getIsDeleted());

      ObjectMetadataDto dto = objectMetadataService.updateObject(
              UUID.fromString(request.getObjectId()),
              UUID.fromString(request.getBucketId()),
              objectMetadata
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

  private static ObjectResponse toObjectResponse(ObjectMetadataDto dto) {
    ObjectResponse.Builder builder = ObjectResponse.newBuilder()
            .setId(dto.getId().toString())
            .setBucketId(dto.getBucketId().toString())
            .setObjectKey(dto.getObjectKey())
            .setLatestVersionId(dto.getLatestVersionId())
            .setLatestEtag(dto.getLatestEtag())
            .setLatestSize(dto.getLatestSize())
            .setCreatedAt(toTimestamp(dto.getCreatedAt()))
            .setIsDeleted(dto.getIsDeleted());

    if (dto.getUpdatedAt() != null) {
      builder.setUpdatedAt(toTimestamp(dto.getUpdatedAt()));
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
