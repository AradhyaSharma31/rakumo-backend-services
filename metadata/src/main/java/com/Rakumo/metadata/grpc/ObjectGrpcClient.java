/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.rakumo.metadata.grpc;

import com.rakumo.object.storage.DeleteObjectsInBucketRequest;
import com.rakumo.object.storage.DeleteObjectsInBucketResponse;
import com.rakumo.object.storage.FileStorageServiceProtoGrpc;
import io.grpc.StatusRuntimeException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

/**
 * GRPC client for interacting with the file storage service.
 */
@Slf4j
@Component
public class ObjectGrpcClient {

  @GrpcClient("file-storage-service")
  private FileStorageServiceProtoGrpc
          .FileStorageServiceProtoBlockingStub fileStorageServiceProtoStub;

  /**
   * Deletes multiple objects from a specified bucket for a given owner.
   *
   * @param ownerId    the ID of the owner of the bucket
   * @param bucketId   the ID of the bucket from which to delete objects
   * @param objectKeys the list of object keys to be deleted
   * @param fileIds    the list of file IDs corresponding to the objects to be deleted
   * @return a response containing the result of the delete operation
   */
  public DeleteObjectsInBucketResponse deleteObjectsInBucket(String ownerId, String bucketId, List<String> objectKeys, List<String> fileIds) {
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
