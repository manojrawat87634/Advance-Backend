// package com.example.demo.controllers;

// import java.util.Map;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import com.example.demo.dto.auth.AuthRequest;
// import com.example.demo.services.auth.UserAuthService;

// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.validation.Valid;

// /**
//  * UserProfileController
//  */

// @RestController
// @RequestMapping("/user-profile")
// public class UserProfileController {

//     @Autowired
//     private UserAuthService userAuthService;

//     @GetMapping("/get-user-info")
//     public ResponseEntity<?> getUserInfo(
//             @Valid @RequestBody AuthRequest request) {

//         userAuthService.getUserInfo(request);

//         return ResponseEntity.ok(
//                 Map.of("message", "User registered successfully!"));
//     }

//     @PostMapping("/logout")
//     public ResponseEntity<String> logout(@RequestParam Long sessionId) {
//         userAuthService.logout(sessionId);
//         return ResponseEntity.ok("Logged out successfully");
//     }
// }