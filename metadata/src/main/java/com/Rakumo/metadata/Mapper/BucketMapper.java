package com.Rakumo.metadata.Mapper;

import com.Rakumo.metadata.DTO.BucketDTO;
import com.Rakumo.metadata.Models.Bucket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BucketMapper {

    BucketDTO toDto(Bucket bucket);

    Bucket toEntity(BucketDTO dto);
}