package com.Rakumo.metadata.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectVersionDTO {

    private UUID versionId;
    private UUID objectId;
    private String etag;
    private String storageLocation;
    private long size;
    private String contentType;
    private Instant createdAt;
    private boolean isDeleteMarker;
    private String storageClass;
    private List<CustomMetadataDTO> customMetadata;

}
