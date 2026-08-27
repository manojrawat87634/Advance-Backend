package com.example.demo.repo.profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.models.profile.UserProfileModel;

import java.util.Optional;

@Repository
public interface UserProfileRepo extends JpaRepository<UserProfileModel, Long> {

    // Find profile by user ID (inherited findById works, but explicit for clarity)
    Optional<UserProfileModel> findByUserId(Long userId);

    // Find profile by phone number
    Optional<UserProfileModel> findByPhoneNumber(String phoneNumber);

    // Check if a profile exists for a given user ID
    boolean existsByUserId(Long userId);

    // Custom deletion by user ID
    void deleteByUserId(Long userId);
}