package com.example.demo.repo.image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.models.image.MediaAsset;
import java.util.List;
import java.util.Optional;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    // Find a specific asset by key
    Optional<MediaAsset> findByFileKey(String fileKey);
    // Get all media owned by a specific user (paginated)
    Page<MediaAsset> findByOwnerId(Long ownerId, Pageable pageable);
    // Get all media owned by a user for a specific client application
    List<MediaAsset> findByOwnerIdAndClientAppId(Long ownerId, String clientAppId);
    // Get media linked to a specific entity (e.g., entityType="PRODUCT_IMAGE", entityId="prod_9876")
    List<MediaAsset> findByEntityTypeAndEntityId(String entityType, String entityId);
    // Delete asset record by File Key
    void deleteByFileKey(String fileKey);
}