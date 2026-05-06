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

package com.rakumo.gateway.controller;

import com.rakumo.gateway.dto.MetadataDto;
import com.rakumo.gateway.mapper.GrpcMapper;
import com.rakumo.gateway.service.GrpcMetadataClientService;
import com.rakumo.metadata.bucket.BucketListResponse;
import com.rakumo.metadata.bucket.BucketResponse;
import com.rakumo.metadata.bucket.CreateBucketRequest;
import com.rakumo.metadata.bucket.DeleteBucketRequest;
import com.rakumo.metadata.bucket.DeleteResponse;
import com.rakumo.metadata.bucket.GetBucketRequest;
import com.rakumo.metadata.bucket.GetUserBucketsRequest;
import com.rakumo.metadata.bucket.UpdateBucketRequest;
import com.rakumo.metadata.object.version.custom.CustomMetadataRequest;
import com.rakumo.metadata.object.version.custom.CustomMetadataResponse;
import com.rakumo.metadata.object.version.custom.GetMetadataRequest;
import com.rakumo.metadata.object.version.custom.RemoveMetadataRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling metadata-related API requests in the gateway service.
 */
@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataGatewayController {

  private final GrpcMetadataClientService metadataClientService;
  private final GrpcMapper mapper;

  // ========== BUCKET ENDPOINTS ==========

  /**
   * Handles the bucket creation by forwarding the request to the Metadata microservice via gRPC.
   *
   * @param requestDto the dto containing the bucket creation request details
   * @return ResponseEntity containing the response dto with the created bucket details
   */
  @PostMapping("/buckets")
  public ResponseEntity<MetadataDto.BucketResponseDto> createBucket(
          @RequestBody MetadataDto.CreateBucketRequestDto requestDto) {
    CreateBucketRequest grpcRequest = mapper.toGrpcCreateBucket(requestDto);
    BucketResponse grpcResponse = metadataClientService.createBucket(grpcRequest);
    MetadataDto.BucketResponseDto responseDto = mapper.toDtoBucket(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Handles the request to retrieve bucket.
   *
   * @param bucketId the ID of the bucket to retrieve
   * @param ownerId the ID of the owner of the bucket
   * @return ResponseEntity containing the response dto with the bucket details
   */
  @GetMapping("/buckets/{bucketId}")
  public ResponseEntity<MetadataDto.BucketResponseDto> getBucket(
          @PathVariable String bucketId,
          @RequestParam String ownerId) {
    GetBucketRequest grpcRequest = mapper.toGrpcGetBucket(bucketId, ownerId);
    BucketResponse grpcResponse = metadataClientService.getBucket(grpcRequest);
    MetadataDto.BucketResponseDto responseDto = mapper.toDtoBucket(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Handles the request to update bucket details.
   *
   * @param bucketId the ID of the bucket to update
   * @param requestDto the dto containing the updated bucket details
   * @return ResponseEntity containing the response dto with the updated bucket details
   */
  @PutMapping("/buckets/{bucketId}")
  public ResponseEntity<MetadataDto.BucketResponseDto> updateBucket(
          @PathVariable String bucketId,
          @RequestBody MetadataDto.UpdateBucketRequestDto requestDto) {
    MetadataDto.UpdateBucketRequestDto updatedDto = new MetadataDto.UpdateBucketRequestDto(
            requestDto.ownerId(), bucketId, requestDto.name(),
            requestDto.versioningEnabled(), requestDto.region()
    );
    UpdateBucketRequest grpcRequest = mapper.toGrpcUpdateBucket(updatedDto);
    BucketResponse grpcResponse = metadataClientService.updateBucket(grpcRequest);
    MetadataDto.BucketResponseDto responseDto = mapper.toDtoBucket(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Handles the request to delete a bucket.
   *
   * @param bucketId the ID of the bucket to delete
   * @param ownerId the ID of the owner of the bucket
   * @return ResponseEntity containing the response dto with the deletion result
   */
  @DeleteMapping("/buckets/{bucketId}")
  public ResponseEntity<MetadataDto.DeleteResponseDto> deleteBucket(
          @PathVariable String bucketId,
          @RequestParam String ownerId) {
    DeleteBucketRequest grpcRequest = mapper.toGrpcDeleteBucket(bucketId, ownerId);
    DeleteResponse grpcResponse = metadataClientService.deleteBucket(grpcRequest);
    MetadataDto.DeleteResponseDto responseDto = toDtoDelete(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  private static MetadataDto.DeleteResponseDto toDtoDelete(DeleteResponse grpcResponse) {
    return new MetadataDto.DeleteResponseDto(
            grpcResponse.getSuccess(),
            grpcResponse.getMessage()
    );
  }

  /**
   * Handles the request to retrieve all buckets for a specific user.
   *
   * @param ownerId the ID of the owner whose buckets are to be retrieved
   * @return ResponseEntity containing the response dto with the list of buckets
   */
  @GetMapping("/buckets")
  public ResponseEntity<MetadataDto.BucketListResponseDto> getUserBuckets(
          @RequestParam String ownerId) {
    GetUserBucketsRequest grpcRequest = mapper.toGrpcGetUserBuckets(ownerId);
    BucketListResponse grpcResponse = metadataClientService.getUserBuckets(grpcRequest);
    MetadataDto.BucketListResponseDto responseDto = mapper.toDtoBucketList(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  // ========== VERSION ENDPOINTS ==========

  //    @PostMapping("/versions")
  //    public ResponseEntity<VersionResponseDto> createVersion(
  //    @RequestBody CreateVersionRequestDto requestDto) {
  //        CreateVersionRequest grpcRequest = mapper.toGrpcCreateVersion(requestDto);
  //        VersionResponse grpcResponse = metadataClientService.createVersion(grpcRequest);
  //        VersionResponseDto responseDto = mapper.toDtoVersion(grpcResponse);
  //        return ResponseEntity.ok(responseDto);
  //    }

  //    @GetMapping("/versions/{versionId}")
  //    public ResponseEntity<VersionResponseDto> getVersion(
  //            @PathVariable String versionId,
  //            @RequestParam String objectId) {
  //        GetVersionRequest grpcRequest = mapper.toGrpcGetVersion(versionId, objectId);
  //        VersionResponse grpcResponse = metadataClientService.getVersion(grpcRequest);
  //        VersionResponseDto responseDto = mapper.toDtoVersion(grpcResponse);
  //        return ResponseEntity.ok(responseDto);
  //    }

  //    @GetMapping("/objects/{objectId}/versions")
  //    public ResponseEntity<VersionListResponseDto> getObjectVersions(
  //    @PathVariable String objectId) {
  //        GetObjectVersionsRequest grpcRequest = mapper.toGrpcGetObjectVersions(objectId);
  //        VersionListResponse grpcResponse = metadataClientService.getObjectVersions(grpcRequest);
  //        VersionListResponseDto responseDto = mapper.toDtoVersionList(grpcResponse);
  //        return ResponseEntity.ok(responseDto);
  //    }

  //    @PutMapping("/versions/{versionId}")
  //    public ResponseEntity<VersionResponseDto> updateVersion(
  //            @PathVariable String versionId,
  //            @RequestBody UpdateVersionRequestDto requestDto) {
  //        UpdateVersionRequestDto updatedDto = new UpdateVersionRequestDto(
  //                versionId,
  //                requestDto.objectId(),
  //                requestDto.etag(),
  //                requestDto.storageLocation(),
  //                requestDto.size(),
  //                requestDto.contentType(),
  //                requestDto.isDeleteMarker(),
  //                requestDto.storageClass()
  //        );
  //        UpdateVersionRequest grpcRequest = mapper.toGrpcUpdateVersion(updatedDto);
  //        VersionResponse grpcResponse = metadataClientService.updateVersion(grpcRequest);
  //        VersionResponseDto responseDto = mapper.toDtoVersion(grpcResponse);
  //        return ResponseEntity.ok(responseDto);
  //    }

  //    @DeleteMapping("/versions/{versionId}")
  //    public ResponseEntity<DeleteResponseDto> deleteVersion(
  //            @PathVariable String versionId,
  //            @RequestParam String objectId) {
  //        DeleteVersionRequest grpcRequest = mapper.toGrpcDeleteVersion(versionId, objectId);
  //        DeleteResponse grpcResponse = metadataClientService.deleteVersion(grpcRequest);
  //        DeleteResponseDto responseDto = mapper.toDtoDelete(grpcResponse);
  //        return ResponseEntity.ok(responseDto);
  //    }

  // ========== CUSTOM METADATA ENDPOINTS ==========

  /**
   * Handles the request to add custom metadata to a specific object version.
   *
   * @param requestDto the dto containing the custom metadata details to be added
   * @return ResponseEntity containing the response dto with the updated metadata details
   */
  @PostMapping("/metadata")
  public ResponseEntity<MetadataDto.CustomMetadataResponseDto> addMetadata(
          @RequestBody MetadataDto.CustomMetadataRequestDto requestDto) {
    CustomMetadataRequest grpcRequest = mapper.toGrpcCustomMetadataRequest(requestDto);
    CustomMetadataResponse grpcResponse = metadataClientService.addMetadata(grpcRequest);
    MetadataDto.CustomMetadataResponseDto responseDto =
            mapper.toDtoCustomMetadataResponse(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Handles the request to retrieve custom metadata for a specific object version.
   *
   * @param versionId the ID of the object version for which to retrieve custom metadata
   * @return ResponseEntity containing the response dto with the custom metadata details
   */
  @GetMapping("/versions/{versionId}/metadata")
  public ResponseEntity<MetadataDto.CustomMetadataResponseDto> getMetadata(
          @PathVariable String versionId) {
    GetMetadataRequest grpcRequest = mapper.toGrpcGetMetadata(versionId);
    CustomMetadataResponse grpcResponse = metadataClientService.getMetadata(grpcRequest);
    MetadataDto.CustomMetadataResponseDto responseDto =
            mapper.toDtoCustomMetadataResponse(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Handles the request to remove a specific custom metadata key from an object version.
   *
   * @param versionId the ID of the object version from which to remove the custom metadata
   * @param key the key of the custom metadata to be removed
   * @return ResponseEntity containing the response dto with the updated metadata
   */
  @DeleteMapping("/versions/{versionId}/metadata")
  public ResponseEntity<MetadataDto.CustomMetadataResponseDto> removeMetadata(
          @PathVariable String versionId,
          @RequestParam String key) {
    RemoveMetadataRequest grpcRequest = mapper.toGrpcRemoveMetadata(versionId, key);
    CustomMetadataResponse grpcResponse = metadataClientService.removeMetadata(grpcRequest);
    MetadataDto.CustomMetadataResponseDto responseDto =
            mapper.toDtoCustomMetadataResponse(grpcResponse);
    return ResponseEntity.ok(responseDto);
  }
}
