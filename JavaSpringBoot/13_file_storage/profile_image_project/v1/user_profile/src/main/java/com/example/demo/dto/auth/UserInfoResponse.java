package com.example.demo.dto.auth;
import java.util.List;

public class UserInfoResponse {
    private Long id;
    private String email;
    private Boolean isEmailVerified;
    private List<String> roles;
    private String sessionId;

    public UserInfoResponse(Long id, String email, Boolean isEmailVerified, List<String> roles, String sessionId) {
        this.id = id;
        this.email = email;
        this.isEmailVerified = isEmailVerified;
        this.roles = roles;
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public List<String> getRoles() { return roles; }
    public String getSessionId() { return sessionId; }
}