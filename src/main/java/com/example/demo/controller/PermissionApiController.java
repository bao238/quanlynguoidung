package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Permission;
import com.example.demo.repository.PermissionRepository;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/permissions")
public class PermissionApiController {

    private final PermissionRepository repo;

    public PermissionApiController(PermissionRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public Permission create(@RequestBody Permission p) {
        return repo.save(p);
    }

    @GetMapping
    public List<Permission> list() {
        return repo.findAll();
    }
}