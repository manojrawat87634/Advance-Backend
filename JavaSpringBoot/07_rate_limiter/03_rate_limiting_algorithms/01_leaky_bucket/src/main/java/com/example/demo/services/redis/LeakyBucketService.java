package com.example.demo.services.redis;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LeakyBucketService {
    private final StringRedisTemplate redisTemplate;
    public LeakyBucketService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /* Add a request to the bucket.*/
    public void enqueue(String bucketKey, String request) {
        redisTemplate.opsForList().rightPush(bucketKey, request);
    }

    /* Remove the oldest request from the bucket.*/
    public Optional<String> dequeue(String bucketKey) {
        return Optional.ofNullable(redisTemplate.opsForList().leftPop(bucketKey));
    }

    /* Current number of requests waiting in the bucket. */
    public long size(String bucketKey) {
        Long size = redisTemplate.opsForList().size(bucketKey);
        return size == null ? 0 : size;
    }

    /*  Returns true if the bucket has reached its capacity.*/
    public boolean isFull(String bucketKey, long capacity) {
        return size(bucketKey) >= capacity;
    }

    /** Remove every request from the bucket. Useful while testing.*/
    public void clear(String bucketKey) {
        redisTemplate.delete(bucketKey);
    }
}