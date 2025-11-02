package com.Rakumo.gateway.controller;

import com.Rakumo.gateway.grpc.*;
import com.Rakumo.gateway.service.GrpcMetadataClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataGatewayController {

    private final GrpcMetadataClientService metadataClientService;

    // ========== BUCKET ENDPOINTS ==========

    @PostMapping("/buckets")
    public ResponseEntity<BucketResponse> createBucket(@RequestBody CreateBucketRequest request) {
        BucketResponse response = metadataClientService.createBucket(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buckets/{bucketId}")
    public ResponseEntity<BucketResponse> getBucket(
            @PathVariable String bucketId,
            @RequestParam String ownerId) {
        GetBucketRequest request = GetBucketRequest.newBuilder()
                .setBucketId(bucketId)
                .setOwnerId(ownerId)
                .build();
        BucketResponse response = metadataClientService.getBucket(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/buckets/{bucketId}")
    public ResponseEntity<BucketResponse> updateBucket(
            @PathVariable String bucketId,
            @RequestBody UpdateBucketRequest request) {
        UpdateBucketRequest updateRequest = request.toBuilder()
                .setBucketId(bucketId)
                .build();
        BucketResponse response = metadataClientService.updateBucket(updateRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/buckets/{bucketId}")
    public ResponseEntity<DeleteResponse> deleteBucket(
            @PathVariable String bucketId,
            @RequestParam String ownerId) {
        DeleteBucketRequest request = DeleteBucketRequest.newBuilder()
                .setBucketId(bucketId)
                .setOwnerId(ownerId)
                .build();
        DeleteResponse response = metadataClientService.deleteBucket(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buckets")
    public ResponseEntity<BucketListResponse> getUserBuckets(@RequestParam String ownerId) {
        GetUserBucketsRequest request = GetUserBucketsRequest.newBuilder()
                .setOwnerId(ownerId)
                .build();
        BucketListResponse response = metadataClientService.getUserBuckets(request);
        return ResponseEntity.ok(response);
    }

    // ========== OBJECT ENDPOINTS ==========

    @PostMapping("/objects")
    public ResponseEntity<ObjectResponse> createObject(@RequestBody CreateObjectRequest request) {
        ObjectResponse response = metadataClientService.createObject(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/objects/{objectId}")
    public ResponseEntity<ObjectResponse> getObject(
            @PathVariable String objectId,
            @RequestParam String bucketId) {
        GetObjectRequest request = GetObjectRequest.newBuilder()
                .setObjectId(objectId)
                .setBucketId(bucketId)
                .build();
        ObjectResponse response = metadataClientService.getObject(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buckets/{bucketId}/objects")
    public ResponseEntity<ObjectListResponse> getBucketObjects(@PathVariable String bucketId) {
        GetBucketObjectsRequest request = GetBucketObjectsRequest.newBuilder()
                .setBucketId(bucketId)
                .build();
        ObjectListResponse response = metadataClientService.getBucketObjects(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/objects/{objectId}")
    public ResponseEntity<ObjectResponse> updateObject(
            @PathVariable String objectId,
            @RequestBody UpdateObjectRequest request) {
        UpdateObjectRequest updateRequest = request.toBuilder()
                .setObjectId(objectId)
                .build();
        ObjectResponse response = metadataClientService.updateObject(updateRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/objects/{objectId}")
    public ResponseEntity<DeleteResponse> deleteObject(
            @PathVariable String objectId,
            @RequestParam String bucketId) {
        DeleteObjectRequest request = DeleteObjectRequest.newBuilder()
                .setObjectId(objectId)
                .setBucketId(bucketId)
                .build();
        DeleteResponse response = metadataClientService.deleteObject(request);
        return ResponseEntity.ok(response);
    }

    // ========== VERSION ENDPOINTS ==========

    @PostMapping("/versions")
    public ResponseEntity<VersionResponse> createVersion(@RequestBody CreateVersionRequest request) {
        VersionResponse response = metadataClientService.createVersion(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/versions/{versionId}")
    public ResponseEntity<VersionResponse> getVersion(
            @PathVariable String versionId,
            @RequestParam String objectId) {
        GetVersionRequest request = GetVersionRequest.newBuilder()
                .setVersionId(versionId)
                .setObjectId(objectId)
                .build();
        VersionResponse response = metadataClientService.getVersion(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/objects/{objectId}/versions")
    public ResponseEntity<VersionListResponse> getObjectVersions(@PathVariable String objectId) {
        GetObjectVersionsRequest request = GetObjectVersionsRequest.newBuilder()
                .setObjectId(objectId)
                .build();
        VersionListResponse response = metadataClientService.getObjectVersions(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/versions/{versionId}")
    public ResponseEntity<VersionResponse> updateVersion(
            @PathVariable String versionId,
            @RequestBody UpdateVersionRequest request) {
        UpdateVersionRequest updateRequest = request.toBuilder()
                .setVersionId(versionId)
                .build();
        VersionResponse response = metadataClientService.updateVersion(updateRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/versions/{versionId}")
    public ResponseEntity<DeleteResponse> deleteVersion(
            @PathVariable String versionId,
            @RequestParam String objectId) {
        DeleteVersionRequest request = DeleteVersionRequest.newBuilder()
                .setVersionId(versionId)
                .setObjectId(objectId)
                .build();
        DeleteResponse response = metadataClientService.deleteVersion(request);
        return ResponseEntity.ok(response);
    }

    // ========== CUSTOM METADATA ENDPOINTS ==========

    @PostMapping("/metadata")
    public ResponseEntity<CustomMetadataResponse> addMetadata(@RequestBody CustomMetadataRequest request) {
        CustomMetadataResponse response = metadataClientService.addMetadata(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/versions/{versionId}/metadata")
    public ResponseEntity<CustomMetadataResponse> getMetadata(@PathVariable String versionId) {
        GetMetadataRequest request = GetMetadataRequest.newBuilder()
                .setVersionId(versionId)
                .build();
        CustomMetadataResponse response = metadataClientService.getMetadata(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/versions/{versionId}/metadata")
    public ResponseEntity<CustomMetadataResponse> removeMetadata(
            @PathVariable String versionId,
            @RequestParam String key) {
        RemoveMetadataRequest request = RemoveMetadataRequest.newBuilder()
                .setVersionId(versionId)
                .setKey(key)
                .build();
        CustomMetadataResponse response = metadataClientService.removeMetadata(request);
        return ResponseEntity.ok(response);
    }
}