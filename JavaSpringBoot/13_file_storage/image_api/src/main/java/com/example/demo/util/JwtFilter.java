package com.example.demo.util;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtil jwtUtil; // Verifies signature using jwt.secret

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        log.info("===> [MEDIA JWT FILTER] Processing Request: {} {}", request.getMethod(), requestURI);
        log.info("===> [MEDIA JWT FILTER] Authorization Header: {}", authHeader != null ? (authHeader.substring(0, Math.min(20, authHeader.length())) + "...") : "MISSING");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            try {
                // 1. Verify token signature and expiry
                Claims claims = jwtUtil.extractAllClaims(token);
                String tokenType = claims.get("type", String.class);
                String subject = claims.getSubject();

                log.info("===> [MEDIA JWT FILTER] Claims extracted successfully. Subject: '{}', Type: '{}'", subject, tokenType);
                
                String targetUserId;

                if ("service".equals(tokenType)) {
                    // --- CALL FROM CORE MICROSERVICE ---
                    targetUserId = request.getHeader("X-User-Id");
                    log.info("===> [MEDIA JWT FILTER] Service Token Detected. X-User-Id Header: '{}'", targetUserId);

                    if (targetUserId == null) {
                        log.warn("===> [MEDIA JWT FILTER REJECT] X-User-Id header missing for service token");
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "X-User-Id header missing");
                        return;
                    }
                } else {
                    // --- CALL DIRECTLY FROM USER / FORWARDED USER TOKEN ---
                    targetUserId = subject;
                    log.info("===> [MEDIA JWT FILTER] User Token Detected. Using Subject as targetUserId: '{}'", targetUserId);
                }

                if (targetUserId == null || targetUserId.isEmpty()) {
                    log.warn("===> [MEDIA JWT FILTER REJECT] targetUserId evaluated to NULL or empty string");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid user identity in token");
                    return;
                }

                // 2. Set Spring Security context with validated user context
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(targetUserId, null, List.of());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("===> [MEDIA JWT FILTER SUCCESS] SecurityContext set for principal: '{}'", targetUserId);

            } catch (Exception e) {
                log.error("===> [MEDIA JWT FILTER ERROR] JWT parsing/validation failed! Reason: {}", e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token: " + e.getMessage());
                return;
            }
        } else {
            log.warn("===> [MEDIA JWT FILTER WARNING] No valid 'Authorization: Bearer' header found for request to {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }
}