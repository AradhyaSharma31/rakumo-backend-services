package com.Rakumo.metadata.Repository;

import com.Rakumo.metadata.Models.CustomMetadata;
import com.Rakumo.metadata.Models.ObjectVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomMetadataRepo extends JpaRepository<CustomMetadata, UUID> {

    void deleteByObjectVersionAndKey(ObjectVersion version, String key);

}
