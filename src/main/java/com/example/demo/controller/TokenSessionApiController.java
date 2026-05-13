package com.example.demo.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.OpenApiConfig;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthTokenService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/token-sessions")
@CrossOrigin
@Tag(name = "TokenSessions")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH_SCHEME)
public class TokenSessionApiController {

    private final AuthTokenService tokenService;
    private final UserRepository userRepository;

    public TokenSessionApiController(AuthTokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    public record ActiveUserView(
        UUID userId,
        String username,
        String fullName,
        LocalDateTime lastLoginAt,
        int activeTokenCount,
        Instant expiresAtEarliest,
        List<String> tokenValues
    ) {}

    public record TokenSessionsResponse(
        int totalActiveTokens,
        List<ActiveUserView> activeUsers
    ) {}

    @GetMapping
    public TokenSessionsResponse listActiveTokenSessions() {
        List<AuthTokenService.ActiveToken> activeTokens = tokenService.listActiveTokens();
        int totalActiveTokens = activeTokens.size();

        Map<UUID, List<AuthTokenService.ActiveToken>> byUser = activeTokens.stream()
            .collect(Collectors.groupingBy(AuthTokenService.ActiveToken::userId));

        List<ActiveUserView> activeUsers = byUser.entrySet().stream()
            .map((entry) -> toActiveUserView(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing((ActiveUserView v) -> v.expiresAtEarliest, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
            .toList();

        return new TokenSessionsResponse(totalActiveTokens, activeUsers);
    }

    private ActiveUserView toActiveUserView(UUID userId, List<AuthTokenService.ActiveToken> tokens) {
        int count = tokens.size();
        Instant earliest = tokens.stream()
            .map(AuthTokenService.ActiveToken::expiresAt)
            .min(Instant::compareTo)
            .orElse(null);

        List<String> tokenValues = tokens.stream()
            .map(AuthTokenService.ActiveToken::token)
            .filter(t -> t != null && !t.isBlank())
            .toList();

        // Virtual admin không có trong DB user thường.
        if (tokenService.isVirtualAdminUserId(userId)) {
            return new ActiveUserView(
                userId,
                tokenService.getVirtualAdminUsername(),
                "Quản lý hệ thống",
                null,
                count,
                earliest,
                tokenValues
            );
        }

        User u = userRepository.findByIdAndDeletedAtIsNull(userId).orElse(null);
        String username = u != null ? u.getUsername() : userId.toString();
        String fullName = u != null ? u.getFullName() : "";
        LocalDateTime lastLoginAt = u != null ? u.getLastLoginAt() : null;

        return new ActiveUserView(userId, username, fullName, lastLoginAt, count, earliest, tokenValues);
    }
}

