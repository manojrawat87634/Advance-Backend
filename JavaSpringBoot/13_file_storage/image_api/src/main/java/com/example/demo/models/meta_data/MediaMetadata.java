package com.example.demo.models.meta_data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "media_metadata")
public class MediaMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ Tells Hibernate MySQL handles AUTO_INCREMENT
    private Long id; 

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaStatus status = MediaStatus.PENDING; // Default status

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MediaStatus { PENDING, COMPLETED, FAILED }

    public MediaMetadata() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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