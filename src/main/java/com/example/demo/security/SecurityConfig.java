package com.example.demo.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthTokenFilter authTokenFilter,
                                           ObjectMapper objectMapper) throws Exception {

        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(eh -> eh.authenticationEntryPoint((req, res, ex) -> handleUnauth(req, res, objectMapper)));
        http.exceptionHandling(eh -> eh.accessDeniedHandler((req, res, ex) -> handleForbidden(req, res, objectMapper)));

        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/",
                "/auth", "/auth.html",
                "/api/auth/**",
                "/swagger-ui/**", "/v3/api-docs/**",
                "/adminlte/**",
                "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.svg", "/**/*.ico"
            ).permitAll()
            // Trang + file tĩnh phân quyền: tạm thời cho phép ROLE_VIEW để xem trang
            // (các thao tác gán/thu hồi quyền vẫn bị chặn theo rule POST/DELETE bên dưới)
            .requestMatchers("/admin/role-permissions", "/admin-role-permissions.html")
                .hasAnyAuthority("ROLE_VIEW", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
            // Trang Token/Session: chỉ cần ROLE_VIEW để xem
            .requestMatchers("/admin/sessions", "/admin-token-sessions.html")
                .hasAnyAuthority("ROLE_VIEW", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
            .requestMatchers("/admin/**").authenticated()

            // Gán / gỡ role cho user: cho phép USER_UPDATE (điều chỉnh thành viên) trước rule POST/DELETE /api/users/** chung
            .requestMatchers(HttpMethod.POST, "/api/users/*/roles/*")
                .hasAnyAuthority("USER_UPDATE", "USER_CREATE", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/users/*/roles/*")
                .hasAnyAuthority("USER_UPDATE", "USER_DELETE", "ROLE_ADMIN")

            .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyAuthority("USER_VIEW", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/users/**").hasAnyAuthority("USER_CREATE", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyAuthority("USER_UPDATE", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyAuthority("USER_DELETE", "ROLE_ADMIN")

            .requestMatchers(HttpMethod.GET, "/api/students/**").hasAnyAuthority("STUDENT_VIEW", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/students/**").hasAnyAuthority("STUDENT_CREATE", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/students/**").hasAnyAuthority("STUDENT_UPDATE", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/students/**").hasAnyAuthority("STUDENT_DELETE", "ROLE_ADMIN")

            .requestMatchers(HttpMethod.GET, "/api/role-permissions/**")
                .hasAnyAuthority("ROLE_VIEW", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/token-sessions")
                .hasAnyAuthority("ROLE_VIEW", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
            // Chỉ ADMIN / SUPER_ADMIN mới được gán / thu hồi quyền theo role
            .requestMatchers(HttpMethod.POST, "/api/role-permissions")
                .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/role-permissions")
                .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/roles/**", "/api/permissions/**")
                .authenticated()
            .requestMatchers("/api/roles/**", "/api/permissions/**")
                .hasAnyRole("ADMIN", "SUPER_ADMIN")

            .requestMatchers("/api/**").authenticated()
            .anyRequest().permitAll()
        );

        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void handleUnauth(HttpServletRequest req, HttpServletResponse res, ObjectMapper objectMapper)
        throws IOException, ServletException {

        String path = req.getRequestURI();
        if (path != null && path.startsWith("/api/")) {
            res.setStatus(401);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(res.getOutputStream(), Map.of("message", "Unauthorized"));
            return;
        }
        res.sendRedirect("/auth");
    }

    private void handleForbidden(HttpServletRequest req, HttpServletResponse res, ObjectMapper objectMapper)
        throws IOException, ServletException {
        String path = req.getRequestURI();
        if (path != null && path.startsWith("/api/")) {
            res.setStatus(403);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(res.getOutputStream(), Map.of("message", "Forbidden"));
            return;
        }
        String cp = req.getContextPath() == null ? "" : req.getContextPath();
        if (path != null
            && ("/admin/role-permissions".equals(path)
                || "/admin-role-permissions.html".equals(path)
                || path.endsWith("/admin-role-permissions.html")
                || "/admin/sessions".equals(path)
                || "/admin-token-sessions.html".equals(path)
                || path.endsWith("/admin-token-sessions.html"))) {
            res.sendRedirect(cp + "/admin/users");
            return;
        }
        res.sendRedirect(cp + "/auth");
    }
}

