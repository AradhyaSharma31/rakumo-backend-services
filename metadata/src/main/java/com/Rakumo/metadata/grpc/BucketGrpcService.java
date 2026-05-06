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
import com.rakumo.metadata.bucket.BucketListResponse;
import com.rakumo.metadata.bucket.BucketResponse;
import com.rakumo.metadata.bucket.BucketServiceGrpc;
import com.rakumo.metadata.bucket.CreateBucketRequest;
import com.rakumo.metadata.bucket.DeleteBucketRequest;
import com.rakumo.metadata.bucket.DeleteResponse;
import com.rakumo.metadata.bucket.GetBucketRequest;
import com.rakumo.metadata.bucket.GetUserBucketsRequest;
import com.rakumo.metadata.bucket.UpdateBucketRequest;
import com.rakumo.metadata.dto.BucketDto;
import com.rakumo.metadata.exceptions.BucketNotFoundException;
import com.rakumo.metadata.services.BucketService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * GRPC service implementation for bucket operations.
 */
@GrpcService
@RequiredArgsConstructor
public class BucketGrpcService extends BucketServiceGrpc.BucketServiceImplBase {

  private final BucketService bucketService;

  @Override
  public void createBucket(CreateBucketRequest request,
                           StreamObserver<BucketResponse> responseObserver) {
    try {
      BucketDto dto = bucketService.createBucket(
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
      BucketDto dto = bucketService.getBucket(
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
      BucketDto dto = bucketService.updateBucket(
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
              UUID.fromString(request.getOwnerId()),
              UUID.fromString(request.getBucketId())
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

  @Override
  public void getUserBuckets(GetUserBucketsRequest request,
                             StreamObserver<BucketListResponse> responseObserver) {
    try {
      List<BucketDto> dtos = bucketService.getUserBuckets(UUID.fromString(request.getOwnerId()));
      responseObserver.onNext(toBucketListResponse(dtos));
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(Status.INTERNAL
              .withDescription("Delete failed: " + e.getMessage())
              .asRuntimeException());
    }
  }

  private BucketResponse toBucketResponse(BucketDto dto) {
    BucketResponse response;
    if (dto.getUpdatedAt() != null) {
      response = BucketResponse.newBuilder()
              .setBucketId(dto.getBucketId().toString())
              .setOwnerId(dto.getOwnerId().toString())
              .setName(dto.getName())
              .setVersioningEnabled(dto.isVersioningEnabled())
              .setCreatedAt(toTimestamp(dto.getCreatedAt()))
              .setUpdatedAt(toTimestamp(dto.getUpdatedAt()))
              .build();
    } else {
      response = BucketResponse.newBuilder()
              .setBucketId(dto.getBucketId().toString())
              .setOwnerId(dto.getOwnerId().toString())
              .setName(dto.getName())
              .setVersioningEnabled(dto.isVersioningEnabled())
              .setCreatedAt(toTimestamp(dto.getCreatedAt()))
              .setUpdatedAt(Timestamp.getDefaultInstance())
              .build();
    }

    return response;
  }

  private BucketListResponse toBucketListResponse(List<BucketDto> dtos) {
    return BucketListResponse.newBuilder()
            .addAllBuckets(dtos.stream()
                    .map(this::toBucketResponse)
                    .collect(Collectors.toList()))
            .build();
  }

  private static Timestamp toTimestamp(Instant instant) {
    return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
  }
}
