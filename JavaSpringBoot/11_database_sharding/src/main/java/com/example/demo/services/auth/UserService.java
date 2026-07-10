package com.example.demo.services.auth;

import org.springframework.stereotype.Service;

import com.example.demo.models.auth.UserModel;
import com.example.demo.repo.auth.userRepo.UserRepoShard1;
import com.example.demo.repo.auth.userRepo.UserRepoShard2;

@Service
public class UserService {

    private final UserRepoShard1 shard1UserRepo;
    private final UserRepoShard2 shard2UserRepo;

    public UserService(UserRepoShard1 shard1UserRepo, UserRepoShard2 shard2UserRepo) {
        this.shard1UserRepo = shard1UserRepo;
        this.shard2UserRepo = shard2UserRepo;
    }

    public UserModel saveUser(UserModel user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required for sharding evaluation!");
        }

        // Get the first character, convert to lowercase
        char firstLetter = user.getEmail().toLowerCase().charAt(0);

        // Route: a-m -> Shard 1 | Everything else (n-z, numbers, symbols) -> Shard 2
        if (firstLetter >= 'a' && firstLetter <= 'm') {
            System.out.println("Routing " + user.getEmail() + " to SHARD 1");
            return shard1UserRepo.save(user);
        } else {
            System.out.println("Routing " + user.getEmail() + " to SHARD 2");
            return shard2UserRepo.save(user);
        }
    }

    public UserModel getUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }

        char firstLetter = email.toLowerCase().charAt(0);
        if (firstLetter >= 'a' && firstLetter <= 'm') {
            // .orElse(null) extracts the UserModel out of the Optional wrapper
            return shard1UserRepo.findByEmail(email).orElse(null);
        } else {
            // Do the same here
            return shard2UserRepo.findByEmail(email).orElse(null);
        }
    }
}