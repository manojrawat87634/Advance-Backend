package com.example.demo.config;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
// import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.api.StatefulRedisConnection;

@Configuration
public class BucketConfiguration {

    public io.github.bucket4j.BucketConfiguration createBucket(long capacity,
                                                               Duration duration) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, duration)
                .build();
        return io.github.bucket4j.BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }
 
    @Bean
public ProxyManager<String> proxyManager(
        StatefulRedisConnection<String, byte[]> connection) {

    return Bucket4jLettuce
            .casBasedBuilder(connection)
            .build();
}

}