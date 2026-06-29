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
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
        throws ServletException, IOException {

    String path = request.getServletPath();

    String key;
    long capacity;
    Duration duration;

    if (path.startsWith("/auth")) {
        key = "auth:" + getClientIp(request);
        capacity = 5;
        duration = Duration.ofDays(1);

    } else if (path.startsWith("/admin")) {
        key = "admin:" + getClientIp(request);
        capacity = 100;
        duration = Duration.ofDays(1);

    } else if (path.startsWith("/student")) {
        key = "student:" + getClientIp(request);
        capacity = 60;
        duration = Duration.ofDays(1);

    } else {
        filterChain.doFilter(request, response);
        return;
    }

    if (!rateLimitService.isAllowed(key, capacity, duration)) {
        response.setStatus(429);
        response.getWriter().write("Rate limit exceeded.");
        return;
    }

    filterChain.doFilter(request, response);
}

private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");

    if (forwarded != null && !forwarded.isBlank()) {
        return forwarded.split(",")[0];
    }

    return request.getRemoteAddr();
}
}