package com.example.demo.apiKey.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiKeyResponse {
    private Long id;
    private String apiKey;
    private String name;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}