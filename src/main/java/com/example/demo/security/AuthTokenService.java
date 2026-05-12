package com.example.demo.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

    public static final String COOKIE_NAME = "AUTH_TOKEN";

    private static final Duration TTL = Duration.ofHours(12);

    private final Map<String, Entry> tokens = new ConcurrentHashMap<>();
    private final boolean virtualAdminEnabled;
    private final String virtualAdminUsername;
    private final String virtualAdminPassword;
    private final UUID virtualAdminUserId;

    public AuthTokenService(
        @Value("${app.virtual-admin.enabled:true}") boolean virtualAdminEnabled,
        @Value("${app.virtual-admin.username:admin}") String virtualAdminUsername,
        @Value("${app.virtual-admin.password:admin123}") String virtualAdminPassword,
        @Value("${app.virtual-admin.user-id:00000000-0000-0000-0000-000000000001}") String virtualAdminUserId
    ) {
        this.virtualAdminEnabled = virtualAdminEnabled;
        this.virtualAdminUsername = virtualAdminUsername;
        this.virtualAdminPassword = virtualAdminPassword;
        this.virtualAdminUserId = UUID.fromString(virtualAdminUserId);
    }

    public String issueToken(UUID userId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new Entry(userId, Instant.now().plus(TTL)));
        return token;
    }

    public UUID resolveUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Entry entry = tokens.get(token);
        if (entry == null) {
            return null;
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            tokens.remove(token);
            return null;
        }
        return entry.userId();
    }

    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        tokens.remove(token);
    }

    public boolean isVirtualAdminEnabled() {
        return virtualAdminEnabled;
    }

    public String getVirtualAdminUsername() {
        return virtualAdminUsername;
    }

    public UUID getVirtualAdminUserId() {
        return virtualAdminUserId;
    }

    public boolean matchesVirtualAdminLogin(String username, String password) {
        if (!virtualAdminEnabled) {
            return false;
        }
        return username != null
            && password != null
            && virtualAdminUsername.equalsIgnoreCase(username.trim())
            && virtualAdminPassword.equals(password.trim());
    }

    public boolean isVirtualAdminUserId(UUID userId) {
        return virtualAdminEnabled && virtualAdminUserId.equals(userId);
    }

    public boolean isVirtualAdminPrincipal(String principalName) {
        return virtualAdminEnabled
            && principalName != null
            && virtualAdminUsername.equalsIgnoreCase(principalName.trim());
    }

    private record Entry(UUID userId, Instant expiresAt) {}
}

