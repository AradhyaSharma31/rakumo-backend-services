package com.Rakumo.object.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    // provides lifecycle management for gRPC channels
    @Bean
    public ManagedChannel metadataServiceChannel() {
        return ManagedChannelBuilder
                .forAddress("localhost", 9010)
                .usePlaintext()
                .build();
    }



}
