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

package com.rakumo.object.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonUtils() {} // Prevent instantiation

    public static <T> List<T> readList(Path path, Class<T> type) throws IOException {
        return mapper.readValue(Files.readAllBytes(path),
                mapper.getTypeFactory().constructCollectionType(List.class, type));
    }

    // support for TypeReference
    public static <T> T readValue(Path path, TypeReference<T> typeReference) throws IOException {
        return mapper.readValue(Files.readAllBytes(path), typeReference);
    }

    public static <T> T readValue(Path path, Class<T> type) throws IOException {
        return mapper.readValue(path.toFile(), type);
    }

    public static void write(Path path, Object data) throws IOException {
        Path tempPath = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            mapper.writeValue(tempPath.toFile(), data);
            Files.move(tempPath, path, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }
}
