package com.example.demo.config;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Configuration
public class BucketConfig {

    @Bean
    public Bucket bucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofDays(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}