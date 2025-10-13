package com.Rakumo.object.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadRequest {
    @NotBlank
    private String bucketName;

    @NotBlank
    private String objectKey;

    private String ownerId;
    private String contentType;
}