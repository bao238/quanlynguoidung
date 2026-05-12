package com.example.demo.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.RolePermission;
import com.example.demo.repository.RolePermissionRepository;

@Service
public class RolePermissionService {

    private final RolePermissionRepository repository;

    public RolePermissionService(RolePermissionRepository repository) {
        this.repository = repository;
    }

    public RolePermission assignPermission(UUID roleId, UUID permissionId) {
        if (repository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            return repository.findByRoleIdAndPermissionId(roleId, permissionId).orElseThrow();
        }
        return repository.save(new RolePermission(roleId, permissionId));
    }

    @Transactional
    public void removePermission(UUID roleId, UUID permissionId) {
        repository.deleteByRoleIdAndPermissionId(roleId, permissionId);
    }

    public List<RolePermission> getPermissionsByRoleId(UUID roleId) {
        return repository.findByRoleId(roleId);
    }

}