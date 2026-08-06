package com.example.demo.controller.profile;

import com.example.demo.models.profile.UserProfileModel;
import com.example.demo.services.profile.UserProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
public class UserProfileController {

    private final UserProfileService profileService;

    public UserProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<UserProfileModel> getProfile(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PostMapping
    public ResponseEntity<UserProfileModel> createProfile(
            Authentication authentication,
            @RequestBody UserProfileModel profileRequest) {
        Long userId = extractUserId(authentication);
        System.out.println(userId);
        UserProfileModel createdProfile = profileService.createProfile(userId, profileRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProfile);
    }

    @PutMapping
    public ResponseEntity<UserProfileModel> updateProfile(
            Authentication authentication,
            @RequestBody UserProfileModel profileRequest) {
        Long userId = extractUserId(authentication);
        UserProfileModel updatedProfile = profileService.updateProfile(userId, profileRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(Authentication authentication) {
        Long userId = extractUserId(authentication);
        profileService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(Authentication authentication) {
        // authentication.getPrincipal() returns userIdStr set in your JwtFilter
        return Long.parseLong(authentication.getPrincipal().toString());
    }
}