// RegularObjectRepository.java
package com.Rakumo.object.repository;

import com.Rakumo.object.entity.RegularObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    Optional<RegularObjectEntity> findByBucketNameAndObjectKey(String bucketName, String objectKey);
}
