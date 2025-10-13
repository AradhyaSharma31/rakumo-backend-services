// File: src/main/java/com/Rakumo/object/repository/MultipartUploadRepository.java
package com.Rakumo.object.repository;

import com.Rakumo.object.entity.MultipartUploadEntity;
import com.Rakumo.object.enumeration.UploadStatus;
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