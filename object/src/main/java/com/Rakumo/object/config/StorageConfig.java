package com.Rakumo.object.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageConfig {

    @Value("${storage.root:./storage}")
    private String storageRoot;

    @Value("${storage.temp:./storage/temp}")
    private String tempRoot;

    @Bean
    public Path storageRootPath() {
        return Paths.get(storageRoot);
    }

    @Bean
    public Path tempRootPath() {
        return Paths.get(tempRoot);
    }
}