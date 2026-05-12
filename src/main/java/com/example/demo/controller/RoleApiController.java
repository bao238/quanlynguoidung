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

import com.example.demo.config.OpenApiConfig;
import com.example.demo.model.Role;
import com.example.demo.service.RoleService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH_SCHEME)
public class RoleApiController {

    private final RoleService roleService;

    public RoleApiController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public Role create(@RequestBody Role role) {
        return roleService.createRole(role);
    }

    @GetMapping
    public List<Role> list(@RequestParam(required = false) String q,
                             @RequestParam(required = false) String id,
                             @RequestParam(required = false) String name) {
        if (q == null && id == null && name == null) {
            return roleService.findAll();
        }
        return roleService.findRoles(q, id, name);
    }

    @GetMapping("/{roleId}")
    public Role getOne(@PathVariable UUID roleId) {
        return roleService.getRole(roleId);
    }

    @PutMapping("/{roleId}")
    public Role update(@PathVariable UUID roleId, @RequestBody Role body) {
        return roleService.updateRole(roleId, body);
    }

    @DeleteMapping("/{roleId}")
    public void delete(@PathVariable UUID roleId) {
        roleService.deleteRole(roleId);
    }

    @PostMapping("/{roleId}/permissions/{permId}")
    public Role addPermission(@PathVariable UUID roleId,
                              @PathVariable UUID permId) {
        return roleService.addPermission(roleId, permId);
    }
}
