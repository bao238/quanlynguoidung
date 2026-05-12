package com.example.demo.config;

import com.example.demo.security.AuthTokenService;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI — bám theo cách gọi API của giao diện AdminLTE (cookie phiên đăng nhập).
 */
@Configuration
public class OpenApiConfig {

    /** Tên scheme trong Swagger UI → Authorize */
    public static final String COOKIE_AUTH_SCHEME = "cookieAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Quản lý đào tạo — API (AdminLTE)")
                .description("""
                    Các endpoint dùng bởi trang tĩnh trong `static/` (auth.html, admin-users.html, admin-role-permissions.html, index.html).

                    **Đăng nhập giống AdminLTE:** `POST /api/auth/login` trả body JSON và set cookie HttpOnly `"""
                    + AuthTokenService.COOKIE_NAME + """
                    `. Trình duyệt gửi kèm cookie cho các request tiếp theo.

                    **Thử trên Swagger UI:** sau khi gọi Login, copy giá trị cookie token (DevTools → Application → Cookies) và dán vào **Authorize** → scheme `cookieAuth`.
                    """)
                .version("1.0"))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local (cùng origin với AdminLTE)")
            ))
            .tags(List.of(
                new Tag().name("Auth").description("Đăng ký / đăng nhập / phiên — khớp auth.html và header admin"),
                new Tag().name("Users").description("Quản lý user — khớp admin-users.html (`/api/users`)"),
                new Tag().name("Roles").description("Vai trò — khớp admin-users.html & admin-role-permissions.html"),
                new Tag().name("Permissions").description("Quyền — khớp admin-role-permissions.html"),
                new Tag().name("RolePermissions").description("Gán quyền theo role — khớp admin-role-permissions.html"),
                new Tag().name("Students").description("Sinh viên — khớp index.html (`/api/students`)")
            ))
            .components(new Components()
                .addSecuritySchemes(COOKIE_AUTH_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name(AuthTokenService.COOKIE_NAME)
                        .description(
                            "Token phiên (cùng cookie mà backend set khi login). Tên cookie: `"
                                + AuthTokenService.COOKIE_NAME + "`."
                        )));
    }
}
