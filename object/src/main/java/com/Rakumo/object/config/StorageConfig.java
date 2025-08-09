package com.Rakumo.object.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties(prefix =  "storage")
@Data
public class StorageConfig {

    private Path root = Path.of("/data");

}
