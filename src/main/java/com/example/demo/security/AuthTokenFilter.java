package com.example.demo.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RolePermissionRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final AuthTokenService tokenService;
    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public AuthTokenFilter(AuthTokenService tokenService,
                           UserRepository userRepository,
                           RolePermissionRepository rolePermissionRepository,
                           PermissionRepository permissionRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = readCookie(request, AuthTokenService.COOKIE_NAME);
            UUID userId = tokenService.resolveUserId(token);
            if (userId != null) {
                if (tokenService.isVirtualAdminUserId(userId)) {
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                        tokenService.getVirtualAdminUsername(),
                        null,
                        List.of(
                            new SimpleGrantedAuthority("ROLE_USER"),
                            new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
                    filterChain.doFilter(request, response);
                    return;
                }
                User u = userRepository.findByIdAndDeletedAtIsNull(userId).orElse(null);
                if (u != null && Boolean.TRUE.equals(u.getIsActive())) {
                    List<SimpleGrantedAuthority> authorities = buildAuthorities(u);
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                        u.getUsername(),
                        null,
                        authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> buildAuthorities(User user) {
        Set<String> authoritySet = new HashSet<>();
        authoritySet.add("ROLE_USER");

        List<UUID> roleIds = user.getRoles().stream()
            .map(Role::getId)
            .filter(id -> id != null)
            .toList();

        user.getRoles().stream()
            .map(Role::getCode)
            .filter(code -> code != null && !code.isBlank())
            .map(code -> "ROLE_" + code.trim().toUpperCase())
            .forEach(authoritySet::add);

        if (!roleIds.isEmpty()) {
            List<UUID> permissionIds = rolePermissionRepository.findByRoleIdIn(roleIds).stream()
                .map(rp -> rp.getPermissionId())
                .filter(id -> id != null)
                .distinct()
                .toList();

            if (!permissionIds.isEmpty()) {
                permissionRepository.findAllById(permissionIds).stream()
                    .map(Permission::getCode)
                    .filter(code -> code != null && !code.isBlank())
                    .map(code -> code.trim().toUpperCase())
                    .forEach(authoritySet::add);
            }
        }

        return authoritySet.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}

