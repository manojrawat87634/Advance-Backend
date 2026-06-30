package com.example.demo.apiKey.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.apiKey.entity.ApiKey;
import com.example.demo.apiKey.repository.ApiKeyRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Only handle /apikey/**
        if (!request.getServletPath().startsWith("/apikey")) {
    filterChain.doFilter(request, response);
    return;
}

        // Already authenticated?
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("x-api-key");
        System.out.println(apiKey);
        if (apiKey == null || apiKey.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "API Key is required");
            return;
        }

        ApiKey savedKey = apiKeyRepository.findByApiKey(apiKey)
        .orElse(null);
        
        System.out.println(savedKey);
        if (savedKey == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid API Key");
            return;
        }

        if (!savedKey.getActive()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "API Key is inactive");
            return;
        }
        
        // Optional expiration check
        if (savedKey.getExpiresAt() != null &&
        savedKey.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "API Key expired");
            return;
        }

        UserDetails userDetails =
        userDetailsService.loadUserByUsername(
            savedKey.getUser().getEmail());
            
            System.out.println(userDetails.getUsername());
            UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        System.out.println("Hii ");
        filterChain.doFilter(request, response);
    }
}