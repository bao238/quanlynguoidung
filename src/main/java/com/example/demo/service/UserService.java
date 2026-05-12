package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.ForgotPasswordPayload;
import com.example.demo.dto.LoginPayload;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterPayload;
import com.example.demo.dto.UserPayload;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthTokenService;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final AuthTokenService authTokenService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepo, RoleRepository roleRepo, AuthTokenService authTokenService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.authTokenService = authTokenService;
    }

    public User createUser(UserPayload body) {
        validateCreateBody(body);
        String username = body.username.trim();
        ensureUsernameNotExists(username);
        if (hasText(body.email)) {
            ensureEmailNotExists(body.email.trim());
        }

        LocalDateTime now = LocalDateTime.now();
        User u = new User();
        // SQL Server uniqueidentifier works reliably when we provide UUID explicitly.
        u.setId(UUID.randomUUID());
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(body.password));
        String fullName = trimToNull(body.fullName);
        u.setFullName(fullName != null ? fullName : username);
        u.setEmail(trimToNull(body.email));
        u.setPhone(trimToNull(body.phone));
        u.setAvatarUrl(trimToNull(body.avatarUrl));
        u.setIsActive(body.isActive != null ? body.isActive : Boolean.TRUE);
        u.setCreatedAt(now);
        u.setUpdatedAt(now);
        u.setCreatedBy(normalizeAuditUserId(body.createdBy));
        u.setUpdatedBy(normalizeAuditUserId(body.updatedBy));
        return userRepo.save(u);
    }

    public User updateUser(UUID userId, UserPayload body) {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required");
        }

        User u = findExistingUser(userId);

        if (body.username != null) {
            String nextUsername = body.username.trim();
            if (nextUsername.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be blank");
            }
            if (!nextUsername.equalsIgnoreCase(u.getUsername())
                && userRepo.existsByUsernameIgnoreCaseAndDeletedAtIsNull(nextUsername)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
            }
            u.setUsername(nextUsername);
        }

        if (hasText(body.password)) {
            u.setPasswordHash(passwordEncoder.encode(body.password.trim()));
        }
        if (body.fullName != null) {
            String nextFullName = trimToNull(body.fullName);
            if (nextFullName != null) {
                u.setFullName(nextFullName);
            }
        }
        if (body.email != null) {
            String nextEmail = trimToNull(body.email);
            if (nextEmail != null
                && !nextEmail.equalsIgnoreCase(u.getEmail())
                && userRepo.existsByEmailIgnoreCaseAndDeletedAtIsNull(nextEmail)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }
            u.setEmail(nextEmail);
        }
        if (body.phone != null) {
            u.setPhone(trimToNull(body.phone));
        }
        if (body.avatarUrl != null) {
            u.setAvatarUrl(trimToNull(body.avatarUrl));
        }
        if (body.isActive != null) {
            u.setIsActive(body.isActive);
        }
        if (body.updatedBy != null) {
            u.setUpdatedBy(normalizeAuditUserId(body.updatedBy));
        }
        u.setUpdatedAt(LocalDateTime.now());
        return userRepo.save(u);
    }

    public void deleteUser(UUID userId, String deletedBy) {
        User u = findExistingUser(userId);
        LocalDateTime now = LocalDateTime.now();
        u.setDeletedAt(now);
        u.setDeletedBy(normalizeAuditUserId(deletedBy));
        u.setIsActive(Boolean.FALSE);
        u.setUpdatedAt(now);
        userRepo.save(u);
    }

    public User getUser(UUID userId) {
        return findExistingUser(userId);
    }

    public List<User> findUsers(String q, String id, String name) {
        List<User> users = userRepo.findAllByDeletedAtIsNull();
        if (hasText(id)) {
            String idKeyword = id.trim().toLowerCase(Locale.ROOT);
            if (looksLikeUuid(idKeyword)) {
                users = users.stream()
                    .filter(u -> u.getId() != null && u.getId().toString().equalsIgnoreCase(idKeyword))
                    .toList();
            } else {
                users = users.stream()
                    .filter(u -> u.getId() != null && u.getId().toString().toLowerCase(Locale.ROOT).contains(idKeyword))
                    .toList();
            }
        }
        if (hasText(name)) {
            String nameKeyword = name.trim().toLowerCase(Locale.ROOT);
            users = users.stream()
                .filter(u -> containsIgnoreCase(u.getFullName(), nameKeyword)
                    || containsIgnoreCase(u.getUsername(), nameKeyword))
                .toList();
        }
        if (hasText(q)) {
            String keyword = q.trim().toLowerCase(Locale.ROOT);
            users = users.stream()
                .filter(u -> containsIgnoreCase(u.getFullName(), keyword)
                    || containsIgnoreCase(u.getUsername(), keyword)
                    || containsIgnoreCase(u.getEmail(), keyword)
                    || (u.getId() != null && u.getId().toString().toLowerCase(Locale.ROOT).contains(keyword)))
                .toList();
        }
        return users;
    }

    public User assignRole(UUID userId, UUID roleId) {
        User u = findExistingUser(userId);
        Role r = roleRepo.findById(roleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        if (u.getRoles().contains(r)) {
            return u;
        }
        u.getRoles().add(r);
        u.setUpdatedAt(LocalDateTime.now());
        return userRepo.save(u);
    }

    public User removeRole(UUID userId, UUID roleId, String actorUsername) {
        User u = findExistingUser(userId);
        Role r = roleRepo.findById(roleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        if (isAdminRole(r) && hasText(actorUsername) && actorUsername.trim().equalsIgnoreCase(u.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove ADMIN role from current user");
        }

        boolean removed = u.getRoles().removeIf(role -> role != null && roleId.equals(role.getId()));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not have this role");
        }

        u.setUpdatedAt(LocalDateTime.now());
        return userRepo.save(u);
    }

    public User register(RegisterPayload body) {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required");
        }
        if (!hasText(body.fullName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
        }
        UserPayload payload = new UserPayload();
        payload.username = body.username;
        payload.password = body.password;
        payload.fullName = body.fullName;
        payload.email = body.email;
        payload.phone = body.phone;
        payload.isActive = Boolean.TRUE;
        payload.createdBy = null;
        payload.updatedBy = null;
        return createUser(payload);
    }

    public LoginResponse login(LoginPayload body) {
        if (body == null || !hasText(body.username) || !hasText(body.password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }

        if (authTokenService.matchesVirtualAdminLogin(body.username, body.password)) {
            LoginResponse response = new LoginResponse();
            response.userId = authTokenService.getVirtualAdminUserId();
            response.username = authTokenService.getVirtualAdminUsername();
            response.fullName = "Quản lý hệ thống";
            response.isActive = Boolean.TRUE;
            response.lastLoginAt = LocalDateTime.now();
            response.message = "Login successful";
            return response;
        }

        String account = body.username.trim();
        User user = userRepo.findByUsernameIgnoreCaseAndDeletedAtIsNull(account)
            .or(() -> userRepo.findByEmailIgnoreCaseAndDeletedAtIsNull(account))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is inactive");
        }

        String rawPassword = body.password.trim();
        if (!isPasswordMatched(rawPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginAt(now);
        user.setUpdatedAt(now);

        // Backward compatibility for legacy plain-text passwords.
        if (!looksLikeBCrypt(user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
        }

        userRepo.save(user);

        LoginResponse response = new LoginResponse();
        response.userId = user.getId();
        response.username = user.getUsername();
        response.fullName = user.getFullName();
        response.isActive = user.getIsActive();
        response.lastLoginAt = user.getLastLoginAt();
        response.message = "Login successful";
        return response;
    }

    public void forgotPassword(ForgotPasswordPayload body) {
        if (body == null || !hasText(body.newPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is required");
        }
        validatePassword(body.newPassword.trim(), "New password");

        User user;
        boolean hasUsername = hasText(body.username);
        boolean hasEmail = hasText(body.email);
        if (hasUsername && hasEmail) {
            user = userRepo.findByUsernameIgnoreCaseAndDeletedAtIsNull(body.username.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(body.email.trim())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and email do not match");
            }
        } else if (hasUsername) {
            user = userRepo.findByUsernameIgnoreCaseAndDeletedAtIsNull(body.username.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        } else if (hasEmail) {
            user = userRepo.findByEmailIgnoreCaseAndDeletedAtIsNull(body.email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username or email is required");
        }

        user.setPasswordHash(passwordEncoder.encode(body.newPassword.trim()));
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(null);
        userRepo.save(user);
    }

    private void validateCreateBody(UserPayload body) {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required");
        }
        if (!hasText(body.username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (!hasText(body.password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        validatePassword(body.password.trim(), "Password");
    }

    private User findExistingUser(UUID userId) {
        return userRepo.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void ensureUsernameNotExists(String username) {
        if (userRepo.existsByUsernameIgnoreCaseAndDeletedAtIsNull(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
    }

    private void ensureEmailNotExists(String email) {
        if (userRepo.existsByEmailIgnoreCaseAndDeletedAtIsNull(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
    }

    private boolean containsIgnoreCase(String text, String keywordLowerCase) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(keywordLowerCase);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isPasswordMatched(String rawPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }
        if (!looksLikeBCrypt(storedHash)) {
            return rawPassword.equals(storedHash);
        }
        return passwordEncoder.matches(rawPassword, storedHash);
    }

    private boolean looksLikeBCrypt(String value) {
        return value != null && value.startsWith("$2");
    }

    private void validatePassword(String password, String fieldName) {
        if (password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must be at least 6 characters");
        }
    }

    private boolean looksLikeUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private UUID normalizeAuditUserId(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        if (!looksLikeUuid(trimmed)) {
            return null;
        }
        return UUID.fromString(trimmed);
    }

    private boolean isAdminRole(Role role) {
        return role != null && role.getCode() != null && "ADMIN".equalsIgnoreCase(role.getCode().trim());
    }
}
