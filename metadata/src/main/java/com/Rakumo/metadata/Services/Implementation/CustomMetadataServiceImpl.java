package com.Rakumo.metadata.Services.Implementation;

import com.Rakumo.metadata.DTO.CustomMetadataDTO;
import com.Rakumo.metadata.Exceptions.ObjectVersionNotFoundException;
import com.Rakumo.metadata.Mapper.CustomMetadataMapper;
import com.Rakumo.metadata.Models.CustomMetadata;
import com.Rakumo.metadata.Models.ObjectVersion;
import com.Rakumo.metadata.Repository.CustomMetadataRepo;
import com.Rakumo.metadata.Repository.ObjectVersionRepo;
import com.Rakumo.metadata.Services.CustomMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomMetadataServiceImpl implements CustomMetadataService {

    private final CustomMetadataRepo metadataRepository;
    private final ObjectVersionRepo versionRepository;
    private final CustomMetadataMapper metadataMapper;

    @Override
    @Transactional
    public List<CustomMetadataDTO> addMetadata(UUID versionId, String key, String value)
            throws ObjectVersionNotFoundException {
        log.info("Adding metadata to version: {}", versionId);

        ObjectVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ObjectVersionNotFoundException("Version not found"));

        CustomMetadata metadata = new CustomMetadata();
        metadata.setKey(key);
        metadata.setValue(value);
        metadata.setObjectVersion(version);

        metadataRepository.save(metadata);
        log.debug("Added metadata {}={} to version {}", key, value, versionId);

        return version.getCustomMetadata().stream()
                .map(metadataMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomMetadataDTO> getMetadata(UUID versionId)
            throws ObjectVersionNotFoundException {
        log.debug("Fetching metadata for version: {}", versionId);

        ObjectVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ObjectVersionNotFoundException("Version not found"));

        return version.getCustomMetadata().stream()
                .map(metadataMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<CustomMetadataDTO> removeMetadata(UUID versionId, String key)
            throws ObjectVersionNotFoundException {
        log.info("Removing metadata key {} from version: {}", key, versionId);

        ObjectVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ObjectVersionNotFoundException("Version not found"));

        metadataRepository.deleteByObjectVersionAndKey(version, key);
        log.debug("Removed metadata key {} from version {}", key, versionId);

        return version.getCustomMetadata().stream()
                .map(metadataMapper::toDto)
                .collect(Collectors.toList());
    }
}