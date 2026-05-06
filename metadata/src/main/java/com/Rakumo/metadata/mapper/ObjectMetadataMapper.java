package com.Rakumo.metadata.Mapper;

import com.Rakumo.metadata.DTO.ObjectMetadataDTO;
import com.Rakumo.metadata.Models.ObjectMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ObjectMetadataMapper {

    @Mapping(source = "bucket.bucketId", target = "bucketId")
    ObjectMetadataDTO toDto(ObjectMetadata objectMetadata);

    ObjectMetadata toEntity(ObjectMetadataDTO dto);
}