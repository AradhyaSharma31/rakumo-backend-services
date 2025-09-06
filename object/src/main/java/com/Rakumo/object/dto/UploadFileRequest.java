package com.Rakumo.object.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.InputStream;

@Data
@AllArgsConstructor
public class UploadFileRequest {

    @NotBlank
    private String bucketName;

    @NotBlank
    private String objectKey;

    @NotNull
    private InputStream fileData;

    private String contentType;
}
