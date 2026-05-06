package com.Rakumo.metadata.Repository;

import com.Rakumo.metadata.Models.ObjectMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ObjectMetadataRepo extends JpaRepository<ObjectMetadata, UUID>  {

    // @Query("SELECT o FROM ObjectMetadata o WHERE o.bucket.bucketId = :bucketId AND o.isDeleted = false")
    List<ObjectMetadata> findByBucket_BucketId(UUID bucketId);

}
