package com.example.demo.controller.image;
import com.example.demo.dto.image.InitiateUploadRequest;
import com.example.demo.dto.image.MediaAssetResponse;
import com.example.demo.services.media.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;
    /**
     * Called when a logged-in user wants to upload a file.
     * Saves record in MySQL DB and returns a temporary PUT URL.
     */
    @PostMapping("/upload-url")
    public ResponseEntity<MediaAssetResponse> initiateUpload(
            @Valid @RequestBody InitiateUploadRequest request,
            Authentication authentication
            ) {

        // Extract owner ID from JWT 'sub' claim
        // Long ownerId = Long.parseLong(jwt.getSubject());
        Long userId = extractUserId(authentication);

        MediaAssetResponse response = mediaService.initiateUpload(request, userId);
        return ResponseEntity.ok(response);
    }


    @PostMapping(value = "/upload-direct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<MediaAssetResponse> uploadDirect(
        @RequestPart("file") MultipartFile file,
        @RequestParam("clientAppId") String clientAppId,
        @RequestParam("entityType") String entityType,
        @RequestParam("entityId") String entityId,
        Authentication authentication) {

    Long userId = extractUserId(authentication);
    MediaAssetResponse response = mediaService.uploadDirectlyToMinio(file, clientAppId, entityType, entityId, userId);
    return ResponseEntity.ok(response);
}
    /**
     * Called when an app needs to display an image.
     * Validates user access and returns a short-lived (15-second) GET URL.
     */
    @GetMapping("/{mediaId}/access-url")
    public ResponseEntity<MediaAssetResponse> getAccessUrl(
            @PathVariable Long mediaId,
            Authentication authentication
) {

        Long userId = extractUserId(authentication);

        MediaAssetResponse response = mediaService.getAccessUrl(mediaId, userId);
        return ResponseEntity.ok(response);
    }

    
    private Long extractUserId(Authentication authentication) {
        // authentication.getPrincipal() returns userIdStr set in your JwtFilter
        return Long.parseLong(authentication.getPrincipal().toString());
    }
}