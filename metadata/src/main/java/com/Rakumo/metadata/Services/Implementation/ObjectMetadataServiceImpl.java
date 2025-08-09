package com.Rakumo.metadata.Services.Implementation;

import com.Rakumo.metadata.DTO.ObjectMetadataDTO;
import com.Rakumo.metadata.Exceptions.ObjectNotFoundException;
import com.Rakumo.metadata.Exceptions.UnauthorizedAccessException;
import com.Rakumo.metadata.Mapper.ObjectMetadataMapper;
import com.Rakumo.metadata.Models.Bucket;
import com.Rakumo.metadata.Models.ObjectMetadata;
import com.Rakumo.metadata.Repository.BucketRepo;
import com.Rakumo.metadata.Repository.ObjectMetadataRepo;
import com.Rakumo.metadata.Services.ObjectMetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ObjectMetadataServiceImpl implements ObjectMetadataService {

    private final ObjectMetadataRepo objectMetadataRepo;
    private final BucketRepo bucketRepo;
    private final ObjectMetadataMapper objectMapper;

    public ObjectMetadataServiceImpl(ObjectMetadataRepo objectMetadataRepo,
                                     BucketRepo bucketRepo,
                                     ObjectMetadataMapper objectMapper) {
        this.objectMetadataRepo = objectMetadataRepo;
        this.bucketRepo = bucketRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ObjectMetadataDTO createObjectMetadata(UUID bucketId, String objectKey, String latestVersionId, String latestEtag, long latestSize) {
        log.info("Creating object in bucket: {}", bucketId);

        Bucket bucket = bucketRepo.findById(bucketId)
                .orElseThrow(() -> new IllegalArgumentException("Bucket not found: " + bucketId));

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setBucket(bucket);
        objectMetadata.setObjectKey(objectKey);
        objectMetadata.setLatestVersionId(latestVersionId);
        objectMetadata.setLatestSize(latestSize);
        objectMetadata.setLatestEtag(latestEtag);
        objectMetadata.setCreatedAt(Instant.now());
        objectMetadata.setUpdatedAt(Instant.now());
        objectMetadata.setDeleted(false);

        ObjectMetadata savedObject = objectMetadataRepo.save(objectMetadata);
        log.debug("Created object with ID: {}", savedObject.getId());

        return objectMapper.toDto(savedObject);
    }

    @Override
    @Transactional(readOnly = true)
    public ObjectMetadataDTO getObject(UUID bucketId, UUID objectId) throws ObjectNotFoundException, UnauthorizedAccessException {
        log.debug("Fetching object {} from bucket {}", objectId, bucketId);

        ObjectMetadata object = objectMetadataRepo.findById(objectId)
                .orElseThrow(() -> {
                    log.warn("Object not found: {}", objectId);
                    return new ObjectNotFoundException("Object not found: " + objectId);
                });

//        if (!object.getBucket().getBucketId().equals(bucketId)) {
//            log.warn("Unauthorized access attempt to object {} from bucket {}", objectId, bucketId);
//            throw new UnauthorizedAccessException("Access denied to object");
//        }

        ObjectMetadataDTO dto = objectMapper.toDto(object);
        dto.setBucketId(bucketId);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObjectMetadataDTO> getBucketObject(UUID bucketId) {
        log.debug("Fetching all objects for bucket: {}", bucketId);

        List<ObjectMetadata> objects = objectMetadataRepo.findByBucket_BucketId(bucketId);

        return objects.stream()
                .map(objectMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ObjectMetadataDTO updateObject(UUID objectId, UUID bucketId, String latestVersionId, String latestEtag, Long latestSize, Boolean isDeleted) {
        log.info("Updating object: {}", objectId);

        ObjectMetadata object = objectMetadataRepo.findById(objectId)
                .orElseThrow(() -> {
                    log.warn("Update failed - object not found: {}", objectId);
                    return new ObjectNotFoundException("Object not found: " + objectId);
                });

        if (!object.getBucket().getBucketId().equals(bucketId)) {
            log.warn("Update failed - object not found: {}", objectId);
            throw new ObjectNotFoundException("Object not found: " + objectId);
        }

        if (latestVersionId != null) object.setLatestVersionId(latestVersionId);
        if (latestEtag != null) object.setLatestEtag(latestEtag);
        if (latestSize != null) object.setLatestSize(latestSize);
        if (isDeleted != null) object.setDeleted(isDeleted);
        object.setUpdatedAt(Instant.now());

        ObjectMetadata updatedObject = objectMetadataRepo.save(object);
        log.debug("Object {} updated successfully", objectId);

        return objectMapper.toDto(updatedObject);
    }

    @Override
    @Transactional
    public void deleteObject(UUID bucketId, UUID objectId) throws ObjectNotFoundException, UnauthorizedAccessException {
        log.info("Deleting object {} form bucket {}", objectId, bucketId);

        ObjectMetadata objectMetadata = objectMetadataRepo.findById(objectId)
                .orElseThrow(() -> {
                    log.warn("Delete failed - object not found: {}", objectId);
                    return new ObjectNotFoundException("Object not found: " + objectId);
                });

        if (!objectMetadata.getBucket().getBucketId().equals(bucketId)) {
            log.warn("Unauthorized delete attempt to object {} from bucket {}", objectId, bucketId);
            throw new UnauthorizedAccessException("Delete permission denied");
        }

        objectMetadataRepo.delete(objectMetadata);
        log.debug("Object {} deleted successfully", objectId);
    }
}
