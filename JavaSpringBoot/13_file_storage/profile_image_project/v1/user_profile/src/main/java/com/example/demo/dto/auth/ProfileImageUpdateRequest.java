package com.example.demo.dto.auth;

import jakarta.validation.constraints.NotNull;

/**
 * ProfileImageUpdateRequest
 */
public class ProfileImageUpdateRequest {
    
    @NotNull(message = "Media ID is required")
    private Long mediaId; // 👈 Changed String to Long

    public ProfileImageUpdateRequest() {}

    public ProfileImageUpdateRequest(Long mediaId) {
        this.mediaId = mediaId;
    }

    public Long getMediaId() { return mediaId; }
    public void setMediaId(Long mediaId) { this.mediaId = mediaId; }
}