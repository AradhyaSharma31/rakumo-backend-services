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

package com.rakumo.metadata.repository;

import com.rakumo.metadata.entity.ObjectMetadata;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ObjectMetadata entity, providing CRUD operations and custom queries.
 */
@Repository
public interface ObjectMetadataRepo extends JpaRepository<ObjectMetadata, UUID>  {

  /**
   * Finds a list of ObjectMetadata entities by the associated bucket ID.
   *
   * @param bucketId the UUID of the bucket to search for
   * @return a list of ObjectMetadata entities associated with the specified bucket ID
   */
  List<ObjectMetadata> findByBucketBucketId(UUID bucketId);
}
