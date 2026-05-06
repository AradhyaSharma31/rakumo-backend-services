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

package com.rakumo.gateway.service;

import com.rakumo.metadata.bucket.BucketListResponse;
import com.rakumo.metadata.bucket.BucketResponse;
import com.rakumo.metadata.bucket.BucketServiceGrpc;
import com.rakumo.metadata.bucket.CreateBucketRequest;
import com.rakumo.metadata.bucket.DeleteBucketRequest;
import com.rakumo.metadata.bucket.GetBucketRequest;
import com.rakumo.metadata.bucket.GetUserBucketsRequest;
import com.rakumo.metadata.bucket.UpdateBucketRequest;
import com.rakumo.metadata.object.CreateObjectRequest;
import com.rakumo.metadata.object.DeleteObjectRequest;
import com.rakumo.metadata.object.GetBucketObjectsRequest;
import com.rakumo.metadata.object.GetObjectRequest;
import com.rakumo.metadata.object.ObjectListResponse;
import com.rakumo.metadata.object.ObjectResponse;
import com.rakumo.metadata.object.ObjectServiceGrpc;
import com.rakumo.metadata.object.UpdateObjectRequest;
import com.rakumo.metadata.object.version.CreateVersionRequest;
import com.rakumo.metadata.object.version.DeleteResponse;
import com.rakumo.metadata.object.version.DeleteVersionRequest;
import com.rakumo.metadata.object.version.GetObjectVersionsRequest;
import com.rakumo.metadata.object.version.GetVersionRequest;
import com.rakumo.metadata.object.version.ObjectVersionServiceGrpc;
import com.rakumo.metadata.object.version.UpdateVersionRequest;
import com.rakumo.metadata.object.version.VersionListResponse;
import com.rakumo.metadata.object.version.VersionResponse;
import com.rakumo.metadata.object.version.custom.CustomMetadataRequest;
import com.rakumo.metadata.object.version.custom.CustomMetadataResponse;
import com.rakumo.metadata.object.version.custom.CustomMetadataServiceGrpc;
import com.rakumo.metadata.object.version.custom.GetMetadataRequest;
import com.rakumo.metadata.object.version.custom.RemoveMetadataRequest;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

/**
 * GrpcMetadataClientService is a service class that acts as a gRPC client.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcMetadataClientService {

  @GrpcClient("metadata-service")
  private BucketServiceGrpc.BucketServiceBlockingStub bucketStub;

  @GrpcClient("metadata-service")
  private ObjectServiceGrpc.ObjectServiceBlockingStub objectStub;

  @GrpcClient("metadata-service")
  private ObjectVersionServiceGrpc.ObjectVersionServiceBlockingStub versionStub;

  @GrpcClient("metadata-service")
  private CustomMetadataServiceGrpc.CustomMetadataServiceBlockingStub customMetadataStub;

  // ========== BUCKET OPERATIONS ==========

  /**
   * Creates a new bucket using the provided CreateBucketRequest.
   *
   * @param request the CreateBucketRequest containing the details of the bucket to be created
   * @return a BucketResponse containing the details of the created bucket
   * @throws RuntimeException if the bucket creation fails due to gRPC service unavailability
   */
  public BucketResponse createBucket(CreateBucketRequest request) {
    try {
      log.info("Creating bucket: {}", request.getName());
      return bucketStub.createBucket(request);
    } catch (StatusRuntimeException e) {
      log.error("Bucket creation failed: {}", e.getStatus());
      throw new RuntimeException("Bucket service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Retrieves a bucket using the provided GetBucketRequest.
   *
   * @param request the GetBucketRequest containing the ID of the bucket to be retrieved
   * @return a BucketResponse containing the details of the retrieved bucket
   * @throws RuntimeException if the bucket retrieval fails due to gRPC service unavailability
   */
  public BucketResponse getBucket(GetBucketRequest request) {
    try {
      log.info("Getting bucket: {}", request.getBucketId());
      return bucketStub.getBucket(request);
    } catch (StatusRuntimeException e) {
      log.error("Bucket fetch failed: {}", e.getStatus());
      throw new RuntimeException("Bucket service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Updates a bucket using the provided UpdateBucketRequest.
   *
   * @param request the UpdateBucketRequest containing the details of the bucket to be updated
   * @return a BucketResponse containing the details of the updated bucket
   * @throws RuntimeException if the bucket update fails due to gRPC service unavailability
   */
  public BucketResponse updateBucket(UpdateBucketRequest request) {
    try {
      log.info("Updating bucket: {}", request.getBucketId());
      return bucketStub.updateBucket(request);
    } catch (StatusRuntimeException e) {
      log.error("Bucket update failed: {}", e.getStatus());
      throw new RuntimeException("Bucket service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Deletes a bucket using the provided DeleteBucketRequest.
   *
   * @param request the DeleteBucketRequest containing the ID of the bucket to be deleted
   * @return a DeleteResponse indicating the success or failure of the deletion
   * @throws RuntimeException if the bucket deletion fails due to gRPC service unavailability
   */
  public com.rakumo.metadata.bucket.DeleteResponse deleteBucket(DeleteBucketRequest request) {
    try {
      log.info("Deleting bucket: {}", request.getBucketId());
      return bucketStub.deleteBucket(request);
    } catch (StatusRuntimeException e) {
      log.error("Bucket deletion failed: {}", e.getStatus());
      throw new RuntimeException("Bucket service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Retrieves the list of buckets owned by a user using the provided GetUserBucketsRequest.
   *
   * @param request the GetUserBucketsRequest containing the ID of the user whose buckets are to be retrieved
   * @return a BucketListResponse containing the list of buckets owned by the user
   * @throws RuntimeException if the bucket list retrieval fails due to gRPC service unavailability
   */
  public BucketListResponse getUserBuckets(GetUserBucketsRequest request) {
    try {
      log.info("Getting buckets for user: {}", request.getOwnerId());
      return bucketStub.getUserBuckets(request);
    } catch (StatusRuntimeException e) {
      log.error("Bucket list failed: {}", e.getStatus());
      throw new RuntimeException("Bucket service unavailable: " + e.getStatus().getDescription());
    }
  }

  // ========== OBJECT OPERATIONS ==========

  /**
   * Creates a new object using the provided CreateObjectRequest.
   *
   * @param request the CreateObjectRequest containing the details of the object to be created
   * @return an ObjectResponse containing the details of the created object
   * @throws RuntimeException if the object creation fails due to gRPC service unavailability
   */
  public ObjectResponse createObject(CreateObjectRequest request) {
    try {
      log.info("Creating object: {}", request.getObjectKey());
      return objectStub.createObject(request);
    } catch (StatusRuntimeException e) {
      log.error("Object creation failed: {}", e.getStatus());
      throw new RuntimeException("Object service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Retrieves an object using the provided GetObjectRequest.
   *
   * @param request the GetObjectRequest containing the ID of the object to be retrieved
   * @return an ObjectResponse containing the details of the retrieved object
   * @throws RuntimeException if the object retrieval fails due to gRPC service unavailability
   */
  public ObjectResponse getObject(GetObjectRequest request) {
    try {
      log.info("Getting object: {}", request.getObjectId());
      return objectStub.getObject(request);
    } catch (StatusRuntimeException e) {
      log.error("Object fetch failed: {}", e.getStatus());
      throw new RuntimeException("Object service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Retrieves the list of objects in a bucket using the provided GetBucketObjectsRequest.
   *
   * @param request the GetBucketObjectsRequest containing the ID of the bucket whose objects are to be retrieved
   * @return an ObjectListResponse containing the list of objects in the bucket
   * @throws RuntimeException if the object list retrieval fails due to gRPC service unavailability
   */
  public ObjectListResponse getBucketObjects(GetBucketObjectsRequest request) {
    try {
      log.info("Getting objects for bucket: {}", request.getBucketId());
      return objectStub.getBucketObjects(request);
    } catch (StatusRuntimeException e) {
      log.error("Object list failed: {}", e.getStatus());
      throw new RuntimeException("Object service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Updates an object using the provided UpdateObjectRequest.
   *
   * @param request the UpdateObjectRequest containing the details of the object to be updated
   * @return an ObjectResponse containing the details of the updated object
   * @throws RuntimeException if the object update fails due to gRPC service unavailability
   */
  public ObjectResponse updateObject(UpdateObjectRequest request) {
    try {
      log.info("Updating object: {}", request.getObjectId());
      return objectStub.updateObject(request);
    } catch (StatusRuntimeException e) {
      log.error("Object update failed: {}", e.getStatus());
      throw new RuntimeException("Object service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Deletes an object using the provided DeleteObjectRequest.
   *
   * @param request the DeleteObjectRequest containing the ID of the object to be deleted
   * @return a DeleteResponse indicating the success or failure of the deletion
   * @throws RuntimeException if the object deletion fails due to gRPC service unavailability
   */
  public com.rakumo.metadata.object.DeleteResponse deleteObject(DeleteObjectRequest request) {
    try {
      log.info("Deleting object: {}", request.getObjectId());
      return objectStub.deleteObject(request);
    } catch (StatusRuntimeException e) {
      log.error("Object deletion failed: {}", e.getStatus());
      throw new RuntimeException("Object service unavailable: " + e.getStatus().getDescription());
    }
  }

  // ========== VERSION OPERATIONS ==========

  /**
   * Creates a new version of an object using the provided CreateVersionRequest.
   *
   * @param request the CreateVersionRequest containing the details of the version to be created
   * @return a VersionResponse containing the details of the created version
   * @throws RuntimeException if the version creation fails due to gRPC service unavailability
   */
  public VersionResponse createVersion(CreateVersionRequest request) {
    try {
      log.info("Creating version for object: {}", request.getObjectId());
      return versionStub.createVersion(request);
    } catch (StatusRuntimeException e) {
      log.error("Version creation failed: {}", e.getStatus());
      throw new RuntimeException("Version service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Retrieves a version of an object using the provided GetVersionRequest.
   *
   * @param request the GetVersionRequest containing the ID of the version to be retrieved
   * @return a VersionResponse containing the details of the retrieved version
   * @throws RuntimeException if the version retrieval fails due to gRPC service unavailability
   */
  public VersionResponse getVersion(GetVersionRequest request) {
    try {
      log.info("Getting version: {}", request.getVersionId());
      return versionStub.getVersion(request);
    } catch (StatusRuntimeException e) {
      log.error("Version fetch failed: {}", e.getStatus());
      throw new RuntimeException("Version service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Retrieves the list of versions for an object using the provided GetObjectVersionsRequest.
   *
   * @param request the GetObjectVersionsRequest containing the ID of the object whose versions are to be retrieved
   * @return a VersionListResponse containing the list of versions for the object
   * @throws RuntimeException if the version list retrieval fails due to gRPC service unavailability
   */
  public VersionListResponse getObjectVersions(GetObjectVersionsRequest request) {
    try {
      log.info("Getting versions for object: {}", request.getObjectId());
      return versionStub.getObjectVersions(request);
    } catch (StatusRuntimeException e) {
      log.error("Version list failed: {}", e.getStatus());
      throw new RuntimeException("Version service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Updates a version of an object using the provided UpdateVersionRequest.
   *
   * @param request the UpdateVersionRequest containing the details of the version to be updated
   * @return a VersionResponse containing the details of the updated version
   * @throws RuntimeException if the version update fails due to gRPC service unavailability
   */
  public VersionResponse updateVersion(UpdateVersionRequest request) {
    try {
      log.info("Updating version: {}", request.getVersionId());
      return versionStub.updateVersion(request);
    } catch (StatusRuntimeException e) {
      log.error("Version update failed: {}", e.getStatus());
      throw new RuntimeException("Version service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Deletes a version of an object using the provided DeleteVersionRequest.
   *
   * @param request the DeleteVersionRequest containing the ID of the version to be deleted
   * @return a DeleteResponse indicating the success or failure of the deletion
   * @throws RuntimeException if the version deletion fails due to gRPC service unavailability
   */
  public DeleteResponse deleteVersion(DeleteVersionRequest request) {
    try {
      log.info("Deleting version: {}", request.getVersionId());
      return versionStub.deleteVersion(request);
    } catch (StatusRuntimeException e) {
      log.error("Version deletion failed: {}", e.getStatus());
      throw new RuntimeException("Version service unavailable: " + e.getStatus().getDescription());
    }
  }

  // ========== CUSTOM METADATA OPERATIONS ==========

  /**
   * Retrieves custom metadata for a version using the provided GetMetadataRequest.
   *
   * @param request the GetMetadataRequest containing the ID of the version whose metadata is to be retrieved
   * @return a CustomMetadataResponse containing the custom metadata for the version
   * @throws RuntimeException if the metadata retrieval fails due to gRPC service unavailability
   */
  public CustomMetadataResponse addMetadata(CustomMetadataRequest request) {
    try {
      log.info("Adding metadata for version: {}", request.getVersionId());
      return customMetadataStub.addMetadata(request);
    } catch (StatusRuntimeException e) {
      log.error("Metadata addition failed: {}", e.getStatus());
      throw new RuntimeException("Metadata service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Retrieves custom metadata for a version using the provided GetMetadataRequest.
   *
   * @param request the GetMetadataRequest containing the ID of the version whose metadata is to be retrieved
   * @return a CustomMetadataResponse containing the custom metadata for the version
   * @throws RuntimeException if the metadata retrieval fails due to gRPC service unavailability
   */
  public CustomMetadataResponse getMetadata(GetMetadataRequest request) {
    try {
      log.info("Getting metadata for version: {}", request.getVersionId());
      return customMetadataStub.getMetadata(request);
    } catch (StatusRuntimeException e) {
      log.error("Metadata fetch failed: {}", e.getStatus());
      throw new RuntimeException("Metadata service unavailable: " + e.getStatus().getDescription());
    }
  }

  /**
   * Removes custom metadata from a version using the provided RemoveMetadataRequest.
   *
   * @param request the RemoveMetadataRequest containing the details of the metadata to be removed
   * @return a CustomMetadataResponse indicating the success or failure of the removal
   * @throws RuntimeException if the metadata removal fails due to gRPC service unavailability
   */
  public CustomMetadataResponse removeMetadata(RemoveMetadataRequest request) {
    try {
      log.info("Removing metadata for version: {}", request.getVersionId());
      return customMetadataStub.removeMetadata(request);
    } catch (StatusRuntimeException e) {
      log.error("Metadata removal failed: {}", e.getStatus());
      throw new RuntimeException("Metadata service unavailable: " + e.getStatus().getDescription());
    }
  }
}
