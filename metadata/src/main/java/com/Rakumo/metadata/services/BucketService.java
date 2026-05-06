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

package com.rakumo.metadata.services;

import com.rakumo.metadata.dto.BucketDto;
import com.rakumo.metadata.exceptions.BucketNotFoundException;
import com.rakumo.metadata.exceptions.ObjectDeletionException;
import com.rakumo.metadata.exceptions.UnauthorizedAccessException;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing the lifecycle and retrieval of storage buckets.
 */
public interface BucketService {

  /**
   * Creates a new storage bucket for a specific user.
   *
   * @param ownerId           the unique identifier of the bucket owner
   * @param name              the unique name of the bucket
   * @param versoningEnabled  whether versioning should be enabled for the bucket
   * @param region            the AWS region where the bucket will be located
   * @return the Data Transfer Object representing the created bucket
   */
  BucketDto createBucket(UUID ownerId, String name, Boolean versoningEnabled, String region);

  /**
   * Retrieves a specific bucket by its ID and owner.
   *
   * @param ownerId  the unique identifier of the user requesting the bucket
   * @param bucketId the unique identifier of the bucket to retrieve
   * @return the Data Transfer Object representing the bucket
   * @throws BucketNotFoundException if the bucket does not exist or does not belong to the owner
   * @throws UnauthorizedAccessException if the user does not have permission to access the bucket
   */
  BucketDto getBucket(UUID ownerId, UUID bucketId)
      throws BucketNotFoundException, UnauthorizedAccessException;

  /**
   * Updates the configuration of an existing bucket.
   *
   * @param bucketId          the unique identifier of the bucket to update
   * @param name              the new name for the bucket (can be null to keep existing)
   * @param versioningEnabled the new versioning status (can be null to keep existing)
   * @param region            the new region (can be null to keep existing)
   * @return the updated Data Transfer Object representing the bucket
   * @throws BucketNotFoundException if the bucket with the given ID does not exist
   */
  BucketDto updateBucket(UUID bucketId, String name,
                         Boolean versioningEnabled, String region)
          throws BucketNotFoundException;

  /**
   * Deletes a bucket permanently.
   *
   * @param bucketId the unique identifier of the bucket to delete
   * @param ownerId  the unique identifier of the user requesting the deletion
   * @throws BucketNotFoundException if the bucket does not exist or the user is not the owner
   * @throws UnauthorizedAccessException if the user does not have permission to delete the bucket
   * @throws ObjectDeletionException if the bucket contains objects that cannot be deleted
   */
  void deleteBucket(UUID bucketId, UUID ownerId)
          throws BucketNotFoundException, UnauthorizedAccessException, ObjectDeletionException;

  /**
   * Retrieves all buckets owned by a specific user.
   *
   * @param ownerId the unique identifier of the bucket owner
   * @return a list of Data Transfer Objects representing the user's buckets
   */
  List<BucketDto> getUserBuckets(UUID ownerId);
}
