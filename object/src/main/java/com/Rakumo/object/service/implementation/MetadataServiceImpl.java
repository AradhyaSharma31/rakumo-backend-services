package com.Rakumo.object.service.implementation;

import com.Rakumo.object.exception.MetadataSyncException;
import com.Rakumo.object.model.LocalObjectReference;
import com.Rakumo.object.service.MetadataService;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataServiceImpl implements MetadataService {

    @GrpcClient("metadata-service")
    private com.rakumo.metadata.object.ObjectServiceGrpc.ObjectServiceBlockingStub objectServiceStub;

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void recordMetadata(LocalObjectReference ref) throws MetadataSyncException {
        try {
            com.rakumo.metadata.object.CreateObjectRequest request = com.rakumo.metadata.object.CreateObjectRequest.newBuilder()
                    .setBucketId(ref.getBucketName())
                    .setObjectKey(ref.getObjectKey())
                    .setLatestVersionId(ref.getVersionId())
                    .setLatestEtag(ref.getChecksum())
//                    .setLatestSize(ref.getSize())
                    .build();

            com.rakumo.metadata.object.ObjectResponse response = objectServiceStub.createObject(request);
            log.debug("Recorded metadata for {}/{}. Object ID: {}",
                    ref.getBucketName(),
                    ref.getObjectKey(),
                    response.getId());
        } catch (StatusRuntimeException e) {
            log.error("Failed to record metadata: {}", e.getStatus().getDescription());
            throw new MetadataSyncException("Metadata service unavailable");
        }
    }

    @Override
    public boolean exists(String bucketName, String objectKey) throws MetadataSyncException {
        try {
            com.rakumo.metadata.object.GetBucketObjectsRequest request = com.rakumo.metadata.object.GetBucketObjectsRequest.newBuilder()
                    .setBucketId(bucketName)
                    .build();

            com.rakumo.metadata.object.ObjectListResponse response = objectServiceStub.getBucketObjects(request);
            return response.getObjectsList().stream()
                    .anyMatch(obj -> obj.getObjectKey().equals(objectKey));
        } catch (StatusRuntimeException e) {
            log.error("Metadata lookup failed: {}", e.getStatus().getDescription());
            throw new MetadataSyncException("Metadata service unavailable");
        }
    }
}