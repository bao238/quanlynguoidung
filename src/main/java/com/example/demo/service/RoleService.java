package com.example.demo.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.Role;
import com.example.demo.model.RolePermission;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RolePermissionRepository;
import com.example.demo.repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepo;

    private final PermissionRepository permRepo;

    private final RolePermissionRepository rolePermissionRepository;

    private final RolePermissionService rolePermissionService;

    public RoleService(RoleRepository roleRepo,
                       PermissionRepository permRepo,
                       RolePermissionRepository rolePermissionRepository,
                       RolePermissionService rolePermissionService) {
        this.roleRepo = roleRepo;
        this.permRepo = permRepo;
        this.rolePermissionRepository = rolePermissionRepository;
        this.rolePermissionService = rolePermissionService;
    }

    public Role createRole(Role role) {
        return roleRepo.save(role);
    }

    public java.util.List<Role> findAll() {
        return roleRepo.findAll();
    }

    public Role addPermission(UUID roleId, UUID permId) {
        roleRepo.findById(roleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        permRepo.findById(permId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found"));
        rolePermissionService.assignPermission(roleId, permId);
        return roleRepo.findById(roleId).orElseThrow();
    }

    public java.util.List<Role> findRoles(String q, String id, String name) {
        java.util.List<Role> roles = roleRepo.findAll();
        if (hasText(id)) {
            String idKeyword = id.trim().toLowerCase(Locale.ROOT);
            roles = roles.stream()
                .filter(r -> r.getId() != null
                    && r.getId().toString().toLowerCase(Locale.ROOT).contains(idKeyword))
                .toList();
        }
        if (hasText(name)) {
            String nameKeyword = name.trim().toLowerCase(Locale.ROOT);
            roles = roles.stream()
                .filter(r -> containsIgnoreCase(r.getName(), nameKeyword)
                    || containsIgnoreCase(r.getCode(), nameKeyword))
                .toList();
        }
        if (hasText(q)) {
            String keyword = q.trim().toLowerCase(Locale.ROOT);
            roles = roles.stream()
                .filter(r -> containsIgnoreCase(r.getName(), keyword)
                    || containsIgnoreCase(r.getCode(), keyword)
                    || containsIgnoreCase(r.getDescription(), keyword)
                    || (r.getId() != null
                        && r.getId().toString().toLowerCase(Locale.ROOT).contains(keyword)))
                .toList();
        }
        return roles;
    }

    public Role getRole(UUID roleId) {
        return roleRepo.findById(roleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
    }

    public Role updateRole(UUID roleId, Role body) {
        Role r = getRole(roleId);
        if (body.getCode() != null) {
            r.setCode(body.getCode().trim());
        }
        if (body.getName() != null) {
            r.setName(body.getName().trim());
        }
        if (body.getDescription() != null) {
            r.setDescription(trimToNull(body.getDescription()));
        }
        if (body.getIsSystem() != null) {
            r.setIsSystem(body.getIsSystem());
        }
        if (body.getIsActive() != null) {
            r.setIsActive(body.getIsActive());
        }
        return roleRepo.save(r);
    }

    public void deleteRole(UUID roleId) {
        getRole(roleId);
        java.util.List<RolePermission> links = rolePermissionRepository.findByRoleId(roleId);
        if (!links.isEmpty()) {
            rolePermissionRepository.deleteAll(links);
        }
        roleRepo.deleteById(roleId);
    }

    private boolean containsIgnoreCase(String text, String keywordLowerCase) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(keywordLowerCase);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
