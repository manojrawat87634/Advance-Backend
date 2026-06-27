package com.example.demo.config;

import java.io.IOException;
import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.services.redis.RateLimitService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        // LOGIN RATE LIMIT
        if (path.equals("/auth/login")) {
            String ip = request.getRemoteAddr();

            boolean allowed = rateLimitService.isAllowed(
                    "rl:login:ip:" + ip,
                    5,
                    Duration.ofMinutes(10));

            if (!allowed) {
                response.setStatus(429);
                response.getWriter().write("Too many login attempts");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}