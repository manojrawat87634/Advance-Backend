package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.auth.AuthRequest;
import com.example.demo.models.auth.UserModel;
import com.example.demo.services.auth.UserService;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    // Use constructor injection instead of field @Autowired for cleaner code
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest request) {
        // 1. Map incoming DTO properties to your model object
        UserModel user = new UserModel();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // In production, bcrypt this!
        
        // 2. Pass it to the service which decides shard1 or shard2 automatically
        UserModel savedUser = userService.saveUser(user);
        
        return ResponseEntity.ok("User registered successfully via sharding rule! ID: " + savedUser.getId());
    }

    // Optional: Add a quick fetch endpoint to test your sharding retrievals later
    @GetMapping("/user")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        UserModel user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found in any shard.");
        }
        return ResponseEntity.ok(user);
    }
}