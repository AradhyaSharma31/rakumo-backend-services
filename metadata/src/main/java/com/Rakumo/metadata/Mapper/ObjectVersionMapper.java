package com.Rakumo.metadata.Mapper;

import com.Rakumo.metadata.DTO.ObjectVersionDTO;
import com.Rakumo.metadata.Models.ObjectVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ObjectVersionMapper {
    ObjectVersionMapper INSTANCE = Mappers.getMapper(ObjectVersionMapper.class);

    @Mapping(source = "object.id", target = "objectId")
    ObjectVersionDTO toDto(ObjectVersion objectVersion);

    ObjectVersion toEntity(ObjectVersionDTO dto);
}