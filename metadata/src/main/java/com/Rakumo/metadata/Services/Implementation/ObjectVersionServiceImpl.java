package com.Rakumo.metadata.Services.Implementation;

import com.Rakumo.metadata.DTO.ObjectVersionDTO;
import com.Rakumo.metadata.Exceptions.ObjectVersionNotFoundException;
import com.Rakumo.metadata.Mapper.ObjectVersionMapper;
import com.Rakumo.metadata.Models.ObjectMetadata;
import com.Rakumo.metadata.Models.ObjectVersion;
import com.Rakumo.metadata.Repository.ObjectMetadataRepo;
import com.Rakumo.metadata.Repository.ObjectVersionRepo;
import com.Rakumo.metadata.Services.ObjectVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectVersionServiceImpl implements ObjectVersionService {

    private final ObjectVersionRepo versionRepository;
    private final ObjectMetadataRepo objectRepository;
    private final ObjectVersionMapper versionMapper;

    @Transactional
    @Override
    public ObjectVersionDTO createVersion(UUID objectId, String etag,
                                          String storageLocation, long size,
                                          String contentType, boolean isDeleteMarker,
                                          String storageClass) {
        log.info("Creating new version for object: {}", objectId);

        ObjectMetadata object = objectRepository.findById(objectId)
                .orElseThrow(() -> new IllegalArgumentException("Object not found: " + objectId));

        ObjectVersion version = new ObjectVersion();
        version.setObject(object);
        version.setEtag(etag);
        version.setStorageLocation(storageLocation);
        version.setSize(size);
        version.setContentType(contentType);
        version.setCreatedAt(Instant.now());
        version.setDeleteMarker(isDeleteMarker);
        version.setStorageClass(storageClass);

        ObjectVersion savedVersion = versionRepository.save(version);
        log.debug("Created version with ID: {}", savedVersion.getVersionId());

        return versionMapper.toDto(savedVersion);
    }

    @Override
    @Transactional(readOnly = true)
    public ObjectVersionDTO getVersion(UUID objectId, UUID versionId)
            throws ObjectVersionNotFoundException {
        log.debug("Fetching version {} for object {}", versionId, objectId);

        ObjectVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> {
                    log.warn("Version not found: {}", versionId);
                    return new ObjectVersionNotFoundException("Version not found: " + versionId);
                });

        if (!version.getObject().getId().equals(objectId)) {
            log.warn("Version {} doesn't belong to object {}", versionId, objectId);
            throw new ObjectVersionNotFoundException("Version not found for specified object");
        }

        return versionMapper.toDto(version);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObjectVersionDTO> getObjectVersions(UUID objectId) {
        log.debug("Fetching all versions for object: {}", objectId);

        List<ObjectVersion> versions = versionRepository.findByObject_Id(objectId);
        return versions.stream()
                .map(versionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ObjectVersionDTO updateVersion(UUID versionId, UUID objectId,
                                          String etag, String storageLocation,
                                          Long size, String contentType,
                                          Boolean isDeleteMarker, String storageClass)
            throws ObjectVersionNotFoundException {
        log.info("Updating version: {}", versionId);

        ObjectVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> {
                    log.warn("Version not found -> {}", versionId);
                    return new ObjectVersionNotFoundException("Version not found: " + versionId);
                });

        if (!version.getObject().getId().equals(objectId)) {
            log.warn("Version {} doesn't belong to {}", versionId, objectId);
            throw new ObjectVersionNotFoundException("Version not found for specified object");
        }

        if (etag != null) version.setEtag(etag);
        if (storageLocation != null) version.setStorageLocation(storageLocation);
        if (size != null) version.setSize(size);
        if (contentType != null) version.setContentType(contentType);
        if (isDeleteMarker != null) version.setDeleteMarker(isDeleteMarker);
        if (storageClass != null) version.setStorageClass(storageClass);

        ObjectVersion updatedVersion = versionRepository.save(version);
        log.debug("Version {} updated successfully", versionId);

        return versionMapper.toDto(updatedVersion);
    }

    @Override
    @Transactional
    public void deleteVersion(UUID objectId, UUID versionId)
            throws ObjectVersionNotFoundException {
        log.info("Deleting version {} from object {}", versionId, objectId);

        ObjectVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> {
                    log.warn("Version not found: {}", versionId);
                    return new ObjectVersionNotFoundException("Version not found: " + versionId);
                });

        if (!version.getObject().getId().equals(objectId)) {
            log.warn("Version {} doesn't belong to object {}", versionId, objectId);
            throw new ObjectVersionNotFoundException("Version not found for specified object");
        }

        versionRepository.delete(version);
        log.debug("Version {} deleted successfully", versionId);
    }

}
