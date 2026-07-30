package com.example.demo.util;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

   @Value("${jwt.access-token-expiry:900000}") // Default 15 mins (1000 * 60 * 15)
    private long ACCESS_TOKEN_EXPIRY; // 15 minutes

   @Value("${jwt.refresh-token-expiry:900000}") // Default 15 mins (1000 * 60 * 15)
    private long REFRESH_TOKEN_EXPIRY; // 7 days

    @Value("${jwt.secret}")
    private String SECRET;
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // ---------------- ACCESS TOKEN ----------------

    public String generateAccessToken(
        Long userId,
        String email,
        String sessionId,
        List<String> roles) {

    Date now = new Date();

    return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("email", email)
            .claim("sid", sessionId)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRY))
            .signWith(getSignKey())
            .compact();
}

    // ---------------- REFRESH TOKEN ----------------

    public String generateRefreshToken(String sessionId) {

        Date now = new Date();

        return Jwts.builder()
                .claim("sid", sessionId)
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRY))
                .signWith(getSignKey())
                .compact();
    }

    // ---------------- COMMON ----------------

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Integer extractUserId(String token) {
        return Integer.parseInt(
                extractAllClaims(token).getSubject()
        );
    }

    public String extractSessionId(String token) {
        return extractAllClaims(token).get("sid", String.class);
    }

@SuppressWarnings("unchecked")
public List<String> extractRoles(String token) {
    Object roles = extractAllClaims(token).get("roles");

    if (roles instanceof List<?>) {
        return (List<String>) roles;
    }

    return List.of();
}
    public String extractJti(String token) {
        return extractAllClaims(token).get("jti", String.class);
    }

    public boolean isExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    public boolean validate(String token) {
        return !isExpired(token);
    }
}