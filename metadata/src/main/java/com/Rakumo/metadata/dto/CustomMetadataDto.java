package com.Rakumo.metadata.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomMetadataDTO {

    private UUID Id;
    private String key;
    private String value;

}
