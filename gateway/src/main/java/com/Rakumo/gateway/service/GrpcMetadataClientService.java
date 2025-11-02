package com.Rakumo.gateway.service;

import com.Rakumo.gateway.grpc.*;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcMetadataClientService {

    // Bucket Service Client
    @GrpcClient("metadata-service")
    private BucketServiceGrpc.BucketServiceBlockingStub bucketStub;

    // Object Metadata Service Client
    @GrpcClient("metadata-service")
    private ObjectServiceGrpc.ObjectServiceBlockingStub objectStub;

    // Object Version Service Client
    @GrpcClient("metadata-service")
    private ObjectVersionServiceGrpc.ObjectVersionServiceBlockingStub versionStub;

    // Custom Metadata Service Client
    @GrpcClient("metadata-service")
    private CustomMetadataServiceGrpc.CustomMetadataServiceBlockingStub customMetadataStub;

    // ========== BUCKET OPERATIONS ==========

    public BucketResponse createBucket(CreateBucketRequest request) {
        try {
            log.info("Creating bucket: {}", request.getName());
            return bucketStub.createBucket(request);
        } catch (StatusRuntimeException e) {
            log.error("Bucket creation failed: {}", e.getStatus());
            throw new RuntimeException("Bucket service unavailable: " + e.getStatus().getDescription());
        }
    }

    public BucketResponse getBucket(GetBucketRequest request) {
        try {
            log.info("Getting bucket: {}", request.getBucketId());
            return bucketStub.getBucket(request);
        } catch (StatusRuntimeException e) {
            log.error("Bucket fetch failed: {}", e.getStatus());
            throw new RuntimeException("Bucket service unavailable: " + e.getStatus().getDescription());
        }
    }

    public BucketResponse updateBucket(UpdateBucketRequest request) {
        try {
            log.info("Updating bucket: {}", request.getBucketId());
            return bucketStub.updateBucket(request);
        } catch (StatusRuntimeException e) {
            log.error("Bucket update failed: {}", e.getStatus());
            throw new RuntimeException("Bucket service unavailable: " + e.getStatus().getDescription());
        }
    }

    public DeleteResponse deleteBucket(DeleteBucketRequest request) {
        try {
            log.info("Deleting bucket: {}", request.getBucketId());
            return bucketStub.deleteBucket(request);
        } catch (StatusRuntimeException e) {
            log.error("Bucket deletion failed: {}", e.getStatus());
            throw new RuntimeException("Bucket service unavailable: " + e.getStatus().getDescription());
        }
    }

    public BucketListResponse getUserBuckets(GetUserBucketsRequest request) {
        try {
            log.info("Getting buckets for user: {}", request.getOwnerId());
            return bucketStub.getUserBuckets(request);
        } catch (StatusRuntimeException e) {
            log.error("Bucket list failed: {}", e.getStatus());
            throw new RuntimeException("Bucket service unavailable: " + e.getStatus().getDescription());
        }
    }

    // ========== OBJECT OPERATIONS ==========

    public ObjectResponse createObject(CreateObjectRequest request) {
        try {
            log.info("Creating object: {}", request.getObjectKey());
            return objectStub.createObject(request);
        } catch (StatusRuntimeException e) {
            log.error("Object creation failed: {}", e.getStatus());
            throw new RuntimeException("Object service unavailable: " + e.getStatus().getDescription());
        }
    }

    public ObjectResponse getObject(GetObjectRequest request) {
        try {
            log.info("Getting object: {}", request.getObjectId());
            return objectStub.getObject(request);
        } catch (StatusRuntimeException e) {
            log.error("Object fetch failed: {}", e.getStatus());
            throw new RuntimeException("Object service unavailable: " + e.getStatus().getDescription());
        }
    }

    public ObjectListResponse getBucketObjects(GetBucketObjectsRequest request) {
        try {
            log.info("Getting objects for bucket: {}", request.getBucketId());
            return objectStub.getBucketObjects(request);
        } catch (StatusRuntimeException e) {
            log.error("Object list failed: {}", e.getStatus());
            throw new RuntimeException("Object service unavailable: " + e.getStatus().getDescription());
        }
    }

    public ObjectResponse updateObject(UpdateObjectRequest request) {
        try {
            log.info("Updating object: {}", request.getObjectId());
            return objectStub.updateObject(request);
        } catch (StatusRuntimeException e) {
            log.error("Object update failed: {}", e.getStatus());
            throw new RuntimeException("Object service unavailable: " + e.getStatus().getDescription());
        }
    }

    public DeleteResponse deleteObject(DeleteObjectRequest request) {
        try {
            log.info("Deleting object: {}", request.getObjectId());
            return objectStub.deleteObject(request);
        } catch (StatusRuntimeException e) {
            log.error("Object deletion failed: {}", e.getStatus());
            throw new RuntimeException("Object service unavailable: " + e.getStatus().getDescription());
        }
    }

    // ========== VERSION OPERATIONS ==========

    public VersionResponse createVersion(CreateVersionRequest request) {
        try {
            log.info("Creating version for object: {}", request.getObjectId());
            return versionStub.createVersion(request);
        } catch (StatusRuntimeException e) {
            log.error("Version creation failed: {}", e.getStatus());
            throw new RuntimeException("Version service unavailable: " + e.getStatus().getDescription());
        }
    }

    public VersionResponse getVersion(GetVersionRequest request) {
        try {
            log.info("Getting version: {}", request.getVersionId());
            return versionStub.getVersion(request);
        } catch (StatusRuntimeException e) {
            log.error("Version fetch failed: {}", e.getStatus());
            throw new RuntimeException("Version service unavailable: " + e.getStatus().getDescription());
        }
    }

    public VersionListResponse getObjectVersions(GetObjectVersionsRequest request) {
        try {
            log.info("Getting versions for object: {}", request.getObjectId());
            return versionStub.getObjectVersions(request);
        } catch (StatusRuntimeException e) {
            log.error("Version list failed: {}", e.getStatus());
            throw new RuntimeException("Version service unavailable: " + e.getStatus().getDescription());
        }
    }

    public VersionResponse updateVersion(UpdateVersionRequest request) {
        try {
            log.info("Updating version: {}", request.getVersionId());
            return versionStub.updateVersion(request);
        } catch (StatusRuntimeException e) {
            log.error("Version update failed: {}", e.getStatus());
            throw new RuntimeException("Version service unavailable: " + e.getStatus().getDescription());
        }
    }

    public DeleteResponse deleteVersion(DeleteVersionRequest request) {
        try {
            log.info("Deleting version: {}", request.getVersionId());
            return versionStub.deleteVersion(request);
        } catch (StatusRuntimeException e) {
            log.error("Version deletion failed: {}", e.getStatus());
            throw new RuntimeException("Version service unavailable: " + e.getStatus().getDescription());
        }
    }

    // ========== CUSTOM METADATA OPERATIONS ==========

    public CustomMetadataResponse addMetadata(CustomMetadataRequest request) {
        try {
            log.info("Adding metadata for version: {}", request.getVersionId());
            return customMetadataStub.addMetadata(request);
        } catch (StatusRuntimeException e) {
            log.error("Metadata addition failed: {}", e.getStatus());
            throw new RuntimeException("Metadata service unavailable: " + e.getStatus().getDescription());
        }
    }

    public CustomMetadataResponse getMetadata(GetMetadataRequest request) {
        try {
            log.info("Getting metadata for version: {}", request.getVersionId());
            return customMetadataStub.getMetadata(request);
        } catch (StatusRuntimeException e) {
            log.error("Metadata fetch failed: {}", e.getStatus());
            throw new RuntimeException("Metadata service unavailable: " + e.getStatus().getDescription());
        }
    }

    public CustomMetadataResponse removeMetadata(RemoveMetadataRequest request) {
        try {
            log.info("Removing metadata for version: {}", request.getVersionId());
            return customMetadataStub.removeMetadata(request);
        } catch (StatusRuntimeException e) {
            log.error("Metadata removal failed: {}", e.getStatus());
            throw new RuntimeException("Metadata service unavailable: " + e.getStatus().getDescription());
        }
    }
}