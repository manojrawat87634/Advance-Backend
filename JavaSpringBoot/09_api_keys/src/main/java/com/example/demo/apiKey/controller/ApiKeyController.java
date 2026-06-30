package com.example.demo.apiKey.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.demo.apiKey.dto.ApiKeyResponse;
import com.example.demo.apiKey.dto.CreateApiKeyRequest;
import com.example.demo.apiKey.service.ApiKeyService;
import com.example.demo.models.auth.UserModel;
import com.example.demo.repo.auth.UserRepo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {
    @Autowired
    private final UserRepo userRepo;

    private final ApiKeyService apiKeyService;
@PostMapping
public ResponseEntity<ApiKeyResponse> createApiKey(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody CreateApiKeyRequest request) {

    UserModel user = userRepo.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

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