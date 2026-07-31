package com.example.demo.services;


import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class S3StorageService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name:my-media-bucket}")
    private String bucketName;

    /**
     * Generates a Pre-Signed PUT URL for direct file upload to MinIO.
     */
    public String generatePresignedUploadUrl(String objectKey, String contentType, int expirationMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(expirationMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate MinIO upload pre-signed URL", e);
        }
    }

    /**
     * Generates a Pre-Signed GET URL for reading/downloading a file from MinIO.
     */
    public String generatePresignedDownloadUrl(String objectKey, int expirationMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(expirationMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate MinIO download pre-signed URL", e);
        }
    }
}