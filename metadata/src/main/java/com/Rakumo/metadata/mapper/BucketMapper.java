package com.Rakumo.metadata.Mapper;

import com.Rakumo.metadata.DTO.BucketDTO;
import com.Rakumo.metadata.Models.Bucket;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BucketMapper {

    BucketDTO toDto(Bucket bucket);

    Bucket toEntity(BucketDTO dto);
}