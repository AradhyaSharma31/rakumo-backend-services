package com.Rakumo.metadata.Services;

import com.Rakumo.metadata.DTO.ObjectVersionDTO;
import com.Rakumo.metadata.Exceptions.ObjectNotFoundException;
import com.Rakumo.metadata.Exceptions.ObjectVersionNotFoundException;

import java.util.List;
import java.util.UUID;

public interface ObjectVersionService {
    ObjectVersionDTO createVersion(UUID objectId, String etag,
                                   String storageLocation, long size,
                                   String contentType, boolean isDeleteMarker,
                                   String storageClass);

    ObjectVersionDTO getVersion(UUID objectId, UUID versionId)
            throws ObjectVersionNotFoundException;

    List<ObjectVersionDTO> getObjectVersions(UUID objectId);

    ObjectVersionDTO updateVersion(UUID versionId, UUID objectId,
                                   String etag, String storageLocation,
                                   Long size, String contentType,
                                   Boolean isDeleteMarker, String storageClass)
            throws ObjectVersionNotFoundException;

    void deleteVersion(UUID objectId, UUID versionId)
            throws ObjectVersionNotFoundException;
}
