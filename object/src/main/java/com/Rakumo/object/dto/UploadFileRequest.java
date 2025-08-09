package com.Rakumo.object.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadFileRequest {

    @NotBlank
    private String bucketName;

    @NotBlank
    private String objectKey;

    @NotNull
    private MultipartFile file;

    private String contentType;
}
