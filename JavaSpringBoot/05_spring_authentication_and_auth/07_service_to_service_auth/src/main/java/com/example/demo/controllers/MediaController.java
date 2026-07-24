package com.example.demo.controllers;

import com.example.demo.models.meta_data.MediaMetadata;
import com.example.demo.repo.meta_data.MediaMetadataRepository;
import com.example.demo.services.S3StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    @Autowired
    private MediaMetadataRepository repository;

    @Autowired
    private S3StorageService s3Service;

    // -------------------------------------------------------------------
    // 1. GENERATE PRESIGNED UPLOAD URL
    // -------------------------------------------------------------------
    @PostMapping("/presign-upload")
    public ResponseEntity<?> getPresignedUploadUrl(
            @RequestBody UploadRequest request,
            Authentication authentication) {

        // Extracted automatically from JwtFilter (targetUserId)
        String userId = (String) authentication.getPrincipal();

        String mediaId = "med_" + UUID.randomUUID().toString().substring(0, 12);
        String s3Key = "uploads/user_" + userId + "/" + mediaId + "-" + request.getFileName();

        // Save initial record as PENDING
        MediaMetadata metadata = new MediaMetadata(
                mediaId, userId, s3Key, request.getMimeType(), request.getFileSize(), MediaMetadata.MediaStatus.PENDING
        );
        repository.save(metadata);

        // Generate short-lived S3 PUT URL (e.g., 5 minutes)
        String uploadUrl = s3Service.generatePresignedUploadUrl(s3Key, request.getMimeType(), 5);

        return ResponseEntity.ok(Map.of(
                "mediaId", mediaId,
                "uploadUrl", uploadUrl,
                "expiresInMinutes", 5
        ));
    }

    // -------------------------------------------------------------------
    // 2. CONFIRM UPLOAD COMPLETED (Called after frontend uploads bytes to S3)
    // -------------------------------------------------------------------
    @PostMapping("/{mediaId}/confirm")
    public ResponseEntity<?> confirmUpload(
            @PathVariable String mediaId,
            Authentication authentication) {

        String userId = (String) authentication.getPrincipal();

        MediaMetadata metadata = repository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found"));

        // Ensure owner matches request principal
        if (!metadata.getOwnerId().equals(userId)) {
            return ResponseEntity.status(403).body("Access Denied: Not the media owner");
        }

        metadata.setStatus(MediaMetadata.MediaStatus.COMPLETED);
        repository.save(metadata);

        return ResponseEntity.ok(Map.of("message", "Media status updated to COMPLETED", "mediaId", mediaId));
    }

    // -------------------------------------------------------------------
    // 3. GENERATE PRESIGNED DOWNLOAD/READ URL
    // -------------------------------------------------------------------
    @GetMapping("/{mediaId}/presign-download")
    public ResponseEntity<?> getPresignedDownloadUrl(
            @PathVariable String mediaId,
            Authentication authentication) {

        MediaMetadata metadata = repository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found"));

        if (metadata.getStatus() != MediaMetadata.MediaStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("Media upload is not completed yet");
        }

        // Generate short-lived S3 GET URL (e.g., 60 minutes)
        String downloadUrl = s3Service.generatePresignedDownloadUrl(metadata.getStorageKey(), 60);

        return ResponseEntity.ok(Map.of(
                "mediaId", mediaId,
                "downloadUrl", downloadUrl,
                "mimeType", metadata.getMimeType(),
                "expiresInMinutes", 60
        ));
    }

    // DTO for upload request
    public static class UploadRequest {
        private String fileName;
        private String mimeType;
        private Long fileSize;

        public String getFileName() { return fileName; }
        public String getMimeType() { return mimeType; }
        public Long getFileSize() { return fileSize; }
    }
}