package com.example.demo.utils;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.repo.auth.UserSessionRepo;
import com.example.demo.services.auth.MyUserDetailService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserSessionRepo sessionRepo;
    @Autowired MyUserDetailService myUserDetailService;
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        // 1. Skip preflight OPTIONS requests from CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(request, response);
            return;
        }
        
        String header = request.getHeader("Authorization");
        
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            if (!jwtUtil.validate(token)) {
                chain.doFilter(request, response);
                return;
            }
            // Extract claims ONCE
            Claims claims = jwtUtil.extractAllClaims(token);
            String userIdStr = claims.getSubject();
            String email = claims.get("email", String.class);
            String sessionId = claims.get("sid", String.class);
            boolean isSessionActive = sessionRepo.existsBySessionIdAndIsRevokedFalse(sessionId);

            if (!isSessionActive) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Session has been revoked or logged out");
                return; // Stop filter chain
            }


            UserDetails userDetails = myUserDetailService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userIdStr,
                    null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
