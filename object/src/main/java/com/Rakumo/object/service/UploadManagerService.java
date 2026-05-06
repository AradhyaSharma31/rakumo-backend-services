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

package com.rakumo.object.service;

import com.rakumo.object.dto.UploadRequest;
import com.rakumo.object.dto.UploadResponse;
import com.rakumo.object.exception.ChecksumMismatchException;
import com.rakumo.object.exception.MetadataServiceException;

import java.io.IOException;
import java.io.InputStream;

public interface UploadManagerService {
    UploadResponse handleRegularUpload(UploadRequest request, InputStream fileData)
            throws IOException, MetadataServiceException;

    String initiateMultipartUpload(UploadRequest request);

    void uploadChunk(String uploadId, int chunkIndex, InputStream chunkData)
            throws IOException;

    UploadResponse completeMultipartUpload(String uploadId)
            throws IOException, MetadataServiceException, ChecksumMismatchException;

    void abortMultipartUpload(String uploadId) throws IOException;
}
