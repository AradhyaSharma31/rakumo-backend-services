package com.Rakumo.metadata.Services;

import com.Rakumo.metadata.DTO.ObjectMetadataDTO;
import com.Rakumo.metadata.Exceptions.ObjectNotFoundException;
import com.Rakumo.metadata.Exceptions.UnauthorizedAccessException;

import java.util.List;
import java.util.UUID;

public interface ObjectMetadataService {

    ObjectMetadataDTO createObjectMetadata(UUID bucketId, String objectKey,
                                           String latestVersionId, String latestEtag, long latestSize);

    ObjectMetadataDTO getObject(UUID bucketId, UUID objectId)
        throws ObjectNotFoundException, UnauthorizedAccessException;

    List<ObjectMetadataDTO> getBucketObject(UUID bucketId);

    ObjectMetadataDTO updateObject(UUID objectId, UUID bucketId,
                                   String latestVersionId, String latestEtag,
                                   Long latestSize, Boolean isDeleted);

    void deleteObject(UUID bucketId, UUID objectId)
        throws ObjectNotFoundException, UnauthorizedAccessException;
}
