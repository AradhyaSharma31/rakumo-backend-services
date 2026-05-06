package com.Rakumo.metadata.gRPC;

import com.Rakumo.object.storage.DeleteObjectsInBucketRequest;
import com.Rakumo.object.storage.DeleteObjectsInBucketResponse;
import com.Rakumo.object.storage.FileStorageServiceProtoGrpc;
import io.grpc.StatusRuntimeException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ObjectGrpcClient {

    @GrpcClient("file-storage-service")
    private FileStorageServiceProtoGrpc.FileStorageServiceProtoBlockingStub fileStorageServiceProtoStub;

    public DeleteObjectsInBucketResponse DeleteObjectsInBucket(String ownerId, String bucketId, List<String> objectKeys, List<String> fileIds) {
        try {

            DeleteObjectsInBucketRequest request = DeleteObjectsInBucketRequest.newBuilder()
                    .setOwnerId(ownerId)
                    .setBucketId(bucketId)
                    .addAllObjectKeys(objectKeys)
                    .addAllFileId(fileIds)
                    .build();

            DeleteObjectsInBucketResponse response = fileStorageServiceProtoStub.deleteObjectsInBucket(request);
            log.info("Deleted objects in bucket {} for user: {}", bucketId, ownerId);
            return response;

        } catch (StatusRuntimeException e) {
            log.error("Failed to delete user object: owner={}, error={}",
                    ownerId, e.getStatus().getCode(), e);
            throw new RuntimeException("Failed to retrieve user buckets: " + e.getStatus().getCode());
        } catch (Exception e) {
            log.error("Unexpected error deleting user object: owner={}", ownerId, e);
            throw new RuntimeException("Unexpected error deleting user object");
        }
    }
}
