package com.example.demo.apiKey.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.demo.apiKey.dto.ApiKeyResponse;
import com.example.demo.apiKey.dto.CreateApiKeyRequest;
import com.example.demo.apiKey.service.ApiKeyService;
import com.example.demo.models.auth.UserModel;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<ApiKeyResponse> createApiKey(
            @AuthenticationPrincipal UserModel user,
            @Valid @RequestBody CreateApiKeyRequest request) {

        ApiKeyResponse response = apiKeyService.createApiKey(user, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> getMyApiKeys(
            @AuthenticationPrincipal UserModel user) {

        List<ApiKeyResponse> response = apiKeyService.getApiKeys(user);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeApiKey(
            @PathVariable Long id,
            @AuthenticationPrincipal UserModel user) {

        apiKeyService.revokeApiKey(id, user);
        return ResponseEntity.noContent().build();
    }

}