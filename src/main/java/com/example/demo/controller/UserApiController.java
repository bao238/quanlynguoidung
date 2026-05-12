package com.example.demo.controller;

import java.util.List;
import java.util.UUID;
import java.security.Principal;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.OpenApiConfig;
import com.example.demo.dto.UserPayload;
import com.example.demo.model.User;
import com.example.demo.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH_SCHEME)
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@RequestBody UserPayload body) {
        return userService.createUser(body);
    }

    @PostMapping("/{userId}/roles/{roleId}")
    public User assignRole(@PathVariable UUID userId,
                           @PathVariable UUID roleId) {
        return userService.assignRole(userId, roleId);
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public User removeRole(@PathVariable UUID userId,
                           @PathVariable UUID roleId,
                           Principal principal) {
        String actor = principal != null ? principal.getName() : null;
        return userService.removeRole(userId, roleId, actor);
    }

    @PutMapping("/{userId}")
    public User update(@PathVariable UUID userId,
                       @RequestBody UserPayload body) {
        return userService.updateUser(userId, body);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable UUID userId,
                       @RequestParam(required = false) String deletedBy) {
        userService.deleteUser(userId, deletedBy);
    }

    @GetMapping
    public List<User> list(@RequestParam(required = false) String q,
                           @RequestParam(required = false) String id,
                           @RequestParam(required = false) String name) {
        return userService.findUsers(q, id, name);
    }

    @GetMapping("/{userId}")
    public User getOne(@PathVariable UUID userId) {
        return userService.getUser(userId);
    }
}
