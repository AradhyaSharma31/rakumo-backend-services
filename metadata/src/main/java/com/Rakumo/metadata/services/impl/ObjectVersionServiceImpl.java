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
//import com.Rakumo.metadata.dto.ObjectVersionDto;
//import com.Rakumo.metadata.exceptions.ObjectVersionNotFoundException;
//import com.Rakumo.metadata.mapper.ObjectVersionMapper;
//import com.Rakumo.metadata.entity.ObjectMetadata;
//import com.Rakumo.metadata.entity.ObjectVersion;
//import com.Rakumo.metadata.repository.ObjectMetadataRepo;
//import com.Rakumo.metadata.repository.ObjectVersionRepo;
//import com.Rakumo.metadata.services.ObjectVersionService;
//import java.util.logging.Logger;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.UUID;
//
///**
// * Service implementation for managing object versions.
// */
//@Service
//@RequiredArgsConstructor
//public class ObjectVersionServiceImpl implements ObjectVersionService {
//
//  private static final Logger LOGGER =
//          Logger.getLogger(ObjectVersionServiceImpl.class.getName());
//
//  private final ObjectVersionRepo versionRepository;
//  private final ObjectMetadataRepo objectRepository;
//  private final ObjectVersionMapper versionMapper;
//
//  @Transactional
//  @Override
//  public ObjectVersionDto createVersion(UUID objectId, String etag,
//                                          String storageLocation, long size,
//                                          String contentType, boolean isDeleteMarker,
//                                          String storageClass) {
//
//    LOGGER.info("Creating new version for object" + objectId);
//
//    ObjectMetadata object = objectRepository.findById(objectId)
//            .orElseThrow(() -> new IllegalArgumentException("Object not found: " + objectId));
//
//    ObjectVersion version = new ObjectVersion();
//    version.setObject(object);
//    version.setEtag(etag);
//    version.setStorageLocation(storageLocation);
//    version.setSize(size);
//    version.setContentType(contentType);
//    version.setCreatedAt(Instant.now());
//    version.setDeleteMarker(isDeleteMarker);
//    version.setStorageClass(storageClass);
//
//    ObjectVersion savedVersion = versionRepository.save(version);
//    LOGGER.warning("Created version with ID: " + savedVersion.getVersionId());
//
//    return versionMapper.toDto(savedVersion);
//  }
//
//  @Override
//  @Transactional(readOnly = true)
//  public ObjectVersionDto getVersion(UUID objectId, UUID versionId)
//            throws ObjectVersionNotFoundException {
//    LOGGER.warning("Fetching version " + versionId + " for object " + objectId);
//
//    ObjectVersion version = versionRepository.findById(versionId)
//            .orElseThrow(() -> {
//              LOGGER.warning("Version not found: " + versionId);
//              return new ObjectVersionNotFoundException("Version not found: " + versionId);
//            });
//
//    if (!version.getObject().getId().equals(objectId)) {
//      LOGGER.warning("Version " + versionId + " doesn't belong to object " + objectId);
//      throw new ObjectVersionNotFoundException("Version not found for specified object");
//    }
//
//    return versionMapper.toDto(version);
//  }
//
//  @Override
//  @Transactional(readOnly = true)
//  public List<ObjectVersionDto> getObjectVersions(UUID objectId) {
//    LOGGER.warning("Fetching all versions for object: " + objectId);
//
//    List<ObjectVersion> versions = versionRepository.findByObject_Id(objectId);
//    return versions.stream()
//            .map(versionMapper::toDto)
//            .toList();
//  }
//
//  @Override
//  @Transactional
//  public ObjectVersionDto updateVersion(UUID versionId, UUID objectId,
//                                          String etag, String storageLocation,
//                                          Long size, String contentType,
//                                          Boolean isDeleteMarker, String storageClass)
//            throws ObjectVersionNotFoundException {
//    LOGGER.info("Updating version: " + versionId);
//
//    ObjectVersion version = versionRepository.findById(versionId)
//            .orElseThrow(() -> {
//              LOGGER.warning("Version not found -> " + versionId);
//              return new ObjectVersionNotFoundException("Version not found: " + versionId);
//            });
//
//    if (!version.getObject().getId().equals(objectId)) {
//      LOGGER.warning("Version " + versionId + " doesn't belong to " + objectId);
//      throw new ObjectVersionNotFoundException("Version not found for specified object");
//    }
//
//    if (etag != null) {
//      version.setEtag(et
//    }
//    if (storageLocation != null) {
//      version.setStorageLocation(storageLocation);
//    }
//    if (size != null) {
//      version.setSize(size);
//    }
//    if (contentType != null) {
//      version.setContentType(contentType);
//    }
//    if (isDeleteMarker != null) {
//      version.setDeleteMarker(isDeleteMarker);
//    }
//    if (storageClass != null) {
//      version.setStorageClass(storageClass);
//    }
//
//    ObjectVersion updatedVersion = versionRepository.save(version);
//    LOGGER.warning("Version " + versionId + " updated successfully");
//
//    return versionMapper.toDto(updatedVersion);
//  }
//
//  @Override
//  @Transactional
//  public void deleteVersion(UUID objectId, UUID versionId)
//            throws ObjectVersionNotFoundException {
//      LOGGER.info("Deleting version " + versionId + " from object " + objectId);
//
//      ObjectVersion version = versionRepository.findById(versionId)
//              .orElseThrow(() -> {
//                  LOGGER.warning("Version not found: " + versionId);
//                  return new ObjectVersionNotFoundException("Version not found: " + versionId);
//              });
//
//      if (!version.getObject().getId().equals(objectId)) {
//          LOGGER.warning("Version " + versionId + " doesn't belong to object " + objectId);
//          throw new ObjectVersionNotFoundException("Version not found for specified object");
//      }
//
//      versionRepository.delete(version);
//      LOGGER.warning("Version " + versionId + " deleted successfully");
//  }
//}
