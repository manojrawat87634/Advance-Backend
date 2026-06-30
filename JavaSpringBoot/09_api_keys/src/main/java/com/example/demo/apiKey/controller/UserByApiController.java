package com.example.demo.apiKey.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repo.auth.UserRepo;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import java.util.List;

import com.example.demo.models.auth.UserModel;

@RestController
@RequestMapping("/apikey")
public class UserByApiController {

    private final UserRepo userRepo;

    public UserByApiController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserModel>> getUsers(Authentication authentication) {

        List<UserModel> users = userRepo.findAll();

        return ResponseEntity.ok(users);
    }
}