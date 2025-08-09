package com.Rakumo.object.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteRequest {

    @NotBlank
    private String bucketName;

    @NotBlank
    private String objectKey;

    private String versionId;
}
