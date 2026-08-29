package com.example.demo.dto.notes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

public class NoteDto {

    @Data
    public static class CreateNoteRequest {
        @NotNull(message = "Media Asset ID is required")
        private Long mediaAssetId;

        @NotBlank(message = "Title is required")
        private String title;

        private String description;

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be positive or zero")
        private Long priceInSubunits;

        private String currency = "INR";
        private Boolean isPublished = true;
    }

    @Data
    public static class UpdateNoteRequest {
        private String title;
        private String description;
        private Long priceInSubunits;
        private String currency;
        private Boolean isPublished;
    }

    @Data
    @Builder
    public static class NoteResponse {
        private Long id;
        private Long uploaderId;
        private Long mediaAssetId;
        private String title;
        private String description;
        private Long priceInSubunits;
        private String currency;
        private Boolean isPublished;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}