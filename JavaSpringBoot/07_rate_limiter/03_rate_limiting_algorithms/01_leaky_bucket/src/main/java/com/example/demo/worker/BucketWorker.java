package com.example.demo.worker;

import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.services.redis.LeakyBucketService;

@Component
public class BucketWorker {
    private static final String BUCKET_KEY = "login_bucket";
    private final LeakyBucketService bucketService;
    
    public BucketWorker(LeakyBucketService bucketService) {
        this.bucketService = bucketService;
    }

    @Scheduled(fixedRate = 200)
    public void processBucket() {
        Optional<String> request = bucketService.dequeue(BUCKET_KEY);
        if (request.isEmpty()) {
            return;
        }
        System.out.println("Leaked request: " + request.get());
    }
}