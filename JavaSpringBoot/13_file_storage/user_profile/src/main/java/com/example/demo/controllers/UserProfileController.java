package com.example.demo.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.auth.AuthRequest;
import com.example.demo.dto.auth.ProfileImageUpdateRequest;
import com.example.demo.services.auth.UserAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * UserProfileController
 */

@RestController
@RequestMapping("/user-profile")
public class UserProfileController {

    @Autowired
    private UserAuthService userAuthService;

    @GetMapping("/get-user-info")
    public ResponseEntity<?> updateUserProfileImage(
            @Valid @RequestBody AuthRequest request) {


        return ResponseEntity.ok(
                Map.of("message", "User registered successfully!"));
    }

    @PostMapping("/update-profile-image")
public ResponseEntity<?> updateUserProfileImage(
        @Valid @RequestBody ProfileImageUpdateRequest request,
        @RequestHeader("Authorization") String bearerToken, // Captures "Bearer <user_access_token>"
        Authentication authentication) {

    // Extract user ID from the Spring Security context
    Long userId = Long.parseLong((String) authentication.getPrincipal());

    // Pass the existing bearerToken straight through
    userAuthService.updateUserProfileImage(userId, request.getMediaId(), bearerToken);

    return ResponseEntity.ok(
            Map.of("message", "Profile Image Updated successfully!"));
}

@PostMapping("/presign-profile-image")
public ResponseEntity<?> getPresignedProfileImage(
        @RequestBody Map<String, Object> request,
        @RequestHeader("Authorization") String bearerToken) {

    String fileName = (String) request.get("fileName");
    String mimeType = (String) request.get("mimeType");
    Long fileSize = Long.parseLong(request.get("fileSize").toString());

    Map<String, Object> response = userAuthService.getPresignedUploadUrl(fileName, mimeType, fileSize, bearerToken);
    return ResponseEntity.ok(response);
}

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam Long sessionId) {
        userAuthService.logout(sessionId);
        return ResponseEntity.ok("Logged out successfully");
    }
}