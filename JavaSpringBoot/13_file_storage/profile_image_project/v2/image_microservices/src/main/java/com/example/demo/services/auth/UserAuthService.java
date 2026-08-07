package com.example.demo.services.auth;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.auth.AuthRequest;
import com.example.demo.models.auth.UserModel;
import com.example.demo.models.auth.UserSessionModel;
import com.example.demo.models.auth.role.Role;
import com.example.demo.models.auth.role.UserRoleModel;
import com.example.demo.repo.auth.UserRepo;
import com.example.demo.repo.auth.UserSessionRepo;
import com.example.demo.repo.auth.role.RoleRepo;
import com.example.demo.repo.auth.role.UserRoleRepo;
import com.example.demo.utils.JwtUtil;
import com.example.demo.helpers.RequestUtils;

import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserSessionRepo sessionRepo;

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

    public Map<String, String> login(AuthRequest request, HttpServletRequest httpRequest) {
        UserModel user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String ipAddress = RequestUtils.getClientIp(httpRequest);
        String userAgent = RequestUtils.getUserAgent(httpRequest);
        String deviceName = RequestUtils.parseDevice(userAgent);

        String sessionId = UUID.randomUUID().toString();

        List<String> roles = userRoleRepo.findRoleNamesByUserId(user.getId());

        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                sessionId,
                roles);

        String refreshToken = jwtUtil.generateRefreshToken(sessionId);

        UserSessionModel session = new UserSessionModel();
        session.setUser(user);
        session.setSessionId(sessionId);
        session.setRefreshToken(refreshToken);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setDeviceName(deviceName);
        session.setLoginAt(LocalDateTime.now());
        session.setLastActivity(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        session.setIsRevoked(false);

        sessionRepo.save(session);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken);    
    }


    @Transactional
public Map<String, Object> getUserInfoByRefreshToken(String bearerToken) {
    if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
        throw new RuntimeException("Missing or invalid Authorization header");
    }

    String oldRefreshToken = bearerToken.substring(7);

    // 1. Fetch active session by refresh token
    UserSessionModel oldSession = sessionRepo.findByRefreshToken(oldRefreshToken)
            .orElseThrow(() -> new RuntimeException("Invalid or expired session"));

    // REUSE DETECTION: If an attacker tries to use an already revoked token
    if (Boolean.TRUE.equals(oldSession.getIsRevoked())) {
        // Security trigger: Revoke ALL sessions for this user due to suspected breach
        sessionRepo.revokeAllSessionsByUserId(oldSession.getUser().getId());
        throw new RuntimeException("Security violation: Refresh token reuse detected. All sessions revoked.");
    }

    if (oldSession.getExpiresAt().isBefore(LocalDateTime.now())) {
        throw new RuntimeException("Session has expired");
    }

    // 2. Fetch associated user
    UserModel user = oldSession.getUser();
    if (Boolean.TRUE.equals(user.getIsDeleted()) || Boolean.FALSE.equals(user.getIsActive())) {
        throw new RuntimeException("User account is disabled or deleted");
    }

    // 3. REVOKE THE OLD SESSION (Single-use enforcement)
    oldSession.setIsRevoked(true);
    oldSession.setExpiresAt(LocalDateTime.now());
    sessionRepo.save(oldSession);

    // 4. CREATE NEW SESSION & TOKENS (Rotation)
    String newSessionId = UUID.randomUUID().toString();
    List<String> roles = userRoleRepo.findRoleNamesByUserId(user.getId());

    String freshAccessToken = jwtUtil.generateAccessToken(
            user.getId(),
            user.getEmail(),
            newSessionId,
            roles);

    String freshRefreshToken = jwtUtil.generateRefreshToken(newSessionId);

    UserSessionModel newSession = new UserSessionModel();
    newSession.setUser(user);
    newSession.setSessionId(newSessionId);
    newSession.setRefreshToken(freshRefreshToken);
    newSession.setIpAddress(oldSession.getIpAddress());
    newSession.setUserAgent(oldSession.getUserAgent());
    newSession.setDeviceName(oldSession.getDeviceName());
    newSession.setLoginAt(oldSession.getLoginAt());
    newSession.setLastActivity(LocalDateTime.now());
    newSession.setExpiresAt(LocalDateTime.now().plusDays(7));
    newSession.setIsRevoked(false);

    sessionRepo.save(newSession);

    // 5. Structure response with BOTH new tokens
    Map<String, Object> userMap = new HashMap<>();
    userMap.put("id", user.getId());
    userMap.put("email", user.getEmail());
    userMap.put("isEmailVerified", user.getIsEmailVerified());
    userMap.put("roles", roles);

    Map<String, Object> response = new HashMap<>();
    response.put("accessToken", freshAccessToken);
    response.put("refreshToken", freshRefreshToken); // MUST return new refresh token
    response.put("user", userMap);

    return response;
}
       @Transactional
    public void logout(Long sessionId) {
        UserSessionModel session = sessionRepo.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setIsRevoked(true);
        sessionRepo.save(session);
    }
}
