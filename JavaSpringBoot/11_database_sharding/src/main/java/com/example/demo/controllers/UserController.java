package com.example.demo.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.auth.AuthRequest;
import com.example.demo.models.auth.UserModel;
import com.example.demo.repo.auth.UserRepo;



@RestController
@RequestMapping("/auth")
public class UserController {
    // @Autowired
    // private UserAuthService userAuthService;
    @Autowired UserRepo userRepo;
    
 
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Email already exists");
        }
        UserModel user = new UserModel();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());   // Plain text (only for learning)
        userRepo.save(user);
        return ResponseEntity.ok("User registered successfully");
    }
}