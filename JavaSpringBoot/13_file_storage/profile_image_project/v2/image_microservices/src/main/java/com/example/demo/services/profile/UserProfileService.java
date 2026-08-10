package com.example.demo.services.profile;

import com.example.demo.dto.image.MediaAssetResponse;
import com.example.demo.dto.profile.UserProfileResponse;
import com.example.demo.models.auth.UserModel;
import com.example.demo.models.profile.UserProfileModel;
import com.example.demo.repo.auth.UserRepo; // Adjust to your User Repo package
import com.example.demo.repo.profile.UserProfileRepo; // Adjust to your Profile Repo package
import com.example.demo.services.media.MediaService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserProfileRepo profileRepo;
    private final UserRepo userRepo;
private final MediaService mediaService;
    public UserProfileService(UserProfileRepo profileRepo, UserRepo userRepo, MediaService mediaService) {
        this.profileRepo = profileRepo;
        this.userRepo = userRepo;
        this.mediaService = mediaService;
        
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {

        UserProfileModel profile = profileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user ID: " + userId));

        MediaAssetResponse mediaResponse = null;

        // Reusing your existing getAccessUrl method!
        if (profile.getProfileMedia() != null && !Boolean.TRUE.equals(profile.getProfileMedia().getIsDeleted())) {
            mediaResponse = mediaService.getAccessUrl(profile.getProfileMedia().getId(), userId);
        }
     return buildResponseDto(profile, mediaResponse);

    }

    @Transactional
    public UserProfileModel createProfile(Long userId, UserProfileModel request) {
        if (profileRepo.existsById(userId)) {
            throw new IllegalStateException("Profile already exists for user ID: " + userId);
        }

        // Attach the UserModel reference so JPA maps the PK/FK correctly
        UserModel userProxy = userRepo.getReferenceById(userId);
        request.setUser(userProxy);

        return profileRepo.save(request);
    }
@Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileModel request) {
        UserProfileModel profile = profileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user ID: " + userId));

        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getProfileMedia() != null) {
            profile.setProfileMedia(request.getProfileMedia());
        }

        UserProfileModel savedProfile = profileRepo.save(profile);

        MediaAssetResponse mediaResponse = null;
        if (savedProfile.getProfileMedia() != null && !Boolean.TRUE.equals(savedProfile.getProfileMedia().getIsDeleted())) {
            mediaResponse = mediaService.getAccessUrl(savedProfile.getProfileMedia().getId(), userId);
        }

        return buildResponseDto(savedProfile, mediaResponse);
    }

    @Transactional
    public void deleteProfile(Long userId) {
        if (!profileRepo.existsById(userId)) {
            throw new RuntimeException("Profile not found for user ID: " + userId);
        }
        profileRepo.deleteById(userId);
    }

private UserProfileResponse buildResponseDto(UserProfileModel profile, MediaAssetResponse mediaResponse) {
    UserProfileResponse.ProfileMediaDto mediaDto = null;

    if (mediaResponse != null) {
        mediaDto = UserProfileResponse.ProfileMediaDto.builder()
                .id(mediaResponse.id())
                .fileName(mediaResponse.fileName())
                .mimeType(mediaResponse.mimeType())
                .fileSizeBytes(mediaResponse.fileSizeBytes())
                .presignedUrl(mediaResponse.accessUrl())
                .build();
    }

    return UserProfileResponse.builder()
            .userId(profile.getUserId())
            .firstName(profile.getFirstName())
            .lastName(profile.getLastName())
            .phoneNumber(profile.getPhoneNumber())
            .bio(profile.getBio())
            .createdAt(profile.getCreatedAt())
            .updatedAt(profile.getUpdatedAt())
            .profileMedia(mediaDto)
            .build();
}
}