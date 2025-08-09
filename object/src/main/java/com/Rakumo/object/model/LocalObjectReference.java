package com.Rakumo.object.model;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class LocalObjectReference {

    private String bucketName;
    private String objectKey;
    private String versionId;
    private String checksum; // SHA-256
    private Path physicalPath;

    public Path computePhysicalPath(Path storageRoot) {
        return storageRoot.resolve(
                bucketName,
                objectKey,
                versionId != null ? versionId : "latest"
        );
    }
}
