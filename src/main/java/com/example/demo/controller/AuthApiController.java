package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.OpenApiConfig;
import com.example.demo.dto.ForgotPasswordPayload;
import com.example.demo.dto.LoginPayload;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.MessageResponse;
import com.example.demo.dto.RegisterPayload;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthTokenService;
import com.example.demo.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthApiController {

    private final UserService userService;
    private final AuthTokenService tokenService;
    private final UserRepository userRepository;

    public AuthApiController(UserService userService, AuthTokenService tokenService, UserRepository userRepository) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    @SecurityRequirements
    public User register(@RequestBody RegisterPayload body) {
        return userService.register(body);
    }

    @PostMapping("/login")
    @SecurityRequirements
    public LoginResponse login(@RequestBody LoginPayload body, HttpServletResponse response) {
        LoginResponse login = userService.login(body);

        String token = tokenService.issueToken(login.userId);
        Cookie cookie = new Cookie(AuthTokenService.COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 12);
        response.addCookie(cookie);

        return login;
    }

    @PostMapping("/forgot-password")
    @SecurityRequirements
    public MessageResponse forgotPassword(@RequestBody ForgotPasswordPayload body) {
        userService.forgotPassword(body);
        return new MessageResponse("Password reset successful");
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH_SCHEME)
    public MessageResponse logout(HttpServletRequest request, HttpServletResponse response) {
        String token = readCookie(request, AuthTokenService.COOKIE_NAME);
        tokenService.revoke(token);

        Cookie cookie = new Cookie(AuthTokenService.COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return new MessageResponse("Logged out");
    }

    @org.springframework.web.bind.annotation.GetMapping("/me")
    @SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH_SCHEME)
    public LoginResponse me(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                "Unauthorized"
            );
        }
        if (tokenService.isVirtualAdminPrincipal(principal.getName())) {
            LoginResponse response = new LoginResponse();
            response.userId = tokenService.getVirtualAdminUserId();
            response.username = tokenService.getVirtualAdminUsername();
            response.fullName = "Quản lý hệ thống";
            response.isActive = Boolean.TRUE;
            response.lastLoginAt = null;
            response.authorities = currentAuthorities();
            response.message = "OK";
            return response;
        }
        User u = userRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull(principal.getName())
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                "Unauthorized"
            ));

        LoginResponse response = new LoginResponse();
        response.userId = u.getId();
        response.username = u.getUsername();
        response.fullName = u.getFullName();
        response.isActive = u.getIsActive();
        response.lastLoginAt = u.getLastLoginAt();
        response.authorities = currentAuthorities();
        response.message = "OK";
        return response;
    }

    private List<String> currentAuthorities() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return List.of();
        }
        return auth.getAuthorities().stream()
            .map(a -> a.getAuthority())
            .filter(a -> a != null && !a.isBlank())
            .distinct()
            .toList();
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
