package com.Rakumo.metadata.Services.Implementation;

import com.Rakumo.metadata.DTO.BucketDTO;
import com.Rakumo.metadata.Exceptions.BucketNotFoundException;
import com.Rakumo.metadata.Exceptions.UnauthorizedAccessException;
import com.Rakumo.metadata.Mapper.BucketMapper;
import com.Rakumo.metadata.Models.Bucket;
import com.Rakumo.metadata.Repository.BucketRepo;
import com.Rakumo.metadata.Services.BucketService;
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

    public BucketServiceImpl(BucketRepo bucketRepository,
                             BucketMapper bucketMapper) {
        this.bucketRepository = bucketRepository;
        this.bucketMapper = bucketMapper;
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
    @Transactional
    public void deleteBucket(UUID ownerId, UUID bucketId)
            throws BucketNotFoundException, UnauthorizedAccessException {
        log.info("Deleting bucket {} for owner {}", bucketId, ownerId);

        Bucket bucket = bucketRepository.findById(bucketId)
                .orElseThrow(() -> {
                    log.warn("Delete failed - bucket not found: {}", bucketId);
                    return new BucketNotFoundException("Bucket not found: " + bucketId);
                });

        if (!bucket.getOwnerId().equals(ownerId)) {
            log.warn("Unauthorized delete attempt by {} to bucket {}", ownerId, bucketId);
            throw new UnauthorizedAccessException("Delete permission denied");
        }

        bucketRepository.delete(bucket);
        log.debug("Bucket {} deleted successfully", bucketId);
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