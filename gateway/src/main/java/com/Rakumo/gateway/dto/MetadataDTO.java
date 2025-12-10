package com.Rakumo.gateway.dto;

import java.util.List;

public class MetadataDTO {

    // Bucket DTOs
    public record CreateBucketRequestDTO(String ownerId, String name, Boolean versioningEnabled, String region) {}
    public record UpdateBucketRequestDTO(String ownerId, String bucketId, String name, Boolean versioningEnabled, String region) {}
    public record BucketResponseDTO(
            String bucketId,
            String ownerId,
            String name,
            String createdAt,
            Boolean versioningEnabled,
            String region,
            List<ObjectMetadataDTO> objects,
            String updatedAt
    ) {}
    public record BucketListResponseDTO(List<BucketResponseDTO> buckets) {}

    // Object DTOs
    public record CreateObjectRequestDTO(String bucketId, String objectKey, String latestVersionId,
                                         String latestEtag, Long latestSize) {}
    public record UpdateObjectRequestDTO(String objectId, String bucketId, String latestVersionId,
                                         String latestEtag, Long latestSize, Boolean isDeleted) {}
    public record ObjectResponseDTO(
            String id,
            String bucketId,
            String objectKey,
            String latestVersionId,
            String latestEtag,
            Long latestSize,
            String createdAt,
            String updatedAt,
            Boolean isDeleted,
            List<ObjectVersionDTO> versions
    ) {}
    public record ObjectListResponseDTO(List<ObjectResponseDTO> objects) {}

    // Version DTOs
    public record CreateVersionRequestDTO(String objectId, String etag, String storageLocation, Long size,
                                          String contentType, Boolean isDeleteMarker, String storageClass,
                                          List<CustomMetadataDTO> customMetadata) {}
    public record UpdateVersionRequestDTO(String versionId, String objectId, String etag, String storageLocation,
                                          Long size, String contentType, Boolean isDeleteMarker, String storageClass) {}
    public record VersionResponseDTO(
            String versionId,
            String objectId,
            String etag,
            String storageLocation,
            Long size,
            String contentType,
            String createdAt,
            Boolean isDeleteMarker,
            String storageClass,
            List<CustomMetadataDTO> customMetadata
    ) {}
    public record VersionListResponseDTO(List<VersionResponseDTO> versions) {}

    // Custom Metadata DTOs
    public record CustomMetadataDTO(String key, String value) {}
    public record CustomMetadataRequestDTO(String versionId, String key, String value) {}
    public record CustomMetadataResponseDTO(String versionId, List<CustomMetadataDTO> metadata) {}

    // Common DTOs
    public record DeleteResponseDTO(Boolean success, String message) {}

    // Nested DTOs
    public record ObjectMetadataDTO(
            String id,
            String bucketId,
            String createdAt,
            String objectKey,
            String latestVersionId,
            String latestEtag,
            Long latestSize,
            String updatedAt,
            Boolean isDeleted
    ) {}

    public record ObjectVersionDTO(
            String versionId,
            String objectId,
            String etag,
            Long size,
            String createdAt,
            Boolean isLatest
    ) {}

}