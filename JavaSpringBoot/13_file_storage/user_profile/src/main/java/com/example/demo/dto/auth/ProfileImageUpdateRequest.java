package com.example.demo.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * ProfileImageUpdate
 */
public class ProfileImageUpdateRequest {
    
    @NotBlank(message = "Media ID is required")
    private String mediaId;

    public ProfileImageUpdateRequest() {}

    public ProfileImageUpdateRequest(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }
}