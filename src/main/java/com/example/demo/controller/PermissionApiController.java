package com.example.demo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Permission;
import com.example.demo.service.PermissionService;

@RestController
@RequestMapping("/api/permissions")
public class PermissionApiController {

    private final PermissionService permissionService;

    public PermissionApiController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    public Permission create(@RequestBody Permission p) {
        return permissionService.create(p);
    }

    @GetMapping
    public List<Permission> list(@RequestParam(required = false) String q,
                                 @RequestParam(required = false) String id,
                                 @RequestParam(required = false) String name) {
        if (q == null && id == null && name == null) {
            return permissionService.findAll();
        }
        return permissionService.findPermissions(q, id, name);
    }

    @GetMapping("/{permissionId}")
    public Permission getOne(@PathVariable UUID permissionId) {
        return permissionService.getPermission(permissionId);
    }

    @PutMapping("/{permissionId}")
    public Permission update(@PathVariable UUID permissionId, @RequestBody Permission body) {
        return permissionService.updatePermission(permissionId, body);
    }

    @DeleteMapping("/{permissionId}")
    public void delete(@PathVariable UUID permissionId) {
        permissionService.deletePermission(permissionId);
    }
}
