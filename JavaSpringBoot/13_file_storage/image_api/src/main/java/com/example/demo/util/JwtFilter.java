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
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Skip CORS preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        log.info("===> [MEDIA JWT FILTER] Processing Request: {} {}", request.getMethod(), requestURI);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            try {
                Claims claims = jwtUtil.extractAllClaims(token);
                String tokenType = claims.get("type", String.class);
                String subject = claims.getSubject();

                log.info("===> [MEDIA JWT FILTER] Claims extracted. Subject: '{}', Type: '{}'", subject, tokenType);
                
                String targetUserId = null;

                if ("service".equals(tokenType)) {
                    // Try getting user ID from header first; fall back to JWT subject if missing
                    targetUserId = request.getHeader("X-User-Id");
                    if (targetUserId == null || targetUserId.isBlank()) {
                        log.info("===> [MEDIA JWT FILTER] X-User-Id header missing for service token, falling back to JWT subject");
                        targetUserId = subject;
                    }
                } else {
                    // Standard user token - subject holds the user ID string
                    targetUserId = subject;
                }

                if (targetUserId == null || targetUserId.isBlank()) {
                    log.warn("===> [MEDIA JWT FILTER REJECT] targetUserId is null or empty");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid user identity in token");
                    return;
                }

                // 2. Populate Spring Security Context
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(targetUserId, null, List.of());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("===> [MEDIA JWT FILTER SUCCESS] SecurityContext principal set to: '{}'", targetUserId);

            } catch (Exception e) {
                log.error("===> [MEDIA JWT FILTER ERROR] Token validation failed: {}", e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token: " + e.getMessage());
                return;
            }
        } else {
            log.warn("===> [MEDIA JWT FILTER WARNING] Missing Authorization header for: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }
}