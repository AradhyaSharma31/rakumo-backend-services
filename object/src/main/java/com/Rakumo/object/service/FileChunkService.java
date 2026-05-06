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

// FileChunkService.java
package com.rakumo.object.service;

import com.rakumo.object.entity.FileChunkInfo;
import com.rakumo.object.entity.LocalObjectReference;
import com.rakumo.object.exception.InvalidChunkException;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public interface FileChunkService {
    String initiateMultipartUpload(LocalObjectReference ref);
    void validateChunk(FileChunkInfo chunk) throws InvalidChunkException, IOException;
    List<FileChunkInfo> listChunks(String uploadId);
    void cleanupStaleChunks(Duration olderThan);
    void cleanupUpload(String uploadId) throws IOException;
}
