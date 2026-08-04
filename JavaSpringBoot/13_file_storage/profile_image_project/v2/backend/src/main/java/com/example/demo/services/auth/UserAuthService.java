package com.example.demo.services.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.auth.AuthRequest;
import com.example.demo.models.auth.UserModel;
import com.example.demo.models.auth.role.Role;
import com.example.demo.models.auth.role.UserRoleModel;
import com.example.demo.repo.auth.UserRepo;
import com.example.demo.repo.auth.role.RoleRepo;
import com.example.demo.repo.auth.role.UserRoleRepo;

import jakarta.transaction.Transactional;

@Service
public class UserAuthService {
    @Autowired 
    private UserRepo userRepo;
    @Autowired 
    private RoleRepo roleRepo;
    @Autowired 
    private UserRoleRepo userRoleRepo;
    @Autowired 
    private PasswordEncoder passwordEncoder;
    

    @Transactional
    public void registerUser(AuthRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        UserModel user = new UserModel();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepo.save(user);

        Role role = roleRepo.findByName("student")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
                UserRoleModel userRole = new UserRoleModel();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepo.save(userRole);
    }
}
