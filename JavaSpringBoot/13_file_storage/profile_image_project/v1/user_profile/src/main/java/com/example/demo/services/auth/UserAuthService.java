package com.example.demo.services.auth;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.example.demo.dto.auth.AuthRequest;
import com.example.demo.dto.mediaDto.PresignRequestDto;
import com.example.demo.dto.mediaDto.PresignResponseDto;
import com.example.demo.helpers.RequestUtils;
import com.example.demo.models.auth.UserModel;
import com.example.demo.models.auth.UserProfile;
import com.example.demo.models.auth.UserSessionModel;
import com.example.demo.models.auth.role.RoleModel;
import com.example.demo.models.auth.role.UserRoleModel;
import com.example.demo.repo.auth.UserProfileRepository;
import com.example.demo.repo.auth.UserRepo;
import com.example.demo.repo.auth.UserSessionRepo;
import com.example.demo.repo.auth.role.RoleRepo;
import com.example.demo.repo.auth.role.UserRoleRepo;
import com.example.demo.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserAuthService {
    private static final Logger log = LoggerFactory.getLogger(UserAuthService.class);
    private final UserRepo userRepo;
    private final UserSessionRepo sessionRepo;
    private final RoleRepo roleRepo;
    private final UserRoleRepo userRoleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserProfileRepository userProfileRepository;

    public UserAuthService(
            UserRepo userRepo,
            UserSessionRepo sessionRepo,
            UserProfileRepository userProfileRepository,
            RoleRepo roleRepo,
            UserRoleRepo userRoleRepo,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.userProfileRepository = userProfileRepository;
        this.sessionRepo = sessionRepo;
        this.roleRepo = roleRepo;
        this.userRoleRepo = userRoleRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${media.service.url:http://localhost:8081}")
    private String mediaServiceUrl;
    // @Autowired
    // private MediaServiceClient mediaServiceClient;

    /**
     * Session validation & user info retrieval.
     * Takes the incoming refresh token, validates the session, updates last
     * activity,
     * generates a fresh access token, and returns user profile details.
     */
    @Transactional
    public Map<String, Object> getUserInfoByRefreshToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String refreshToken = bearerToken.substring(7);

        // 1. Fetch active session by refresh token
        UserSessionModel session = sessionRepo.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid or expired session"));

        if (Boolean.TRUE.equals(session.getIsRevoked())) {
            throw new RuntimeException("Session has been revoked");
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Session has expired");
        }

        // 2. Fetch associated user
        UserModel user = session.getUser();
        if (Boolean.TRUE.equals(user.getIsDeleted()) || Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("User account is disabled or deleted");
        }

        // 3. Update last activity on session
        session.setLastActivity(LocalDateTime.now());
        sessionRepo.save(session);

        // 4. Generate fresh access token
        List<String> roles = userRoleRepo.findRoleNamesByUserId(user.getId());
        String freshAccessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                session.getSessionId(),
                roles);

        // 5. Structure user object response to match frontend expectations
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("isEmailVerified", user.getIsEmailVerified());
        userMap.put("roles", roles);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", freshAccessToken);
        response.put("user", userMap);

        return response;
    }

    @Transactional
    public void registerUser(AuthRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        UserModel user = new UserModel();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepo.save(user);

        RoleModel role = roleRepo.findByName("student")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        UserRoleModel userRole = new UserRoleModel();
        userRole.setUser(user);
        userRole.setRole(role);

        userRoleRepo.save(userRole);
    }

    @Transactional
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

 // STEP 1: Forward user's Bearer token to Media Service /api/v1/media/presign-upload
    public PresignResponseDto getPresignedUrlFromMediaService(PresignRequestDto request, String bearerToken) {
        log.info("===> [STEP 1] Presign Request Initiated");
        log.info("Target URL: {}/api/v1/media/presign-upload", mediaServiceUrl);
        log.info("Request Body Payload -> fileName: '{}', mimeType: '{}', fileSize: {}", 
                 request.getFileName(), request.getMimeType(), request.getFileSize());
        log.info("Authorization Header present: {}", bearerToken != null ? "YES" : "NO");

        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(mediaServiceUrl)
                    .build();

            PresignResponseDto response = restClient.post()
                    .uri("/api/v1/media/presign-upload")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .body(request)
                    .retrieve()
                    .body(PresignResponseDto.class);

            log.info("===> [STEP 1 SUCCESS] Received Media ID: {}", response != null ? response.getMediaId() : "NULL");
            return response;

        } catch (HttpClientErrorException e) {
            log.error("===> [STEP 1 ERROR] Media Service returned Client Error status: {} - Body: {}", 
                      e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (HttpServerErrorException e) {
            log.error("===> [STEP 1 ERROR] Media Service returned Server Error status: {} - Body: {}", 
                      e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("===> [STEP 1 ERROR] Unexpected error while calling Media Service: {}", e.getMessage(), e);
            throw e;
        }
    }

   // STEP 3: Forward user's Bearer token to Media Service /api/v1/media/{mediaId}/confirm
@Transactional
public void updateUserProfileImage(Long userId, Long mediaId, String bearerToken) { // ✅ Change mediaId from String to Long
    log.info("===> [STEP 3] Update Profile Image Initiated for User ID: {}, Media ID: {}", userId, mediaId);
    log.info("Target URL: {}/api/v1/media/{}/confirm", mediaServiceUrl, mediaId);

    try {
        RestClient restClient = RestClient.builder()
                .baseUrl(mediaServiceUrl)
                .build();

        restClient.post()
                .uri("/api/v1/media/{mediaId}/confirm", mediaId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .toBodilessEntity();

        log.info("===> [STEP 3 SUCCESS] Media Service confirmed upload for Media ID: {}", mediaId);

    } catch (HttpClientErrorException e) {
        log.error("===> [STEP 3 ERROR] Media Service confirmation failed status: {} - Body: {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
        throw e;
    } catch (Exception e) {
        log.error("===> [STEP 3 ERROR] Failed to confirm media upload: {}", e.getMessage(), e);
        throw e;
    }

    // Save mediaId to user profile database
    log.info("Saving mediaId: {} to UserProfile DB for userId: {}", mediaId, userId);
    UserProfile profile = userProfileRepository.findById(userId)
            .orElseGet(() -> {
                log.warn("UserProfile not found for userId: {}. Creating a new record...", userId);
                UserModel user = userRepo.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
                
                UserProfile newProfile = new UserProfile();
                newProfile.setUser(user); // ✅ Links user entity to generate ID
                return newProfile;
            });

    profile.setProfileMediaId(mediaId);
    userProfileRepository.save(profile); // ✅ Will now succeed!
    log.info("===> [STEP 3 COMPLETE] Profile successfully updated in database for userId: {}", userId);
}
    @Transactional
    public void logout(Long sessionId) {
        UserSessionModel session = sessionRepo.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setIsRevoked(true);
        sessionRepo.save(session);
    }
}