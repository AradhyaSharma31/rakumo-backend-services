package com.Rakumo.metadata.Mapper;

import com.Rakumo.metadata.DTO.CustomMetadataDTO;
import com.Rakumo.metadata.Models.CustomMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CustomMetadataMapper {
    CustomMetadataMapper INSTANCE = Mappers.getMapper(CustomMetadataMapper.class);

    CustomMetadataDTO toDto(CustomMetadata customMetadata);

    CustomMetadata toEntity(CustomMetadataDTO dto);

    com.Rakumo.metadata.object.version.custom.CustomMetadata toProto(CustomMetadataDTO dto);
}