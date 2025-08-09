package com.Rakumo.object.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownloadRequest {

    @NotBlank
    private String bucketName;

    @NotBlank
    private String objectName;

    private String versionId;
    private Long byteRangeStart;
    private Long byteRangeEnd;
}
