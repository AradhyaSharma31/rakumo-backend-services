package com.Rakumo.gateway.dto;

public class ObjectDTO {

    // File Storage DTOs
    public record StoreFileRequestDTO(String ownerId, String bucketName, String objectKey,
                                      byte[] fileData, String contentType, String expectedChecksum) {}
    public record StoreFileResponseDTO(String bucketName, String objectKey, String versionId,
                                       String checksum, Long sizeBytes) {}
    public record RetrieveFileRequestDTO(String bucketName, String objectKey, String versionId) {}
    public record DeleteFileRequestDTO(String ownerId, String bucketName, String objectKey, String fileId) {}
    public record DeleteFileResponseDTO(Boolean success, String message) {}

    // Upload DTOs
    public record UploadFileRequestDTO(String bucketName, String objectKey, String ownerId,
                                       String contentType, byte[] fileData) {}
    public record InitiateMultipartRequestDTO(String bucketName, String objectKey, String ownerId, String contentType) {}
    public record InitiateMultipartResponseDTO(String uploadId) {}
    public record UploadChunkRequestDTO(String uploadId, Integer chunkIndex, byte[] chunkData) {}
    public record UploadChunkResponseDTO(Boolean success, Integer chunkIndex, String uploadId) {}
    public record CompleteMultipartRequestDTO(String uploadId) {}
    public record AbortMultipartRequestDTO(String uploadId) {}
    public record AbortMultipartResponseDTO(Boolean success, String uploadId) {}
    public record UploadResponseDTO(String bucketName, String objectKey, String versionId,
                                    String checksum, Long sizeBytes, String uploadedAt) {}

    // Download DTOs
    public record DownloadRequestDTO(String bucketName, String objectKey, String versionId) {}
    public record DownloadResponseDTO(String bucketName, String objectKey, String versionId,
                                      Long contentLength, String contentType, String checksum,
                                      String lastModified, byte[] fileData) {}

    // Add to your existing ObjectDTO class

    // Pre-signed URL DTOs
    public record GeneratePreSignedUrlRequestDTO(
            String bucketName,
            String objectKey,
            String versionId,
            String operation, // "DOWNLOAD", "UPLOAD", "DELETE"
            Integer expirationSeconds,
            String contentType
    ) {}

    public record GeneratePreSignedUrlResponseDTO(
            String preSignedUrl,
            String bucketName,
            String objectKey,
            String versionId,
            String operation,
            String expiration
    ) {}

    public record ValidatePreSignedUrlRequestDTO(
            String url,
            String bucketName,
            String objectKey
    ) {}

    public record ValidatePreSignedUrlResponseDTO(
            boolean isValid
    ) {}
}
