package com.Rakumo.gateway.controller;

import com.Rakumo.gateway.dto.MetadataDTO.*;
import com.Rakumo.gateway.mapper.GrpcMapper;
import com.Rakumo.gateway.service.GrpcMetadataClientService;
import com.Rakumo.metadata.bucket.*;
import com.Rakumo.metadata.bucket.DeleteResponse;
import com.Rakumo.metadata.object.*;
//import com.Rakumo.metadata.object.version.*;
import com.Rakumo.metadata.object.version.custom.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataGatewayController {

    private final GrpcMetadataClientService metadataClientService;
    private final GrpcMapper mapper;

    // ========== BUCKET ENDPOINTS ==========

    @PostMapping("/buckets")
    public ResponseEntity<BucketResponseDTO> createBucket(@RequestBody CreateBucketRequestDTO requestDTO) {
        CreateBucketRequest grpcRequest = mapper.toGrpcCreateBucket(requestDTO);
        BucketResponse grpcResponse = metadataClientService.createBucket(grpcRequest);
        BucketResponseDTO responseDTO = mapper.toDtoBucket(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/buckets/{bucketId}")
    public ResponseEntity<BucketResponseDTO> getBucket(
            @PathVariable String bucketId,
            @RequestParam String ownerId) {
        GetBucketRequest grpcRequest = mapper.toGrpcGetBucket(bucketId, ownerId);
        BucketResponse grpcResponse = metadataClientService.getBucket(grpcRequest);
        BucketResponseDTO responseDTO = mapper.toDtoBucket(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/buckets/{bucketId}")
    public ResponseEntity<BucketResponseDTO> updateBucket(
            @PathVariable String bucketId,
            @RequestBody UpdateBucketRequestDTO requestDTO) {
        UpdateBucketRequestDTO updatedDto = new UpdateBucketRequestDTO(
                requestDTO.ownerId(), bucketId, requestDTO.name(),
                requestDTO.versioningEnabled(), requestDTO.region()
        );
        UpdateBucketRequest grpcRequest = mapper.toGrpcUpdateBucket(updatedDto);
        BucketResponse grpcResponse = metadataClientService.updateBucket(grpcRequest);
        BucketResponseDTO responseDTO = mapper.toDtoBucket(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/buckets/{bucketId}")
    public ResponseEntity<DeleteResponseDTO> deleteBucket(
            @PathVariable String bucketId,
            @RequestParam String ownerId) {
        DeleteBucketRequest grpcRequest = mapper.toGrpcDeleteBucket(bucketId, ownerId);
        DeleteResponse grpcResponse = metadataClientService.deleteBucket(grpcRequest);
        DeleteResponseDTO responseDTO = mapper.toDtoDelete(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/buckets")
    public ResponseEntity<BucketListResponseDTO> getUserBuckets(@RequestParam String ownerId) {
        GetUserBucketsRequest grpcRequest = mapper.toGrpcGetUserBuckets(ownerId);
        BucketListResponse grpcResponse = metadataClientService.getUserBuckets(grpcRequest);
        BucketListResponseDTO responseDTO = mapper.toDtoBucketList(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    // ========== OBJECT ENDPOINTS ==========

    @PostMapping("/objects")
    public ResponseEntity<ObjectResponseDTO> createObject(@RequestBody CreateObjectRequestDTO requestDTO) {
        CreateObjectRequest grpcRequest = mapper.toGrpcCreateObject(requestDTO);
        ObjectResponse grpcResponse = metadataClientService.createObject(grpcRequest);
        ObjectResponseDTO responseDTO = mapper.toDtoObject(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/objects/{objectId}")
    public ResponseEntity<ObjectResponseDTO> getObject(
            @PathVariable String objectId,
            @RequestParam String bucketId) {
        GetObjectRequest grpcRequest = mapper.toGrpcGetObject(objectId, bucketId);
        ObjectResponse grpcResponse = metadataClientService.getObject(grpcRequest);
        ObjectResponseDTO responseDTO = mapper.toDtoObject(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/buckets/{bucketId}/objects")
    public ResponseEntity<ObjectListResponseDTO> getBucketObjects(@PathVariable String bucketId) {
        GetBucketObjectsRequest grpcRequest = mapper.toGrpcGetBucketObjects(bucketId);
        ObjectListResponse grpcResponse = metadataClientService.getBucketObjects(grpcRequest);
        ObjectListResponseDTO responseDTO = mapper.toDtoObjectList(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/objects/{objectId}")
    public ResponseEntity<ObjectResponseDTO> updateObject(
            @PathVariable String objectId,
            @RequestBody UpdateObjectRequestDTO requestDTO) {
        UpdateObjectRequestDTO updatedDto = new UpdateObjectRequestDTO(
                objectId, requestDTO.bucketId(), requestDTO.latestVersionId(),
                requestDTO.latestEtag(), requestDTO.latestSize(), requestDTO.isDeleted()
        );
        UpdateObjectRequest grpcRequest = mapper.toGrpcUpdateObject(updatedDto);
        ObjectResponse grpcResponse = metadataClientService.updateObject(grpcRequest);
        ObjectResponseDTO responseDTO = mapper.toDtoObject(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/objects/{objectId}")
    public ResponseEntity<DeleteResponseDTO> deleteObject(
            @PathVariable String objectId,
            @RequestParam String bucketId) {
        DeleteObjectRequest grpcRequest = mapper.toGrpcDeleteObject(objectId, bucketId);
        com.Rakumo.metadata.object.DeleteResponse grpcResponse = metadataClientService.deleteObject(grpcRequest);
        DeleteResponseDTO responseDTO = mapper.toDtoDelete(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    // ========== VERSION ENDPOINTS ==========

//    @PostMapping("/versions")
//    public ResponseEntity<VersionResponseDTO> createVersion(@RequestBody CreateVersionRequestDTO requestDTO) {
//        CreateVersionRequest grpcRequest = mapper.toGrpcCreateVersion(requestDTO);
//        VersionResponse grpcResponse = metadataClientService.createVersion(grpcRequest);
//        VersionResponseDTO responseDTO = mapper.toDtoVersion(grpcResponse);
//        return ResponseEntity.ok(responseDTO);
//    }

//    @GetMapping("/versions/{versionId}")
//    public ResponseEntity<VersionResponseDTO> getVersion(
//            @PathVariable String versionId,
//            @RequestParam String objectId) {
//        GetVersionRequest grpcRequest = mapper.toGrpcGetVersion(versionId, objectId);
//        VersionResponse grpcResponse = metadataClientService.getVersion(grpcRequest);
//        VersionResponseDTO responseDTO = mapper.toDtoVersion(grpcResponse);
//        return ResponseEntity.ok(responseDTO);
//    }

//    @GetMapping("/objects/{objectId}/versions")
//    public ResponseEntity<VersionListResponseDTO> getObjectVersions(@PathVariable String objectId) {
//        GetObjectVersionsRequest grpcRequest = mapper.toGrpcGetObjectVersions(objectId);
//        VersionListResponse grpcResponse = metadataClientService.getObjectVersions(grpcRequest);
//        VersionListResponseDTO responseDTO = mapper.toDtoVersionList(grpcResponse);
//        return ResponseEntity.ok(responseDTO);
//    }

//    @PutMapping("/versions/{versionId}")
//    public ResponseEntity<VersionResponseDTO> updateVersion(
//            @PathVariable String versionId,
//            @RequestBody UpdateVersionRequestDTO requestDTO) {
//        UpdateVersionRequestDTO updatedDto = new UpdateVersionRequestDTO(
//                versionId, requestDTO.objectId(), requestDTO.etag(), requestDTO.storageLocation(),
//                requestDTO.size(), requestDTO.contentType(), requestDTO.isDeleteMarker(), requestDTO.storageClass()
//        );
//        UpdateVersionRequest grpcRequest = mapper.toGrpcUpdateVersion(updatedDto);
//        VersionResponse grpcResponse = metadataClientService.updateVersion(grpcRequest);
//        VersionResponseDTO responseDTO = mapper.toDtoVersion(grpcResponse);
//        return ResponseEntity.ok(responseDTO);
//    }

//    @DeleteMapping("/versions/{versionId}")
//    public ResponseEntity<DeleteResponseDTO> deleteVersion(
//            @PathVariable String versionId,
//            @RequestParam String objectId) {
//        DeleteVersionRequest grpcRequest = mapper.toGrpcDeleteVersion(versionId, objectId);
//        DeleteResponse grpcResponse = metadataClientService.deleteVersion(grpcRequest);
//        DeleteResponseDTO responseDTO = mapper.toDtoDelete(grpcResponse);
//        return ResponseEntity.ok(responseDTO);
//    }

    // ========== CUSTOM METADATA ENDPOINTS ==========

    @PostMapping("/metadata")
    public ResponseEntity<CustomMetadataResponseDTO> addMetadata(@RequestBody CustomMetadataRequestDTO requestDTO) {
        CustomMetadataRequest grpcRequest = mapper.toGrpcCustomMetadataRequest(requestDTO);
        CustomMetadataResponse grpcResponse = metadataClientService.addMetadata(grpcRequest);
        CustomMetadataResponseDTO responseDTO = mapper.toDtoCustomMetadataResponse(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/versions/{versionId}/metadata")
    public ResponseEntity<CustomMetadataResponseDTO> getMetadata(@PathVariable String versionId) {
        GetMetadataRequest grpcRequest = mapper.toGrpcGetMetadata(versionId);
        CustomMetadataResponse grpcResponse = metadataClientService.getMetadata(grpcRequest);
        CustomMetadataResponseDTO responseDTO = mapper.toDtoCustomMetadataResponse(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/versions/{versionId}/metadata")
    public ResponseEntity<CustomMetadataResponseDTO> removeMetadata(
            @PathVariable String versionId,
            @RequestParam String key) {
        RemoveMetadataRequest grpcRequest = mapper.toGrpcRemoveMetadata(versionId, key);
        CustomMetadataResponse grpcResponse = metadataClientService.removeMetadata(grpcRequest);
        CustomMetadataResponseDTO responseDTO = mapper.toDtoCustomMetadataResponse(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }
}