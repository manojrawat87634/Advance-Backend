// package com.example.demo.services.auth;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;

// import com.example.demo.models.auth.UserModel;
// import com.example.demo.repo.auth.UserRepo;

// @Service
// public class CustomUserDetailsService implements UserDetailsService {
//     @Autowired
//     private UserRepo userRepo;

//     @Override
//     public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//         UserModel user = userRepo.findByEmail(email)
//                 .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//         return new org.springframework.security.core.userdetails.User(
//                 user.getEmail(),
//                 user.getPassword(),
//                 user.getIsActive(),
//                 true,
//                 true,
//                 !user.getIsDeleted(),
//                 List.of()
//         );
//     }
// }
