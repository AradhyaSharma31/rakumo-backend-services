package com.Rakumo.metadata.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectMetadataDTO {

    private UUID Id;
    private UUID bucketId;
    private String objectKey;
    private String latestVersionId;
    private String latestEtag;
    private Long latestSize;
    private Instant updatedAt;
    private Instant createdAt;
    private boolean isDeleted;

}
