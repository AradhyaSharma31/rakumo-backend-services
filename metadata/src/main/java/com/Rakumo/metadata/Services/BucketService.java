package com.Rakumo.metadata.Services;

import com.Rakumo.metadata.DTO.BucketDTO;
import com.Rakumo.metadata.Exceptions.BucketNotFoundException;

import java.util.List;
import java.util.UUID;

public interface BucketService {
    // List<BucketDTO> getUserBuckets(UUID ownerId);

    BucketDTO createBucket(UUID ownerId, String name,
                               boolean versioningEnabled, String region);

    BucketDTO getBucket(UUID ownerId, UUID bucketId)
                throws BucketNotFoundException;

    BucketDTO updateBucket(UUID bucketId, String name,
                               Boolean versioningEnabled, String region)
                throws BucketNotFoundException;

    void deleteBucket(UUID bucketId, UUID ownerId)
                throws BucketNotFoundException;

    List<BucketDTO> GetUserBuckets(UUID ownerId);
}