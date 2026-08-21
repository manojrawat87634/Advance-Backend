package com.example.demo.dto.image;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL) // Omits null fields from JSON output
public record MediaAssetResponse(
    Long id,
    Long ownerId,
    String clientAppId,
    String entityType,
    String entityId,
    String fileName,
    String mimeType,
    Long fileSizeBytes,
    String visibility,
    
    // Presigned URLs (only populated depending on endpoint)
    String uploadUrl,  // Presigned PUT URL (5-min expiration for uploading)
    String accessUrl,  // Presigned GET URL (15-sec expiration for viewing)
    
    Instant createdAt
) {
    // Convenient overloaded constructor when no URLs are present
    public MediaAssetResponse(
            Long id,
            Long ownerId,
            String clientAppId,
            String entityType,
            String entityId,
            String fileName,
            String mimeType,
            Long fileSizeBytes,
            String visibility,
            Instant createdAt
    ) {
        this(id, ownerId, clientAppId, entityType, entityId, fileName, mimeType, fileSizeBytes, visibility, null, null, createdAt);
    }
}