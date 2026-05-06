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

package com.rakumo.metadata.mapper;

import com.rakumo.metadata.dto.ObjectMetadataDto;
import com.rakumo.metadata.entity.ObjectMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between ObjectMetadata entity and ObjectMetadataDto.
 */
@Mapper(componentModel = "spring")
public interface ObjectMetadataMapper {

  /**
   * Maps the bucketId from the ObjectMetadata entity to the ObjectMetadataDto.
   */
  @Mapping(source = "bucket.bucketId", target = "bucketId")
  ObjectMetadataDto toDto(ObjectMetadata objectMetadata);

  /**
   * Maps the bucketId from the ObjectMetadataDto to the ObjectMetadata entity.
   *
   * @param dto the ObjectMetadataDto containing the bucketId to be mapped.
   * @return the ObjectMetadata entity with the bucketId mapped from the ObjectMetadataDto.
   */
  ObjectMetadata toEntity(ObjectMetadataDto dto);
}
