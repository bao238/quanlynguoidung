package com.example.demo.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.model.User;
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
    private final UserAuthorityService userAuthorityService;

    public AuthTokenFilter(AuthTokenService tokenService,
                           UserRepository userRepository,
                           UserAuthorityService userAuthorityService) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.userAuthorityService = userAuthorityService;
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
                            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"),
                            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
                    filterChain.doFilter(request, response);
                    return;
                }
                User u = userRepository.findByIdAndDeletedAtIsNullFetchRoles(userId).orElse(null);
                if (u != null && Boolean.TRUE.equals(u.getIsActive())) {
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                        u.getUsername(),
                        null,
                        userAuthorityService.resolveGrantedAuthorities(u)
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
                }
            }
        }

        filterChain.doFilter(request, response);
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
