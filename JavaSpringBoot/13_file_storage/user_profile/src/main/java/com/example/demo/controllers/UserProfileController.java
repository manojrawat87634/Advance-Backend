package com.example.demo.controllers;

import com.example.demo.dto.auth.ProfileImageUpdateRequest;
import com.example.demo.dto.mediaDto.PresignRequestDto;
import com.example.demo.dto.mediaDto.PresignResponseDto;
import com.example.demo.services.auth.UserAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user-profile")
public class UserProfileController {

    @Autowired
    private UserAuthService userAuthService;

    // STEP 1: Frontend asks for Presigned URL (Proxy to Media Service)
    @PostMapping("/presign-avatar")
    public ResponseEntity<PresignResponseDto> getPresignedUrl(
            @Valid @RequestBody PresignRequestDto request,
            @RequestHeader("Authorization") String bearerToken) {
                System.out.println("hello");
        PresignResponseDto response = userAuthService.getPresignedUrlFromMediaService(request, bearerToken);
        System.out.print(response);
        return ResponseEntity.ok(response);
    }

    // STEP 3: Frontend sends mediaId to Main Backend to update profile
    @PostMapping("/update-image")
    public ResponseEntity<?> updateProfileImage(
            @Valid @RequestBody ProfileImageUpdateRequest request,
            @RequestHeader("Authorization") String bearerToken,
            Authentication authentication) {

        Long userId = Long.parseLong((String) authentication.getPrincipal());
        userAuthService.updateUserProfileImage(userId, request.getMediaId(), bearerToken);

        return ResponseEntity.ok(Map.of("message", "Profile image updated successfully!"));
    }

    // Fetch profile information
    // @GetMapping("/me")
    // public ResponseEntity<?> getUserInfo(Authentication authentication) {
    //     Long userId = Long.parseLong((String) authentication.getPrincipal());
    //     // return ResponseEntity.ok(userAuthService.getUserProfile(userId));
    // }

    // Logout endpoint
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam Long sessionId) {
        userAuthService.logout(sessionId);
        return ResponseEntity.ok("Logged out successfully");
    }
}