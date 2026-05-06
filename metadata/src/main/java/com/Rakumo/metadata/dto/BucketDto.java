package com.Rakumo.metadata.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BucketDTO {

    private UUID bucketId;
    private UUID ownerId;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean versioningEnabled;
    private String region;

}
