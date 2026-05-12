package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LoginResponse {
    public UUID userId;
    public String username;
    public String fullName;
    public Boolean isActive;
    public LocalDateTime lastLoginAt;
    public List<String> authorities;
    public String message;
}
