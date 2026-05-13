package com.example.demo.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RolePermissionRepository;

/**
 * Tính danh sách quyền từ {@link User} + bảng role_permissions (một nguồn dùng cho filter và /api/auth/me).
 */
@Service
public class UserAuthorityService {

    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public UserAuthorityService(RolePermissionRepository rolePermissionRepository,
                              PermissionRepository permissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<String> resolveAuthorityStrings(User user) {
        return resolveGrantedAuthorities(user).stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a != null && !a.isBlank())
            .distinct()
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<GrantedAuthority> resolveGrantedAuthorities(User user) {
        Set<String> authoritySet = new HashSet<>();
        authoritySet.add("ROLE_USER");

        if (user == null || user.getRoles() == null) {
            return authoritySet.stream()
                .map(SimpleGrantedAuthority::new)
                .map(ga -> (GrantedAuthority) ga)
                .toList();
        }

        List<UUID> roleIds = user.getRoles().stream()
            .map(Role::getId)
            .filter(id -> id != null)
            .toList();

        user.getRoles().stream()
            .map(Role::getCode)
            .filter(code -> code != null && !code.isBlank())
            .map(code -> "ROLE_" + code.trim().toUpperCase())
            .forEach(authoritySet::add);

        if (!roleIds.isEmpty()) {
            List<UUID> permissionIds = rolePermissionRepository.findByRoleIdIn(roleIds).stream()
                .map(rp -> rp.getPermissionId())
                .filter(id -> id != null)
                .distinct()
                .toList();

            if (!permissionIds.isEmpty()) {
                permissionRepository.findAllById(permissionIds).stream()
                    .map(Permission::getCode)
                    .filter(code -> code != null && !code.isBlank())
                    .map(code -> code.trim().toUpperCase())
                    .forEach(authoritySet::add);
            }
        }

        return authoritySet.stream()
            .map(SimpleGrantedAuthority::new)
            .map(ga -> (GrantedAuthority) ga)
            .toList();
    }
}
