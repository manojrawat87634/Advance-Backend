package com.example.demo.dto.mediaDto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PresignRequestDto {
    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "MIME type is required")
    private String mimeType;

    @NotNull(message = "File size is required")
    private Long fileSize;
}
