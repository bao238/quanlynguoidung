package com.example.demo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    // @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PostMapping
    public User create(@RequestParam String username,
                       @RequestParam String password) {
        return userService.createUser(username, password);
    }

    // @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PostMapping("/{userId}/roles/{roleId}")
    public User assignRole(@PathVariable UUID userId,
                           @PathVariable UUID roleId) {
        return userService.assignRole(userId, roleId);
    }

    // @PreAuthorize("hasAuthority('USER_MANAGE')")
    @GetMapping
    public List<User> list() {
        return userService.findAll();
    }
}