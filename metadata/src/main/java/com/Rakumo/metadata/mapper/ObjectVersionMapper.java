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

import com.rakumo.metadata.dto.ObjectVersionDto;
import com.rakumo.metadata.entity.ObjectVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting between ObjectVersion entity and ObjectVersionDto.
 */
@Mapper(componentModel = "spring")
public interface ObjectVersionMapper {
  ObjectVersionMapper INSTANCE = Mappers.getMapper(ObjectVersionMapper.class);

  /**
   * MapStruct does not support nested property mapping directly, so we need to specify the source and target explicitly.
   * Here we map objectVersion.object.objectId to objectVersionDto.objectId.
   */
  @Mapping(source = "object.id", target = "objectId")
  ObjectVersionDto toDto(ObjectVersion objectVersion);

  /**
   * MapStruct does not support nested property mapping directly, so we need to specify the source and target explicitly.
   * Here we map objectVersionDto.objectId to objectVersion.object.id.
   */
  ObjectVersion toEntity(ObjectVersionDto dto);
}
