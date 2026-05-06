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

// File: src/main/java/com/Rakumo/object/repository/MultipartUploadRepository.java
package com.rakumo.object.repository;

import com.rakumo.object.entity.MultipartUploadEntity;
import com.rakumo.object.enumeration.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MultipartUploadRepository extends JpaRepository<MultipartUploadEntity, String> {

    List<MultipartUploadEntity> findByUserId(String userId);

    List<MultipartUploadEntity> findByStatusAndExpiresAtBefore(String status, Instant expiresAt);

    List<MultipartUploadEntity> findByBucketNameAndObjectKey(String bucketName, String objectKey);

    List<MultipartUploadEntity> findByStatusAndCreatedAtBefore(UploadStatus uploadStatus, Instant cutoff);
}
