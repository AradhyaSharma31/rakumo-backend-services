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

import com.rakumo.metadata.dto.CustomMetadataDto;
import com.rakumo.metadata.exceptions.ObjectVersionNotFoundException;
import com.rakumo.metadata.mapper.CustomMetadataMapper;
import com.rakumo.metadata.entity.CustomMetadata;
import com.rakumo.metadata.entity.ObjectVersion;
import com.rakumo.metadata.repository.CustomMetadataRepo;
import com.rakumo.metadata.repository.ObjectVersionRepo;
import com.rakumo.metadata.services.CustomMetadataService;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for managing custom metadata associated with object versions.
 */
@Service
@RequiredArgsConstructor
public class CustomMetadataServiceImpl implements CustomMetadataService {

  private static final Logger LOGGER =
          Logger.getLogger(CustomMetadataServiceImpl.class.getName());

  private final CustomMetadataRepo metadataRepository;
  private final ObjectVersionRepo versionRepository;
  private final CustomMetadataMapper metadataMapper;

  @Override
  @Transactional
  public List<CustomMetadataDto> addMetadata(UUID versionId, String key, String value)
            throws ObjectVersionNotFoundException {
    LOGGER.info("Adding metadata to version: " + versionId);

    ObjectVersion version = versionRepository.findById(versionId)
            .orElseThrow(() -> new ObjectVersionNotFoundException("Version not found"));

    CustomMetadata metadata = new CustomMetadata();
    metadata.setKey(key);
    metadata.setValue(value);
    metadata.setObjectVersion(version);

    metadataRepository.save(metadata);
    LOGGER.warning("Added metadata " + key + "=" + value + " to version " + versionId);

    return version.getCustomMetadata().stream()
            .map(metadataMapper::toDto)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<CustomMetadataDto> getMetadata(UUID versionId)
            throws ObjectVersionNotFoundException {
    LOGGER.warning("Fetching metadata for version: " + versionId);

    ObjectVersion version = versionRepository.findById(versionId)
            .orElseThrow(() -> new ObjectVersionNotFoundException("Version not found"));

    return version.getCustomMetadata().stream()
            .map(metadataMapper::toDto)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public List<CustomMetadataDto> removeMetadata(UUID versionId, String key)
            throws ObjectVersionNotFoundException {
    LOGGER.info("Removing metadata key " + key + " from version: " + versionId);

    ObjectVersion version = versionRepository.findById(versionId)
            .orElseThrow(() -> new ObjectVersionNotFoundException("Version not found"));

    metadataRepository.deleteByObjectVersionAndKey(version, key);
    LOGGER.warning("Removed metadata key " + key + " from version " + versionId);

    return version.getCustomMetadata().stream()
            .map(metadataMapper::toDto)
            .collect(Collectors.toList());
  }
}
