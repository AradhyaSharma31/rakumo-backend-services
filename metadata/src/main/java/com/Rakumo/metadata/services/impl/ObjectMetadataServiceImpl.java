///*
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//
//package com.rakumo.metadata.services.impl;
//
//import com.Rakumo.metadata.dto.ObjectMetadataDto;
//import com.Rakumo.metadata.exceptions.ObjectNotFoundException;
//import com.Rakumo.metadata.exceptions.UnauthorizedAccessException;
//import com.Rakumo.metadata.mapper.ObjectMetadataMapper;
//import com.Rakumo.metadata.entity.Bucket;
//import com.Rakumo.metadata.entity.ObjectMetadata;
//import com.Rakumo.metadata.repository.BucketRepo;
//import com.Rakumo.metadata.repository.ObjectMetadataRepo;
//import com.Rakumo.metadata.services.ObjectMetadataService;
//import java.util.logging.Logger;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
///**
// * Service implementation for managing object metadata.
// */
//@Service
//public class ObjectMetadataServiceImpl implements ObjectMetadataService {
//
//  private static final Logger LOGGER =
//          Logger.getLogger(ObjectMetadataServiceImpl.class.getName());
//
//  private final ObjectMetadataRepo objectMetadataRepo;
//  private final BucketRepo bucketRepo;
//  private final ObjectMetadataMapper objectMapper;
//
//  /**
//   * Constructor for ObjectMetadataServiceImpl
//   * @param objectMetadataRepo Repository for object metadata operations
//   * @param bucketRepo Repository for bucket operations
//   * @param objectMapper Mapper for converting between ObjectMetadata and ObjectMetadataDto
//   */
//  public ObjectMetadataServiceImpl(ObjectMetadataRepo objectMetadataRepo,
//                                     BucketRepo bucketRepo,
//                                     ObjectMetadataMapper objectMapper) {
//    this.objectMetadataRepo = objectMetadataRepo;
//    this.bucketRepo = bucketRepo;
//    this.objectMapper = objectMapper;
//  }
//
//  @Override
//  @Transactional
//  public ObjectMetadataDto createObjectMetadata(com.rakumo.metadata.dto.ObjectMetadataDto objectMetadataDto) {
//    LOGGER.info("Creating object in bucket: " + bucketId);
//
//    Bucket bucket = bucketRepo.findById(bucketId)
//            .orElseThrow(() -> new IllegalArgumentException("Bucket not found: " + bucketId));
//
//    ObjectMetadata objectMetadata = new ObjectMetadata();
//    objectMetadata.setId(objectMetadataDto.getId());
//    objectMetadata.setBucket(objectMetadataDto.getBucketId());
//    objectMetadata.setObjectKey(objectMetadataDto.getObjectKey());
//    objectMetadata.setLatestVersionId(objectMetadataDto.getLatestVersionId());
//    objectMetadata.setLatestSize(objectMetadataDto.getLatestSize());
//    objectMetadata.setLatestEtag(objectMetadataDto.getLatestEtag());
//    objectMetadata.setCreatedAt(Instant.now());
//    objectMetadata.setUpdatedAt(Instant.now());
//    objectMetadata.setDeleted(false);
//
//    ObjectMetadata savedObject = objectMetadataRepo.save(objectMetadata);
//    LOGGER.warning("Created object with ID: " + savedObject.getId());
//
//    return objectMapper.toDto(savedObject);
//  }
//
//  @Override
//  @Transactional(readOnly = true)
//  public ObjectMetadataDto getObject(UUID bucketId, UUID objectId)
//          throws ObjectNotFoundException, UnauthorizedAccessException {
//    LOGGER.warning("Fetching object " + objectId + " from bucket " + bucketId);
//
//    ObjectMetadata object = objectMetadataRepo.findById(objectId)
//            .orElseThrow(() -> {
//              LOGGER.warning("Object not found: " + objectId);
//              return new ObjectNotFoundException("Object not found: " + objectId);
//            });
//    ObjectMetadataDto dto = objectMapper.toDto(object);
//    dto.setBucketId(bucketId);
//    return dto;
//  }
//
//  @Override
//  @Transactional(readOnly = true)
//  public List<ObjectMetadataDto> getBucketObject(UUID bucketId) {
//    LOGGER.warning("Fetching all objects for bucket: " + bucketId);
//
//    List<ObjectMetadata> objects = objectMetadataRepo.findByBucketBucketId(bucketId);
//
//    return objects.stream()
//            .map(objectMapper::toDto)
//            .collect(Collectors.toList());
//  }
//
//  @Override
//  @Transactional
//  public ObjectMetadataDto updateObject(com.rakumo.metadata.dto.ObjectMetadataDto objectMetadataDto)
//          throws ObjectNotFoundException {
//    LOGGER.info("Updating object: " + objectId);
//
//    ObjectMetadata object = objectMetadataRepo.findById(objectMetadataDto.getObjectKey())
//            .orElseThrow(() -> {
//              LOGGER.warning("Update failed - object not found: " + objectId);
//              return new ObjectNotFoundException("Object not found: " + objectId);
//            });
//
//    if (!object.getBucket().getBucketId().equals(bucketId)) {
//      LOGGER.warning("Update failed - object not found: " + objectId);
//      throw new ObjectNotFoundException("Object not found: " + objectId);
//    }
//
//    if (latestVersionId != null) {
//      object.setLatestVersionId(latestVersionId);
//    }
//    if (latestEtag != null) {
//      object.setLatestEtag(latestEtag);
//    }
//    if (latestSize != null) {
//      object.setLatestSize(latestSize);
//    }
//    if (isDeleted != null) {
//      object.setDeleted(isDeleted);
//    }
//    object.setUpdatedAt(Instant.now());
//
//    ObjectMetadata updatedObject = objectMetadataRepo.save(object);
//    LOGGER.warning("Object " + objectMetadataDto.getId() + " updated successfully");
//
//    return objectMapper.toDto(updatedObject);
//  }
//
//  @Override
//  @Transactional
//  public void deleteObject(UUID bucketId, UUID objectId)
//          throws ObjectNotFoundException, UnauthorizedAccessException {
//    LOGGER.info("Deleting object " + objectId + " form bucket " + bucketId);
//
//    ObjectMetadata objectMetadata = objectMetadataRepo.findById(objectId)
//            .orElseThrow(() -> {
//              LOGGER.warning("Delete failed - object not found: " + objectId);
//              return new ObjectNotFoundException("Object not found: " + objectId);
//            });
//
//    if (!objectMetadata.getBucket().getBucketId().equals(bucketId)) {
//      LOGGER.warning("Unauthorized delete attempt to object "
//              + objectId + " from bucket " + bucketId);
//      throw new UnauthorizedAccessException("Delete permission denied");
//    }
//
//    objectMetadataRepo.delete(objectMetadata);
//    LOGGER.warning("Object " + objectId + " deleted successfully");
//  }
//}
