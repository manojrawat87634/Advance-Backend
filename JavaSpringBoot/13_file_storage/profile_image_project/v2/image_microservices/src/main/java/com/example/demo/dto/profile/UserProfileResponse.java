package com.example.demo.dto.profile;
import com.example.demo.models.profile.UserProfileModel;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String bio;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Profile image metadata + 5-min presigned URL
    private ProfileMediaDto profileMedia;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileMediaDto {
        private Long id;
        private String fileName;
        private String mimeType;
        private Long fileSizeBytes;
        private String presignedUrl; // Expire after 5 minutes
    }

    public static UserProfileResponse fromEntity(UserProfileModel entity, String presignedUrl) {
        ProfileMediaDto mediaDto = null;
        
        if (entity.getProfileMedia() != null) {
            mediaDto = ProfileMediaDto.builder()
                    .id(entity.getProfileMedia().getId())
                    .fileName(entity.getProfileMedia().getFileName())
                    .mimeType(entity.getProfileMedia().getMimeType())
                    .fileSizeBytes(entity.getProfileMedia().getFileSizeBytes())
                    .presignedUrl(presignedUrl)
                    .build();
        }

        return UserProfileResponse.builder()
                .userId(entity.getUserId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phoneNumber(entity.getPhoneNumber())
                .bio(entity.getBio())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .profileMedia(mediaDto)
                .build();
    }
}