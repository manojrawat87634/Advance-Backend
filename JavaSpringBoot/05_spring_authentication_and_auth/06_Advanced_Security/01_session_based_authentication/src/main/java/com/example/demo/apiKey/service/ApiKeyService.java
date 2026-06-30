package com.example.demo.apiKey.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.apiKey.dto.ApiKeyResponse;
import com.example.demo.apiKey.dto.CreateApiKeyRequest;
import com.example.demo.apiKey.entity.ApiKey;
import com.example.demo.apiKey.repository.ApiKeyRepository;
import com.example.demo.apiKey.util.ApiKeyGenerator;
import com.example.demo.models.auth.UserModel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyGenerator apiKeyGenerator;

    public ApiKeyResponse createApiKey(UserModel user, CreateApiKeyRequest request) {

        String generatedApiKey = apiKeyGenerator.generateApiKey();

        ApiKey apiKey = new ApiKey();
        apiKey.setUser(user);
        apiKey.setApiKey(generatedApiKey);
        apiKey.setName(request.getName());
        apiKey.setActive(true);

        ApiKey savedApiKey = apiKeyRepository.save(apiKey);

        return ApiKeyResponse.builder()
                .id(savedApiKey.getId())
                .apiKey(savedApiKey.getApiKey())
                .name(savedApiKey.getName())
                .active(savedApiKey.getActive())
                .createdAt(savedApiKey.getCreatedAt())
                .expiresAt(savedApiKey.getExpiresAt())
                .build();
    }

    public List<ApiKeyResponse> getApiKeys(UserModel user) {

        return apiKeyRepository.findByUser(user)
                .stream()
                .map(apiKey -> ApiKeyResponse.builder()
                        .id(apiKey.getId())
                        .apiKey(apiKey.getApiKey())
                        .name(apiKey.getName())
                        .active(apiKey.getActive())
                        .createdAt(apiKey.getCreatedAt())
                        .expiresAt(apiKey.getExpiresAt())
                        .build())
                .collect(Collectors.toList());
    }

    public void revokeApiKey(Long apiKeyId, UserModel user) {

        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new RuntimeException("API Key not found"));

        if (!apiKey.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You do not own this API key.");
        }

        apiKey.setActive(false);

        apiKeyRepository.save(apiKey);
    }

    public ApiKey validateApiKey(String apiKey) {

        return apiKeyRepository.findByApiKey(apiKey)
                .filter(ApiKey::getActive)
                .orElseThrow(() -> new RuntimeException("Invalid API Key"));
    }
}