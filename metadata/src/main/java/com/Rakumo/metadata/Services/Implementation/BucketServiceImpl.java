package com.Rakumo.metadata.Services.Implementation;

import com.Rakumo.metadata.DTO.BucketDTO;
import com.Rakumo.metadata.Exceptions.BucketNotFoundException;
import com.Rakumo.metadata.Exceptions.ObjectDeletionException;
import com.Rakumo.metadata.Exceptions.UnauthorizedAccessException;
import com.Rakumo.metadata.Mapper.BucketMapper;
import com.Rakumo.metadata.Models.Bucket;
import com.Rakumo.metadata.Models.ObjectMetadata;
import com.Rakumo.metadata.Repository.BucketRepo;
import com.Rakumo.metadata.Repository.ObjectMetadataRepo;
import com.Rakumo.metadata.Services.BucketService;
import com.Rakumo.metadata.gRPC.ObjectGrpcClient;
import com.Rakumo.object.storage.DeleteObjectsInBucketResponse;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BucketServiceImpl implements BucketService {

    private final BucketRepo bucketRepository;
    private final BucketMapper bucketMapper;
    private final ObjectGrpcClient objectGrpcClient;
    private final ObjectMetadataRepo objectMetadataRepo;

    public BucketServiceImpl(BucketRepo bucketRepository,
                             BucketMapper bucketMapper, ObjectGrpcClient objectGrpcClient,
                             ObjectMetadataRepo objectMetadataRepo) {
        this.bucketRepository = bucketRepository;
        this.bucketMapper = bucketMapper;
        this.objectGrpcClient = objectGrpcClient;
        this.objectMetadataRepo = objectMetadataRepo;
    }

    @Override
    @Transactional
    public BucketDTO createBucket(UUID ownerId, String name,
                                  boolean versioningEnabled, String region) {
        log.info("Creating bucket for owner: {}", ownerId);

        Bucket bucket = new Bucket();
        bucket.setOwnerId(ownerId);
        bucket.setName(name);
        bucket.setVersioningEnabled(versioningEnabled);
        bucket.setRegion(region);
        bucket.setCreatedAt(Instant.now());

        Bucket savedBucket = bucketRepository.save(bucket);
        log.debug("Created bucket with ID: {}", savedBucket.getBucketId());

        return bucketMapper.toDto(savedBucket);
    }

    @Override
    @Transactional(readOnly = true)
    public BucketDTO getBucket(UUID ownerId, UUID bucketId)
            throws BucketNotFoundException, UnauthorizedAccessException {
        log.debug("Fetching bucket {} for owner {}", bucketId, ownerId);

        Bucket bucket = bucketRepository.findById(bucketId)
                .orElseThrow(() -> {
                    log.warn("Bucket not found: {}", bucketId);
                    return new BucketNotFoundException("Bucket not found: " + bucketId);
                });

        if (!bucket.getOwnerId().equals(ownerId)) {
            log.warn("Unauthorized access attempt by {} to bucket {}", ownerId, bucketId);
            throw new UnauthorizedAccessException("Access denied to bucket");
        }

        return bucketMapper.toDto(bucket);
    }

    @Override
    @Transactional
    public BucketDTO updateBucket(UUID bucketId, String name,
                                  Boolean versioningEnabled, String region)
            throws BucketNotFoundException {
        log.info("Updating bucket: {}", bucketId);

        Bucket bucket = bucketRepository.findById(bucketId)
                .orElseThrow(() -> {
                    log.warn("Update failed - bucket not found: {}", bucketId);
                    return new BucketNotFoundException("Bucket not found: " + bucketId);
                });

        if (name != null) bucket.setName(name);
        if (versioningEnabled != null) bucket.setVersioningEnabled(versioningEnabled);
        if (region != null) bucket.setRegion(region);
        bucket.setUpdatedAt(Instant.now());

        Bucket updatedBucket = bucketRepository.save(bucket);
        log.debug("Bucket {} updated successfully", bucketId);

        return bucketMapper.toDto(updatedBucket);
    }

    @Override
    public void deleteBucket(UUID ownerId, UUID bucketId)
            throws BucketNotFoundException, UnauthorizedAccessException, ObjectDeletionException {

        log.info("Deleting bucket {} for owner {}", bucketId, ownerId);

        // 1. Find bucket with validation
        Bucket bucket = bucketRepository.findById(bucketId)
                .orElseThrow(() -> {
                    log.warn("Delete failed - bucket not found: {}", bucketId);
                    return new BucketNotFoundException("Bucket not found: " + bucketId);
                });

        if (!bucket.getOwnerId().equals(ownerId)) {
            log.warn("Unauthorized delete attempt by {} to bucket {}", ownerId, bucketId);
            throw new UnauthorizedAccessException("Delete permission denied");
        }

        // 2. Get ALL objects in bucket with ONE query
        List<ObjectMetadata> objects = objectMetadataRepo.findByBucket_BucketId(bucketId);

        if (!objects.isEmpty()) {
            // 3. Prepare lists
            List<String> objectKeys = new ArrayList<>();
            List<String> fileIds = new ArrayList<>();

            for (ObjectMetadata obj : objects) {
                objectKeys.add(obj.getObjectKey());
                fileIds.add(obj.getId().toString());
            }

            // 4. Delete objects via gRPC and CHECK RESPONSE
            DeleteObjectsInBucketResponse response = objectGrpcClient.DeleteObjectsInBucket(
                    ownerId.toString(),
                    bucketId.toString(),
                    objectKeys,
                    fileIds
            );

            // 5. Validate all objects were deleted
            if (response.getDeletedCount() != objectKeys.size()) {
                String errorMsg = String.format(
                        "Failed to delete all objects. Deleted %d/%d. Failures: %s",
                        response.getDeletedCount(),
                        objectKeys.size(),
                        response.getFailedDeletionsList()
                );
                log.error(errorMsg);
                throw new ObjectDeletionException(errorMsg);
            }
        }

        // 6. Delete bucket (only if all objects deleted successfully)
        bucketRepository.delete(bucket);
        log.info("Bucket {} deleted successfully", bucketId);

        // Force flush
        bucketRepository.flush();
        log.info("Transaction committed successfully");
    }

    @Override
    public List<BucketDTO> GetUserBuckets(UUID ownerId) {
        log.info("Listing buckets for owner: {}", ownerId);
        List<Bucket> buckets = bucketRepository.findAllByOwnerId(ownerId);
        return buckets.stream()
                .map(bucketMapper::toDto)
                .collect(Collectors.toList());
    }
}