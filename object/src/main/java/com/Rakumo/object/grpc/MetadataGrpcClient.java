package com.Rakumo.object.grpc;

import com.Rakumo.metadata.bucket.*;
import com.Rakumo.metadata.object.*;
import com.Rakumo.metadata.object.DeleteResponse;
import com.Rakumo.object.exception.MetadataServiceException;
import com.Rakumo.object.exception.ObjectNotFoundException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MetadataGrpcClient {

    @GrpcClient("metadata-service")
    private ObjectServiceGrpc.ObjectServiceBlockingStub objectServiceStub;

    @GrpcClient("bucket-service")
    private BucketServiceGrpc.BucketServiceBlockingStub bucketServiceStub;

    public BucketListResponse userBucketList(String OwnerId) {
        try {
            log.debug("Retrieving buckets for user: {}", OwnerId);

            GetUserBucketsRequest request = GetUserBucketsRequest.newBuilder()
                    .setOwnerId(OwnerId)
                    .build();

            BucketListResponse response = bucketServiceStub.getUserBuckets(request);
            log.info("Retrieved {} buckets for user: {}", response.getBucketsCount(), OwnerId);
            return response;

        } catch (StatusRuntimeException e) {
            log.error("Failed to retrieve user buckets: owner={}, error={}",
                    OwnerId, e.getStatus().getCode(), e);
            throw new RuntimeException("Failed to retrieve user buckets: " + e.getStatus().getCode());
        } catch (Exception e) {
            log.error("Unexpected error retrieving user buckets: owner={}", OwnerId, e);
            throw new RuntimeException("Unexpected error retrieving user buckets");
        }
    }

    /**
     * Create a new object in metadata service
     */
    public ObjectResponse createObject(String id, String bucketId, String objectKey, String versionId, String etag, long size) throws MetadataServiceException {
        try {
            log.debug("Creating object metadata: bucket={}, key={}, version={}", bucketId, objectKey, versionId);

            CreateObjectRequest request = CreateObjectRequest.newBuilder()
                    .setId(id)
                    .setBucketId(bucketId)
                    .setObjectKey(objectKey)
                    .setLatestVersionId(versionId)
                    .setLatestEtag(etag)
                    .setLatestSize(size)
                    .build();

            ObjectResponse response = objectServiceStub.createObject(request);
            log.info("Successfully created object metadata: id={}, bucket={}, key={}",
                    response.getId(), bucketId, objectKey);
            return response;

        } catch (StatusRuntimeException e) {
            log.error("Failed to create object metadata: bucket={}, key={}, error={}",
                    bucketId, objectKey, e.getStatus().getCode(), e);
            throw new MetadataServiceException("Failed to create object: " + e.getStatus().getCode());
        } catch (Exception e) {
            log.error("Unexpected error creating object metadata: bucket={}, key={}", bucketId, objectKey, e);
            throw new MetadataServiceException("Unexpected error creating object");
        }
    }

    /**
     * Get object metadata
     */
    public ObjectResponse getObject(String bucketId, String objectId) throws MetadataServiceException, ObjectNotFoundException {
        try {
            log.debug("Retrieving object metadata: bucket={}, id={}", bucketId, objectId);

            GetObjectRequest request = GetObjectRequest.newBuilder()
                    .setBucketId(bucketId)
                    .setObjectId(objectId)
                    .build();

            ObjectResponse response = objectServiceStub.getObject(request);
            log.debug("Successfully retrieved object metadata: id={}, bucket={}", objectId, bucketId);
            return response;

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                log.warn("Object not found in metadata service: bucket={}, id={}", bucketId, objectId);
                throw new ObjectNotFoundException("Object not found: " + objectId);
            }
            log.error("Failed to retrieve object metadata: bucket={}, id={}, error={}",
                    bucketId, objectId, e.getStatus().getCode(), e);
            throw new MetadataServiceException("Failed to retrieve object: " + e.getStatus().getCode());
        } catch (Exception e) {
            log.error("Unexpected error retrieving object metadata: bucket={}, id={}", bucketId, objectId, e);
            throw new MetadataServiceException("Unexpected error retrieving object");
        }
    }

    /**
     * Get all objects in a bucket
     */
    public ObjectListResponse getBucketObjects(String bucketId) throws MetadataServiceException {
        try {
            log.debug("Retrieving objects for bucket: {}", bucketId);

            GetBucketObjectsRequest request = GetBucketObjectsRequest.newBuilder()
                    .setBucketId(bucketId)
                    .build();

            ObjectListResponse response = objectServiceStub.getBucketObjects(request);
            log.info("Retrieved {} objects for bucket: {}", response.getObjectsCount(), bucketId);
            return response;

        } catch (StatusRuntimeException e) {
            log.error("Failed to retrieve bucket objects: bucket={}, error={}",
                    bucketId, e.getStatus().getCode(), e);
            throw new MetadataServiceException("Failed to retrieve bucket objects: " + e.getStatus().getCode());
        } catch (Exception e) {
            log.error("Unexpected error retrieving bucket objects: bucket={}", bucketId, e);
            throw new MetadataServiceException("Unexpected error retrieving bucket objects");
        }
    }

    /**
     * Update object metadata
     */
    public ObjectResponse updateObject(String objectId, String bucketId, String versionId,
                                       String etag, long size, boolean isDeleted) throws MetadataServiceException, ObjectNotFoundException {
        try {
            log.debug("Updating object metadata: id={}, bucket={}, version={}", objectId, bucketId, versionId);

            UpdateObjectRequest request = UpdateObjectRequest.newBuilder()
                    .setObjectId(objectId)
                    .setBucketId(bucketId)
                    .setLatestVersionId(versionId)
                    .setLatestEtag(etag)
                    .setLatestSize(size)
                    .setIsDeleted(isDeleted)
                    .build();

            ObjectResponse response = objectServiceStub.updateObject(request);
            log.info("Successfully updated object metadata: id={}, bucket={}", objectId, bucketId);
            return response;

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                log.warn("Object not found for update: bucket={}, id={}", bucketId, objectId);
                throw new ObjectNotFoundException("Object not found: " + objectId);
            }
            log.error("Failed to update object metadata: bucket={}, id={}, error={}",
                    bucketId, objectId, e.getStatus().getCode(), e);
            throw new MetadataServiceException("Failed to update object: " + e.getStatus().getCode());
        } catch (Exception e) {
            log.error("Unexpected error updating object metadata: bucket={}, id={}", bucketId, objectId, e);
            throw new MetadataServiceException("Unexpected error updating object");
        }
    }

    /**
     * Delete object metadata
     */
    public DeleteResponse deleteObject(String bucketId, String objectId) throws MetadataServiceException {
        try {
            log.info("Deleting object metadata: bucket={}, id={}", bucketId, objectId);

            DeleteObjectRequest request = DeleteObjectRequest.newBuilder()
                    .setBucketId(bucketId)
                    .setObjectId(objectId)
                    .build();

            DeleteResponse response = objectServiceStub.deleteObject(request);

            if (response.getSuccess()) {
                log.info("Successfully deleted object metadata: bucket={}, id={}", bucketId, objectId);
            } else {
                log.warn("Object metadata deletion failed: bucket={}, id={}, message={}",
                        bucketId, objectId, response.getMessage());
            }
            return response;

        } catch (StatusRuntimeException e) {
            log.error("Failed to delete object metadata: bucket={}, id={}, error={}",
                    bucketId, objectId, e.getStatus().getCode(), e);
            throw new MetadataServiceException("Failed to delete object: " + e.getStatus().getCode());
        } catch (Exception e) {
            log.error("Unexpected error deleting object metadata: bucket={}, id={}", bucketId, objectId, e);
            throw new MetadataServiceException("Unexpected error deleting object");
        }
    }

    /**
     * Helper method to check if object exists
     */
    public boolean objectExists(String bucketId, String objectId) {
        try {
            log.debug("Checking object existence: bucket={}, id={}", bucketId, objectId);
            getObject(bucketId, objectId);
            return true;
        } catch (ObjectNotFoundException e) {
            log.debug("Object does not exist: bucket={}, id={}", bucketId, objectId);
            return false;
        } catch (Exception e) {
            log.warn("Error checking object existence: bucket={}, id={}", bucketId, objectId, e);
            return false;
        }
    }

    /**
     * Helper method to get object size
     */
    public long getObjectSize(String bucketId, String objectId) {
        try {
            ObjectResponse response = getObject(bucketId, objectId);
            return response.getLatestSize();
        } catch (ObjectNotFoundException e) {
            log.warn("Object not found when getting size: bucket={}, id={}", bucketId, objectId);
            return -1L;
        } catch (Exception e) {
            log.error("Error getting object size: bucket={}, id={}", bucketId, objectId, e);
            return -1L;
        }
    }

    /**
     * Helper method to verify object checksum
     */
    public boolean verifyObjectChecksum(String bucketId, String objectId, String expectedEtag) {
        try {
            ObjectResponse response = getObject(bucketId, objectId);
            boolean matches = response.getLatestEtag().equals(expectedEtag);

            if (!matches) {
                log.warn("Checksum mismatch for object: bucket={}, id={}, expected={}, actual={}",
                        bucketId, objectId, expectedEtag, response.getLatestEtag());
            }

            return matches;
        } catch (ObjectNotFoundException e) {
            log.warn("Object not found when verifying checksum: bucket={}, id={}", bucketId, objectId);
            return false;
        } catch (Exception e) {
            log.error("Error verifying object checksum: bucket={}, id={}", bucketId, objectId, e);
            return false;
        }
    }
}