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
            .requestMatchers("/admin/**").authenticated()

            .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyAuthority("USER_VIEW", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/users/**").hasAnyAuthority("USER_CREATE", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyAuthority("USER_UPDATE", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyAuthority("USER_DELETE", "ROLE_ADMIN")

            .requestMatchers(HttpMethod.GET, "/api/students/**").hasAnyAuthority("STUDENT_VIEW", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/students/**").hasAnyAuthority("STUDENT_CREATE", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/students/**").hasAnyAuthority("STUDENT_UPDATE", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/students/**").hasAnyAuthority("STUDENT_DELETE", "ROLE_ADMIN")

            .requestMatchers(HttpMethod.GET, "/api/roles/**", "/api/permissions/**", "/api/role-permissions/**")
                .authenticated()
            .requestMatchers("/api/roles/**", "/api/permissions/**", "/api/role-permissions/**")
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
        res.sendRedirect("/auth");
    }
}

