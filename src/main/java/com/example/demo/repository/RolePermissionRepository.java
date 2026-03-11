package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.RolePermission;
import com.example.demo.model.RolePermissionId;

import java.util.UUID;
import java.util.List;

public interface RolePermissionRepository 
        extends JpaRepository<RolePermission, RolePermissionId> {

    List<RolePermission> findByRoleId(UUID roleId);

    void deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId);
}