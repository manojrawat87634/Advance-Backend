package com.example.demo.services.media;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.image.InitiateUploadRequest;
import com.example.demo.dto.image.MediaAssetResponse;
import com.example.demo.models.image.MediaAsset;
import com.example.demo.models.profile.UserProfileModel;
import com.example.demo.repo.image.MediaAssetRepository;
import com.example.demo.repo.profile.UserProfileRepo;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaAssetRepository mediaRepository;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client; // Injected for direct multipart uploads
        @Autowired
private UserProfileRepo userProfileRepository;
    @Value("${aws.s3.bucket}")
    private String bucketName;

    /**
     * 1-Phase Direct Upload: Takes MultipartFile, streams directly to MinIO,
     * and saves the metadata record in MySQL in a single API call.
     */
    @Transactional
    public MediaAssetResponse uploadDirectlyToMinio(
            MultipartFile file,
            String clientAppId,
            String entityType,
            String entityId,
            Long ownerId) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "-" + originalFileName;
        String fileKey = String.format("apps/%s/%s/%d/%s",
                clientAppId != null ? clientAppId : "default",
                entityType != null ? entityType : "general",
                ownerId,
                uniqueFileName
        );

        // 1. Stream binary file directly to MinIO/S3
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to stream binary file directly to MinIO/S3", e);
        }

        // 2. Persist media metadata in MySQL
        MediaAsset asset = MediaAsset.builder()
                .ownerId(ownerId)
                .clientAppId(clientAppId)
                .entityType(entityType)
                .entityId(entityId)
                .fileKey(fileKey)
                .fileName(originalFileName)
                .mimeType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .visibility("PRIVATE")
                .build();

        MediaAsset savedAsset = mediaRepository.save(asset);

        if ("PROFILE_AVATAR".equalsIgnoreCase(entityType)) {
                UserProfileModel profile = userProfileRepository.findById(ownerId)
                        .orElseGet(() -> {
                                UserProfileModel newProfile = new UserProfileModel();
                                newProfile.setUserId(ownerId);
                                return newProfile;
                        });
                profile.setProfileMedia(savedAsset);
                userProfileRepository.save(profile);
        }
        return mapToResponse(savedAsset, null, null);
    }

    /**
     * Creates a DB record in MySQL and returns a 5-minute presigned PUT URL 
     * allowing direct binary file uploads to S3 / Cloudflare R2 / MinIO.
     */
    @Transactional
    public MediaAssetResponse initiateUpload(InitiateUploadRequest request, Long ownerId) {
        String uniqueFileName = UUID.randomUUID() + "-" + request.fileName();
        String fileKey = String.format("apps/%s/%s/%d/%s",
                request.clientAppId() != null ? request.clientAppId() : "default",
                request.entityType() != null ? request.entityType() : "general",
                ownerId,
                uniqueFileName
        );

        MediaAsset asset = MediaAsset.builder()
                .ownerId(ownerId)
                .clientAppId(request.clientAppId())
                .entityType(request.entityType())
                .entityId(request.entityId())
                .fileKey(fileKey)
                .fileName(request.fileName())
                .mimeType(request.mimeType())
                .fileSizeBytes(request.fileSizeBytes())
                .visibility(request.visibility() != null ? request.visibility() : "PRIVATE")
                .build();

        MediaAsset savedAsset = mediaRepository.save(asset);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(request.mimeType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

        return mapToResponse(savedAsset, uploadUrl, null);
    }

    /**
     * Validates owner access and returns a short-lived (15-second) presigned GET URL 
     * to render the image securely in web or mobile clients.
     */
    @Transactional(readOnly = true)
    public MediaAssetResponse getAccessUrl(Long mediaId, Long requesterId) {
        MediaAsset asset = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media asset not found with ID: " + mediaId));

        if ("PRIVATE".equals(asset.getVisibility()) && !asset.getOwnerId().equals(requesterId)) {
            throw new AccessDeniedException("You do not have permission to access this media asset.");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(asset.getFileKey())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(60*5))
                .getObjectRequest(getObjectRequest)
                .build();

        String accessUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

        return mapToResponse(asset, null, accessUrl);
    }

 private MediaAssetResponse mapToResponse(MediaAsset asset, String uploadUrl, String accessUrl) {
    return new MediaAssetResponse(
            asset.getId(),
            asset.getOwnerId(),
            asset.getClientAppId(),
            asset.getEntityType(),
            asset.getEntityId(),
            asset.getFileName(),
            asset.getMimeType(),
            asset.getFileSizeBytes(),
            asset.getVisibility(),
            uploadUrl,
            accessUrl,
            asset.getCreatedAt()
    );
}
}