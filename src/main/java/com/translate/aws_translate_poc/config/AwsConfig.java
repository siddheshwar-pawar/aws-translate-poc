package com.translate.aws_translate_poc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;

@Configuration
public class AwsConfig {

    @Bean
    public TranslateClient translateClient() {
        return TranslateClient.builder()
                .region(Region.AP_SOUTH_1)
                .build();
    }
}

