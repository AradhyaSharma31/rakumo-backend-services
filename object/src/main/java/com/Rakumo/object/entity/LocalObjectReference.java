// LocalObjectReference.java (Fixed)
package com.Rakumo.object.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalObjectReference {
    private String ownerId;
    private String bucketName;
    private String objectKey;
    private String versionId;
    private String checksum;
    private long size;
    private String physicalPath;
}
