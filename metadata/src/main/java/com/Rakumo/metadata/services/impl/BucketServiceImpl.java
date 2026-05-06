/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.rakumo.metadata.services.implementation;

import com.rakumo.metadata.dto.BucketDto;
import com.rakumo.metadata.exceptions.BucketNotFoundException;
import com.rakumo.metadata.exceptions.ObjectDeletionException;
import com.rakumo.metadata.exceptions.UnauthorizedAccessException;
import com.rakumo.metadata.mapper.BucketMapper;
import com.rakumo.metadata.entity.Bucket;
import com.rakumo.metadata.entity.ObjectMetadata;
import com.rakumo.metadata.repository.BucketRepo;
import com.rakumo.metadata.repository.ObjectMetadataRepo;
import com.rakumo.metadata.grpc.ObjectGrpcClient;
import com.rakumo.metadata.services.BucketService;
import com.rakumo.object.storage.DeleteObjectsInBucketResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service provides bucket apis.
 */
@Service
public class BucketServiceImpl implements BucketService {

  private static final Logger LOGGER = Logger.getLogger(BucketServiceImpl.class.getName());

  private final BucketRepo bucketRepository;
  private final BucketMapper bucketMapper;
  private final ObjectGrpcClient objectGrpcClient;
  private final ObjectMetadataRepo objectMetadataRepo;

  /**
   * Versioning enabled by default for all buckets.
   * This can be overridden during bucket creation or update.
   */
  public BucketServiceImpl(BucketRepo bucketRepository,
                           BucketMapper bucketMapper, ObjectGrpcClient objectGrpcClient,
                           ObjectMetadataRepo objectMetadataRepo) {
    this.bucketRepository = bucketRepository;
    this.bucketMapper = bucketMapper;
    this.objectGrpcClient = objectGrpcClient;
    this.objectMetadataRepo = objectMetadataRepo;
  }

  @Override
  @Transactional
  public BucketDto createBucket(UUID ownerId, String name,
                                Boolean versoningEnabled, String region) {
    LOGGER.info("Creating bucket for owner: " + ownerId);

    Bucket bucket = new Bucket();
    bucket.setOwnerId(ownerId);
    bucket.setName(name);
    bucket.setVersioningEnabled(versoningEnabled);
    bucket.setRegion(region);
    bucket.setCreatedAt(Instant.now());

    Bucket savedBucket = bucketRepository.save(bucket);
    LOGGER.info("Created bucket with ID: " + savedBucket.getBucketId());

    return bucketMapper.toDto(savedBucket);
  }

  @Override
  @Transactional(readOnly = true)
  public BucketDto getBucket(UUID ownerId, UUID bucketId)
            throws BucketNotFoundException, UnauthorizedAccessException {
    LOGGER.info("Fetching bucket " + bucketId + " for owner " + ownerId);

    Bucket bucket = bucketRepository.findById(bucketId)
            .orElseThrow(() -> {
              LOGGER.warning("Bucket not found: " + bucketId);
              return new BucketNotFoundException("Bucket not found: " + bucketId);
            });

    if (!bucket.getOwnerId().equals(ownerId)) {
      LOGGER.warning("Unauthorized access attempt by " + ownerId + " to bucket " + bucketId);
      throw new UnauthorizedAccessException("Access denied to bucket");
    }

    return bucketMapper.toDto(bucket);
  }

  @Override
  @Transactional
  public BucketDto updateBucket(UUID bucketId, String name,
                                Boolean versioningEnabled, String region)
            throws BucketNotFoundException {
    LOGGER.info("Updating bucket: " +  bucketId);

    Bucket bucket = bucketRepository.findById(bucketId)
            .orElseThrow(() -> {
              LOGGER.warning("Update failed - bucket not found: " + bucketId);
              return new BucketNotFoundException("Bucket not found: " + bucketId);
            });

    if (name != null) {
      bucket.setName(name);
    }
    if (versioningEnabled != null) {
      bucket.setVersioningEnabled(versioningEnabled);
    }
    if (region != null) {
      bucket.setRegion(region);
    }
    bucket.setUpdatedAt(Instant.now());

    Bucket updatedBucket = bucketRepository.save(bucket);
    LOGGER.info("Bucket " + bucketId + " updated successfully");

    return bucketMapper.toDto(updatedBucket);
  }

  @Override
  public void deleteBucket(UUID ownerId, UUID bucketId)
          throws BucketNotFoundException, UnauthorizedAccessException, ObjectDeletionException {

    LOGGER.info("Deleting bucket " + bucketId + " for owner " + ownerId);

    Bucket bucket = bucketRepository.findById(bucketId)
            .orElseThrow(() -> {
              LOGGER.warning("Delete failed - bucket not found: " + bucketId);
              return new BucketNotFoundException("Bucket not found: " + bucketId);
            });

    if (!bucket.getOwnerId().equals(ownerId)) {
      LOGGER.warning("Unauthorized delete attempt by " + ownerId + " to bucket " + bucketId);
      throw new UnauthorizedAccessException("Delete permission denied");
    }

    List<ObjectMetadata> objects = objectMetadataRepo.findByBucketBucketId(bucketId);

    if (!objects.isEmpty()) {
      List<String> objectKeys = new ArrayList<>();
      List<String> fileIds = new ArrayList<>();

      for (ObjectMetadata obj : objects) {
        objectKeys.add(obj.getObjectKey());
        fileIds.add(obj.getId().toString());
      }

      DeleteObjectsInBucketResponse response = objectGrpcClient.deleteObjectsInBucket(
              ownerId.toString(),
              bucketId.toString(),
              objectKeys,
              fileIds
      );

      if (response.getDeletedCount() != objectKeys.size()) {
        String errorMsg = String.format(
                "Failed to delete all objects. Deleted %d/%d. Failures: %s",
                response.getDeletedCount(),
                objectKeys.size(),
                response.getFailedDeletionsList()
        );
        LOGGER.warning(errorMsg);
        throw new ObjectDeletionException(errorMsg);
      }
    }

    bucketRepository.delete(bucket);
    LOGGER.info("Bucket " + bucketId + "  deleted successfully");

    bucketRepository.flush();
    LOGGER.info("Transaction committed successfully");
  }

  @Override
  public List<BucketDto> getUserBuckets(UUID ownerId) {
    LOGGER.info("Listing buckets for owner: " + ownerId);
    List<Bucket> buckets = bucketRepository.findAllByOwnerId(ownerId);
    return buckets.stream()
                .map(bucketMapper::toDto)
                .collect(Collectors.toList());
    }
}
