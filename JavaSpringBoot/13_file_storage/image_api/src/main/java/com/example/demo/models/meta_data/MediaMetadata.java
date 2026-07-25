package com.example.demo.models.meta_data;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "media_metadata")
public class MediaMetadata {

    @Id
    private String id; // Unique Media ID (e.g., "med_998877")

    @Column(name = "owner_id", nullable = false)
    private String ownerId; // Target User ID extracted by JwtFilter

    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey; // S3 Path: "uploads/user_101/uuid-filename.jpg"

    @Column(name = "mime_type", nullable = false)
    private String mimeType; // e.g., "image/jpeg"

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaStatus status; // PENDING, COMPLETED, FAILED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MediaStatus { PENDING, COMPLETED, FAILED }

    // Constructors, Getters, and Setters
    public MediaMetadata() {}

    public MediaMetadata(String id, String ownerId, String storageKey, String mimeType, Long sizeBytes, MediaStatus status) {
        this.id = id;
        this.ownerId = ownerId;
        this.storageKey = storageKey;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.status = status;
    }

    // Getters and Setters ...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public MediaStatus getStatus() { return status; }
    public void setStatus(MediaStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}