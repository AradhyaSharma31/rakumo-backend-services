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

package com.rakumo.metadata.services.impl;

import com.rakumo.metadata.dto.ObjectVersionDto;
import com.rakumo.metadata.entity.ObjectMetadata;
import com.rakumo.metadata.entity.ObjectVersion;
import com.rakumo.metadata.exceptions.ObjectVersionNotFoundException;
import com.rakumo.metadata.mapper.ObjectVersionMapper;
import com.rakumo.metadata.repository.ObjectMetadataRepo;
import com.rakumo.metadata.repository.ObjectVersionRepo;
import com.rakumo.metadata.services.ObjectVersionService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing object versions.
 */
@Service
@RequiredArgsConstructor
public class ObjectVersionServiceImpl implements ObjectVersionService {

  private static final Logger LOGGER =
          Logger.getLogger(ObjectVersionServiceImpl.class.getName());

  private final ObjectVersionRepo versionRepository;
  private final ObjectMetadataRepo objectRepository;
  private final ObjectVersionMapper versionMapper;

  @Transactional
  @Override
  public ObjectVersionDto createVersion(ObjectVersion objectVersion) {

    LOGGER.info("Creating new version for object" + objectVersion.getObject().getId());

    ObjectMetadata object = objectRepository.findById(objectVersion.getObject().getId())
            .orElseThrow(() -> new IllegalArgumentException("Object not found: " + objectVersion.getObject().getId()));

    ObjectVersion version = new ObjectVersion();
    version.setObject(objectVersion.getObject());
    version.setEtag(objectVersion.getEtag());
    version.setStorageLocation(objectVersion.getStorageLocation());
    version.setSize(objectVersion.getSize());
    version.setContentType(objectVersion.getContentType());
    version.setCreatedAt(Instant.now());
    version.setIsDeleteMarker(objectVersion.getIsDeleteMarker());
    version.setStorageClass(objectVersion.getStorageClass());

    ObjectVersion savedVersion = versionRepository.save(version);
    LOGGER.warning("Created version with ID: " + savedVersion.getVersionId());

    return versionMapper.toDto(savedVersion);
  }

  @Override
  @Transactional(readOnly = true)
  public ObjectVersionDto getVersion(UUID objectId, UUID versionId)
            throws ObjectVersionNotFoundException {
    LOGGER.warning("Fetching version " + versionId + " for object " + objectId);

    ObjectVersion version = versionRepository.findById(versionId)
            .orElseThrow(() -> {
              LOGGER.warning("Version not found: " + versionId);
              return new ObjectVersionNotFoundException("Version not found: " + versionId);
            });

    if (!version.getObject().getId().equals(objectId)) {
      LOGGER.warning("Version " + versionId + " doesn't belong to object " + objectId);
      throw new ObjectVersionNotFoundException("Version not found for specified object");
    }

    return versionMapper.toDto(version);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ObjectVersionDto> getObjectVersions(UUID objectId) {
    LOGGER.warning("Fetching all versions for object: " + objectId);

    List<ObjectVersion> versions = versionRepository.findByObjectId(objectId);
    return versions.stream()
            .map(versionMapper::toDto)
            .toList();
  }

  @Override
  @Transactional
  public ObjectVersionDto updateVersion(ObjectVersion objectVersion)
            throws ObjectVersionNotFoundException {
    LOGGER.info("Updating version: " + objectVersion.getVersionId());

    ObjectVersion version = versionRepository.findById(objectVersion.getVersionId())
            .orElseThrow(() -> {
              LOGGER.warning("Version not found -> " + objectVersion.getVersionId());
              return new ObjectVersionNotFoundException("Version not found: " + objectVersion.getVersionId());
            });

    if (!version.getObject().getId().equals(objectVersion.getObject().getId())) {
      LOGGER.warning("Version " + objectVersion.getVersionId() + " doesn't belong to " + objectVersion.getObject().getId());
      throw new ObjectVersionNotFoundException("Version not found for specified object");
    }

    if (objectVersion.getEtag() != null) {
      version.setEtag(objectVersion.getEtag());
    }
    if (objectVersion.getStorageLocation() != null) {
      version.setStorageLocation(objectVersion.getStorageLocation());
    }
    if (objectVersion.getSize() != null) {
      version.setSize(objectVersion.getSize());
    }
    if (objectVersion.getContentType() != null) {
      version.setContentType(objectVersion.getContentType());
    }
    if (objectVersion.getIsDeleteMarker() != null) {
      version.setIsDeleteMarker(objectVersion.getIsDeleteMarker());
    }
    if (objectVersion.getStorageClass() != null) {
      version.setStorageClass(objectVersion.getStorageClass());
    }

    ObjectVersion updatedVersion = versionRepository.save(version);
    LOGGER.warning("Version " + objectVersion.getVersionId() + " updated successfully");

    return versionMapper.toDto(updatedVersion);
  }

  @Override
  @Transactional
  public void deleteVersion(UUID objectId, UUID versionId)
            throws ObjectVersionNotFoundException {
    LOGGER.info("Deleting version " + versionId + " from object " + objectId);

    ObjectVersion version = versionRepository.findById(versionId)
              .orElseThrow(() -> {
                LOGGER.warning("Version not found: " + versionId);
                return new ObjectVersionNotFoundException("Version not found: " + versionId);
              });

    if (!version.getObject().getId().equals(objectId)) {
      LOGGER.warning("Version " + versionId + " doesn't belong to object " + objectId);
      throw new ObjectVersionNotFoundException("Version not found for specified object");
    }

    versionRepository.delete(version);
    LOGGER.warning("Version " + versionId + " deleted successfully");
  }
}
