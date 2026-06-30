package com.example.demo.apiKey.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateApiKeyRequest {

    @NotBlank(message = "API key name is required")
    private String name;

}