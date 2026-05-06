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

// RegularObjectRepository.java
package com.rakumo.object.repository;

import com.rakumo.object.entity.RegularObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegularObjectRepository extends JpaRepository<RegularObjectEntity, UUID> {

    @Query("SELECT r FROM RegularObjectEntity r WHERE r.bucketName = :bucketName " +
            "AND r.objectKey = :objectKey AND r.versionId = :versionId")
    Optional<RegularObjectEntity> findByBucketAndKeyAndVersion(
            @Param("bucketName") String bucketName,
            @Param("objectKey") String objectKey,
            @Param("versionId") String versionId);

    @Query("SELECT r.checksum FROM RegularObjectEntity r WHERE r.id = :id")
    String getChecksumById(UUID id);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM RegularObjectEntity r " +
            "WHERE r.bucketName = :bucketId AND r.checksum = :checksum")
    boolean existsByChecksumAndBucketId(
            @Param("checksum") String checksum,
            @Param("bucketId") String bucketId
    );
}
