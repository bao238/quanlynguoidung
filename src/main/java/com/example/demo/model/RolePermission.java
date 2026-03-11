package com.example.demo.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "role_permissions")
@IdClass(RolePermissionId.class)
public class RolePermission {

    @Id
    @Column(name = "role_id")
    private UUID roleId;

    @Id
    @Column(name = "permission_id")
    private UUID permissionId;

    public RolePermission() {}
    public RolePermission(UUID roleId, UUID permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    // ===== Getter Setter =====
    public UUID getRoleId() { return roleId; }
    public void setRoleId(UUID roleId) { this.roleId = roleId; }

    public UUID getPermissionId() { return permissionId; }
    public void setPermissionId(UUID permissionId) { this.permissionId = permissionId; }

}