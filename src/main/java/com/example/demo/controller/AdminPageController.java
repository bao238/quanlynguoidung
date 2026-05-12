package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping("/admin")
    public String admin() {
        return "forward:/admin.html";
    }

    @GetMapping("/admin/users")
    public String users() {
        return "forward:/admin-users.html";
    }

    @GetMapping("/admin/role-permissions")
    public String rolePermissions() {
        return "forward:/admin-role-permissions.html";
    }

    @GetMapping("/auth")
    public String authPage() {
        return "forward:/auth.html";
    }
}
