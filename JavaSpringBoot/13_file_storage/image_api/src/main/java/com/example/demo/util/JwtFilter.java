package com.example.demo.util;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;


@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil; // Verifies signature using jwt.secret

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // 1. Verify token signature and expiry
                Claims claims = jwtUtil.extractAllClaims(token);
                String tokenType = claims.get("type", String.class);
                
                String targetUserId;

                if ("service".equals(tokenType)) {
                    // --- CALL FROM CORE MICROSERVICE ---
                    // Service identity is trusted; target user ID comes from header
                    targetUserId = request.getHeader("X-User-Id");
                    if (targetUserId == null) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "X-User-Id header missing");
                        return;
                    }
                } else {
                    // --- CALL DIRECTLY FROM USER ---
                    // Target user ID comes straight from the signed JWT payload
                    targetUserId = claims.getSubject();
                }

                // 2. Set Spring Security context with validated user context
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(targetUserId, null, List.of());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}