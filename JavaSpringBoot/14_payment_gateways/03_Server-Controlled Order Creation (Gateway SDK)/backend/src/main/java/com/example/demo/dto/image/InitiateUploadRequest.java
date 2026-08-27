package com.example.demo.dto.image;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InitiateUploadRequest(

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name cannot exceed 255 characters")
    String fileName,

    @NotBlank(message = "MIME type is required")
    @Pattern(
        regexp = "^(image/jpeg|image/png|image/webp|image/gif|application/pdf)$",
        message = "Unsupported file type. Supported types: JPEG, PNG, WEBP, GIF, PDF"
    )
    String mimeType,

    @Min(value = 1, message = "File size must be greater than 0 bytes")
    Long fileSizeBytes,

    @Size(max = 100, message = "Client App ID cannot exceed 100 characters")
    String clientAppId,  // e.g., "E-Commerce-App", "CRM-Software"

    @Size(max = 100, message = "Entity Type cannot exceed 100 characters")
    String entityType,   // e.g., "PROFILE_AVATAR", "PRODUCT_IMAGE"

    @Size(max = 255, message = "Entity ID cannot exceed 255 characters")
    String entityId,     // e.g., "product_9876", "user_1024"

    @Pattern(
        regexp = "^(PRIVATE|APP_RESTRICTED|PUBLIC_READ)$",
        message = "Visibility must be PRIVATE, APP_RESTRICTED, or PUBLIC_READ"
    )
    String visibility    // Default will fall back to "PRIVATE" if null
) {}