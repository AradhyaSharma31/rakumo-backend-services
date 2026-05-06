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

import com.rakumo.metadata.dto.ObjectVersionDto;
import com.rakumo.metadata.entity.ObjectVersion;
import com.rakumo.metadata.exceptions.ObjectVersionNotFoundException;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing object versions.
 */
public interface ObjectVersionService {

  /**
   * Creates a new version for the specified object.
   *
   * @param objectVersion the Entity containing the version information to create.
   * @return The created ObjectVersionDto.
   */
  ObjectVersionDto createVersion(ObjectVersion objectVersion);

  /**
   * Retrieves a specific version of an object.
   *
   * @param objectId The ID of the object.
   * @param versionId The ID of the version to retrieve.
   * @return The ObjectVersionDto for the specified version.
   * @throws ObjectVersionNotFoundException If the version is not found.
   */
  ObjectVersionDto getVersion(UUID objectId, UUID versionId)
          throws ObjectVersionNotFoundException;

  /**
   * Retrieves all versions of a specific object.
   *
   * @param objectId The ID of the object.
   * @return A list of ObjectVersionDto representing all versions of the object.
   */
  List<ObjectVersionDto> getObjectVersions(UUID objectId);

  /**
   * Updates the metadata of a specific version of an object.
   *
   * @param objectVersion the Entity containing the version information to create.
   * @return The updated ObjectVersionDto.
   * @throws ObjectVersionNotFoundException If the version is not found.
   */
  ObjectVersionDto updateVersion(ObjectVersion objectVersion)
          throws ObjectVersionNotFoundException;

  /**
   * Deletes a specific version of an object.
   *
   * @param objectId The ID of the object.
   * @param versionId The ID of the version to delete.
   * @throws ObjectVersionNotFoundException If the version is not found.
   */
  void deleteVersion(UUID objectId, UUID versionId)
          throws ObjectVersionNotFoundException;
}
