package com.Rakumo.metadata.Services;

import com.Rakumo.metadata.DTO.CustomMetadataDTO;
import com.Rakumo.metadata.Exceptions.ObjectVersionNotFoundException;

import java.util.List;
import java.util.UUID;

public interface CustomMetadataService {
    List<CustomMetadataDTO> addMetadata(UUID versionId, String key, String value)
            throws ObjectVersionNotFoundException;

    List<CustomMetadataDTO> getMetadata(UUID versionId)
            throws ObjectVersionNotFoundException;

    List<CustomMetadataDTO> removeMetadata(UUID versionId, String key)
            throws ObjectVersionNotFoundException;
}