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

package com.rakumo.gateway.dto;

import java.util.List;

/**
 * Data Transfer Objects (Dtos) for metadata operations in the gateway service.
 * This class contains nested static record classes for various request and response payloads
 * related to buckets, objects, versions, and custom metadata.
 */
public class MetadataDto {

  /**
   * Record class representing a request to create a new bucket.
   *
   * @param ownerId the ID of the bucket owner
   * @param name the name of the bucket
   * @param versioningEnabled whether versioning is enabled for the bucket
   * @param region the region where the bucket will be created
   */
  public record CreateBucketRequestDto(String ownerId, String name, Boolean versioningEnabled, String region) {}

  /**
   * Record class representing a request to update an existing bucket.
   *
   * @param ownerId the ID of the bucket owner
   * @param bucketId the ID of the bucket to be updated
   * @param name the new name of the bucket
   * @param versioningEnabled whether versioning is enabled for the bucket
   * @param region the new region for the bucket
   */
  public record UpdateBucketRequestDto(String ownerId, String bucketId, String name, Boolean versioningEnabled, String region) {}

  /**
   * Record class representing the response for bucket-related operations.
   *
   * @param bucketId the ID of the bucket
   * @param ownerId the ID of the bucket owner
   * @param name the name of the bucket
   * @param createdAt the creation timestamp of the bucket
   * @param versioningEnabled whether versioning is enabled for the bucket
   * @param region the region where the bucket is located
   * @param objects a list of objects contained in the bucket
   * @param updatedAt the last update timestamp of the bucket
   */
  public record BucketResponseDto(
            String bucketId,
            String ownerId,
            String name,
            String createdAt,
            Boolean versioningEnabled,
            String region,
            List<ObjectMetadataDto> objects,
            String updatedAt
  ) {}

  /**
   * Record class representing the response for a list of buckets.
   *
   * @param buckets a list of BucketResponseDto representing the buckets
   */
  public record BucketListResponseDto(List<BucketResponseDto> buckets) {}

  /**
   * Record class representing a request to create a new object in a bucket.
   *
   * @param bucketId the ID of the bucket where the object will be created
   * @param objectKey the key (name) of the object
   * @param latestVersionId the ID of the latest version of the object
   * @param latestEtag the ETag of the latest version of the object
   * @param latestSize the size of the latest version of the object
   */
  public record CreateObjectRequestDto(String bucketId, String objectKey, String latestVersionId,
                                         String latestEtag, Long latestSize) {}

  /**
   * Record class representing a request to update an existing object in a bucket.
   *
   * @param objectId the ID of the object to be updated
   * @param bucketId the ID of the bucket where the object is located
   * @param latestVersionId the ID of the latest version of the object
   * @param latestEtag the ETag of the latest version of the object
   * @param latestSize the size of the latest version of the object
   * @param isDeleted whether the object is marked as deleted
   */
  public record UpdateObjectRequestDto(String objectId, String bucketId, String latestVersionId,
                                         String latestEtag, Long latestSize, Boolean isDeleted) {}

  /**
   * Record class representing the response for object-related operations.
   *
   * @param id the ID of the object
   * @param bucketId the ID of the bucket where the object is located
   * @param objectKey the key (name) of the object
   * @param latestVersionId the ID of the latest version of the object
   * @param latestEtag the ETag of the latest version of the object
   * @param latestSize the size of the latest version of the object
   * @param createdAt the creation timestamp of the object
   * @param updatedAt the last update timestamp of the object
   * @param isDeleted whether the object is marked as deleted
   * @param versions a list of versions associated with the object
   */
  public record ObjectResponseDto(
            String id,
            String bucketId,
            String objectKey,
            String latestVersionId,
            String latestEtag,
            Long latestSize,
            String createdAt,
            String updatedAt,
            Boolean isDeleted,
            List<ObjectVersionDto> versions
  ) {}

  /**
   * Record class representing the response for a list of objects in a bucket.
   *
   * @param objects a list of ObjectResponseDto representing the objects in the bucket
   */
  public record ObjectListResponseDto(List<ObjectResponseDto> objects) {}

  /**
   * Record class representing a request to create a new version of an object.
   *
   * @param objectId the ID of the object for which the version is being created
   * @param etag the ETag of the new version
   * @param storageLocation the storage location of the new version
   * @param size the size of the new version
   * @param contentType the content type of the new version
   * @param isDeleteMarker whether this version is a delete marker
   * @param storageClass the storage class of the new version
   * @param customMetadata a list of custom metadata key-value pairs associated with the new version
   */
  public record CreateVersionRequestDto(String objectId, String etag, String storageLocation, Long size,
                                          String contentType, Boolean isDeleteMarker, String storageClass,
                                          List<CustomMetadataDto> customMetadata) {}

  /**
   * Record class representing a request to update an existing version of an object.
   *
   * @param versionId the ID of the version to be updated
   * @param objectId the ID of the object associated with the version
   * @param etag the new ETag of the version
   * @param storageLocation the new storage location of the version
   * @param size the new size of the version
   * @param contentType the new content type of the version
   * @param isDeleteMarker whether this version is a delete marker
   * @param storageClass the new storage class of the version
   */
  public record UpdateVersionRequestDto(String versionId, String objectId, String etag, String storageLocation,
                                          Long size, String contentType, Boolean isDeleteMarker, String storageClass) {}

  /**
   * Record class representing the response for version-related operations.
   *
   * @param versionId the ID of the version
   * @param objectId the ID of the object associated with the version
   * @param etag the ETag of the version
   * @param storageLocation the storage location of the version
   * @param size the size of the version
   * @param contentType the content type of the version
   * @param createdAt the creation timestamp of the version
   * @param isDeleteMarker whether this version is a delete marker
   * @param storageClass the storage class of the version
   * @param customMetadata a list of custom metadata key-value pairs associated with the version
   */
  public record VersionResponseDto(
            String versionId,
            String objectId,
            String etag,
            String storageLocation,
            Long size,
            String contentType,
            String createdAt,
            Boolean isDeleteMarker,
            String storageClass,
            List<CustomMetadataDto> customMetadata
  ) {}

  /**
   * Record class representing the response for a list of versions associated with an object.
   *
   * @param versions a list of VersionResponseDto representing the versions of the object
   */
  public record VersionListResponseDto(List<VersionResponseDto> versions) {}

  /**
   * Record class representing a custom metadata key-value pair.
   *
   * @param key the key of the custom metadata
   * @param value the value of the custom metadata
   */
  public record CustomMetadataDto(String key, String value) {}

  /**
   * Record class representing a request to update custom metadata for a specific version of an object.
   *
   * @param versionId the ID of the version for which the custom metadata is being updated
   * @param key the key of the custom metadata to be updated
   * @param value the new value of the custom metadata
   */
  public record CustomMetadataRequestDto(String versionId, String key, String value) {}

  /**
   * Record class representing the response for custom metadata operations.
   *
   * @param versionId the ID of the version associated with the custom metadata
   * @param metadata a list of CustomMetadataDto representing the custom metadata key-value pairs
   */
  public record CustomMetadataResponseDto(String versionId, List<CustomMetadataDto> metadata) {}

  /**
   * Record class representing the response for delete operations.
   *
   * @param success whether the delete operation was successful
   * @param message a message providing additional information about the delete operation
   */
  public record DeleteResponseDto(Boolean success, String message) {}

  /**
   * Record class representing the metadata of an object.
   *
   * @param id the ID of the object
   * @param bucketId the ID of the bucket where the object is located
   * @param createdAt the creation timestamp of the object
   * @param objectKey the key (name) of the object
   * @param latestVersionId the ID of the latest version of the object
   * @param latestEtag the ETag of the latest version of the object
   * @param latestSize the size of the latest version of the object
   * @param updatedAt the last update timestamp of the object
   * @param isDeleted whether the object is marked as deleted
   */
  public record ObjectMetadataDto(
            String id,
            String bucketId,
            String createdAt,
            String objectKey,
            String latestVersionId,
            String latestEtag,
            Long latestSize,
            String updatedAt,
            Boolean isDeleted
  ) {}

  /**
   * Record class representing the metadata of a specific version of an object.
   *
   * @param versionId the ID of the version
   * @param objectId the ID of the object associated with the version
   * @param etag the ETag of the version
   * @param size the size of the version
   * @param createdAt the creation timestamp of the version
   * @param isLatest whether this version is the latest version of the object
   */
  public record ObjectVersionDto(
            String versionId,
            String objectId,
            String etag,
            Long size,
            String createdAt,
            Boolean isLatest
  ) {}
}
