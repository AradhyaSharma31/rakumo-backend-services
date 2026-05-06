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

import com.rakumo.metadata.dto.ObjectMetadataDto;
import com.rakumo.metadata.entity.ObjectMetadata;
import com.rakumo.metadata.exceptions.ObjectNotFoundException;
import com.rakumo.metadata.exceptions.UnauthorizedAccessException;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing the lifecycle and retrieval of object metadata.
 */
public interface ObjectMetadataService {

  /**
   * Creates a new metadata record for a storage object.
   *
   * @param objectMetadata the Entity containing the metadata information to create
   * @param objectId the unique identifier for the new object metadata record
   * @param bucketId the unique identifier of the bucket to which the object belongs
   * @return the created object metadata DTO
   */
  ObjectMetadataDto createObjectMetadata(UUID objectId, UUID bucketId, ObjectMetadata objectMetadata);

  /**
   * Retrieves the metadata for a specific object.
   *
   * @param bucketId the identifier of the bucket containing the object
   * @param objectId the unique identifier of the object
   * @return the metadata DTO for the requested object
   * @throws ObjectNotFoundException if the object does not exist
   * @throws UnauthorizedAccessException if the user does not have permission to access the object
   */
  ObjectMetadataDto getObject(UUID bucketId, UUID objectId)
      throws ObjectNotFoundException, UnauthorizedAccessException;

  /**
   * Retrieves all object metadata entries associated with a specific bucket.
   *
   * @param bucketId the unique identifier of the bucket
   * @return a list of object metadata DTOs found in the bucket
   */
  List<ObjectMetadataDto> getBucketObject(UUID bucketId);

  /**
   * Updates the metadata for an existing object to reflect a new version or state change.
   *
   * @param objectMetadata the Entity containing the updated metadata information
   * @param objectId the unique identifier of the object metadata record to update
   * @param bucketId the unique identifier of the bucket to which the object belongs
   * @return the updated object metadata DTO
   */
  ObjectMetadataDto updateObject(UUID objectId, UUID bucketId, ObjectMetadata objectMetadata);

  /**
   * Deletes the metadata record for a specific object.
   *
   * @param bucketId the identifier of the bucket containing the object
   * @param objectId the unique identifier of the object to delete
   * @throws ObjectNotFoundException if the object does not exist
   * @throws UnauthorizedAccessException if the user does not have permission to delete the object
   */
  void deleteObject(UUID bucketId, UUID objectId)
      throws ObjectNotFoundException, UnauthorizedAccessException;
}
