package com.example.demo.services.redis;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.config.BucketConfiguration;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.distributed.proxy.ProxyManager;

@Service
public class RateLimitService {

    private final ProxyManager<String> proxyManager;
    private final BucketConfiguration configuration;

    public RateLimitService(
            ProxyManager<String> proxyManager,
            BucketConfiguration configuration) {

        this.proxyManager = proxyManager;
        this.configuration = configuration;
    }

    public boolean isAllowed(
            String key,
            long capacity,
            Duration duration) {

        Bucket bucket = proxyManager.builder()
                .build(
                        key,
                        () -> configuration.createBucket(capacity, duration));

        return bucket.tryConsume(1);
    }
}