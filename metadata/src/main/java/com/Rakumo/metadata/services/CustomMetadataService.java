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

import com.rakumo.metadata.dto.CustomMetadataDto;
import com.rakumo.metadata.exceptions.ObjectVersionNotFoundException;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing custom key-value metadata associated with object versions.
 */
public interface CustomMetadataService {

  /**
   * Adds a new metadata key-value pair to the specified object version.
   *
   * @param versionId the unique identifier of the object version
   * @param key the key for the custom metadata entry
   * @param value the value associated with the metadata key
   * @return the updated list of custom metadata for the object version
   * @throws ObjectVersionNotFoundException if the specified version ID does not exist
   */
  List<CustomMetadataDto> addMetadata(UUID versionId, String key, String value)
      throws ObjectVersionNotFoundException;

  /**
   * Retrieves all custom metadata associated with the specified object version.
   *
   * @param versionId the unique identifier of the object version
   * @return a list of custom metadata DTOs associated with the version
   * @throws ObjectVersionNotFoundException if the specified version ID does not exist
   */
  List<CustomMetadataDto> getMetadata(UUID versionId)
      throws ObjectVersionNotFoundException;

  /**
   * Removes a specific metadata entry from the specified object version.
   *
   * @param versionId the unique identifier of the object version
   * @param key the key of the metadata entry to remove
   * @return the updated list of custom metadata for the object version
   * @throws ObjectVersionNotFoundException if the specified version ID does not exist
   */
  List<CustomMetadataDto> removeMetadata(UUID versionId, String key)
      throws ObjectVersionNotFoundException;
}
