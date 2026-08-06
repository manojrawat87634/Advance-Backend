package com.example.demo.controller.profile;





import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.profile.UserProfileModel;

@RestController
@RequestMapping("/api/v1/profile")
public class UserProfileController {

    private final UserProfileService profileService;

    public UserProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    // GET /api/v1/profile - Fetch authenticated user's profile
    @GetMapping
    public ResponseEntity<UserProfileModel> getMyProfile(Authentication authentication) {
        Long userId = extractUserId(authentication);
        UserProfileModel profile = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }

    // POST /api/v1/profile - Create profile for authenticated user
    @PostMapping
    public ResponseEntity<UserProfileModel> createProfile(
            Authentication authentication,
            @RequestBody UserProfileModel profileRequest) {
        Long userId = extractUserId(authentication);
        UserProfileModel createdProfile = profileService.createProfile(userId, profileRequest);
        return ResponseEntity.status(201).body(createdProfile);
    }

    // PUT /api/v1/profile - Update authenticated user's profile
    @PutMapping
    public ResponseEntity<UserProfileModel> updateProfile(
            Authentication authentication,
            @RequestBody UserProfileModel profileRequest) {
        Long userId = extractUserId(authentication);
        UserProfileModel updatedProfile = profileService.updateProfile(userId, profileRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    // DELETE /api/v1/profile - Delete authenticated user's profile
    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(Authentication authentication) {
        Long userId = extractUserId(authentication);
        profileService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }

    // Helper method to extract the user ID from the Authentication principal
    private Long extractUserId(Authentication authentication) {
        // Adjust cast/method call depending on how your UserFilter stores the Principal
        // e.g., ((CustomUserPrincipal) authentication.getPrincipal()).getId() 
        // or Long.parseLong(authentication.getName())
        return Long.parseLong(authentication.getName());
    }
}