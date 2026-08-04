package com.example.demo.controller.auth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.auth.AuthRequest;
import com.example.demo.services.auth.UserAuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
      private UserAuthService userAuthService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody AuthRequest request) {

        userAuthService.registerUser(request);

        return ResponseEntity.ok(
                Map.of("message", "User registered successfully!")
        );
    }

}
