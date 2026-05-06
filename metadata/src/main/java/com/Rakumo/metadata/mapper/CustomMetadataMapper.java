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

import com.rakumo.metadata.dto.CustomMetadataDto;
import com.rakumo.metadata.entity.CustomMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting between CustomMetadata entity and CustomMetadataDto.
 */
@Mapper(componentModel = "spring")
public interface CustomMetadataMapper {
  CustomMetadataMapper INSTANCE = Mappers.getMapper(CustomMetadataMapper.class);

  /**
   * Converts a CustomMetadata entity to a CustomMetadataDto.
   *
   * @param customMetadata the CustomMetadata entity to convert
   * @return the corresponding CustomMetadataDto
   */
  CustomMetadataDto toDto(CustomMetadata customMetadata);

  /**
   * Converts a CustomMetadataDto to a CustomMetadata entity.
   *
   * @param dto the CustomMetadataDto to convert
   * @return the corresponding CustomMetadata entity
   */
  CustomMetadata toEntity(CustomMetadataDto dto);

  /**
   * Converts a CustomMetadataDto to a CustomMetadata entity for use in gRPC communication.
   *
   * @param dto the CustomMetadataDto to convert
   * @return the corresponding CustomMetadata entity
   */
  com.rakumo.metadata.object.version.custom.CustomMetadata toProto(CustomMetadataDto dto);
}
