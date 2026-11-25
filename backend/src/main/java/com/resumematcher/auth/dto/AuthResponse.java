package com.resumematcher.auth.dto;

import java.util.UUID;

public class AuthResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private String role;
    private String token;
    private String refreshToken;

    public AuthResponse() {}

    public AuthResponse(UUID userId, String email, String fullName, String role, String token, String refreshToken) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
