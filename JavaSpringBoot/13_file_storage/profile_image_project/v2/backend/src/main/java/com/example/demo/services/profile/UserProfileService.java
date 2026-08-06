package com.example.demo.services.profile;

import com.example.demo.models.auth.UserModel;
import com.example.demo.models.profile.UserProfileModel;
import com.example.demo.repo.auth.UserRepo; // Adjust to your User Repo package
import com.example.demo.repo.profile.UserProfileRepo; // Adjust to your Profile Repo package
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserProfileRepo profileRepo;
    private final UserRepo userRepo;

    public UserProfileService(UserProfileRepo profileRepo, UserRepo userRepo) {
        this.profileRepo = profileRepo;
        this.userRepo = userRepo;
    }

    @Transactional(readOnly = true)
    public UserProfileModel getProfile(Long userId) {
        return profileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user ID: " + userId));
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
    public UserProfileModel updateProfile(Long userId, UserProfileModel request) {
        UserProfileModel existingProfile = getProfile(userId);

        if (request.getFirstName() != null) {
            existingProfile.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            existingProfile.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            existingProfile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getBio() != null) {
            existingProfile.setBio(request.getBio());
        }
        if (request.getProfileMediaId() != null) {
            existingProfile.setProfileMediaId(request.getProfileMediaId());
        }

        return profileRepo.save(existingProfile);
    }

    @Transactional
    public void deleteProfile(Long userId) {
        if (!profileRepo.existsById(userId)) {
            throw new RuntimeException("Profile not found for user ID: " + userId);
        }
        profileRepo.deleteById(userId);
    }
}