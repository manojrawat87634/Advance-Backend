package com.example.demo.repo.meta_data;


import com.example.demo.models.meta_data.MediaMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaMetadataRepository extends JpaRepository<MediaMetadata, String> {

    // Find all media files owned by a specific user
    List<MediaMetadata> findByOwnerId(String ownerId);

    // Find pending media entries
    List<MediaMetadata> findByStatus(MediaMetadata.MediaStatus status);
}