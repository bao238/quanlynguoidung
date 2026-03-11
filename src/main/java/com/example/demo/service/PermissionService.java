package com.example.demo.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.demo.model.Permission;
import com.example.demo.repository.PermissionRepository;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepo;

    public PermissionService(PermissionRepository permissionRepo) {
        this.permissionRepo = permissionRepo;
    }

    /**
     * Tạo permission mới
     */
    public Permission create(Permission permission) {
        return permissionRepo.save(permission);
    }

    /**
     * Lấy danh sách permission
     */
    public List<Permission> findAll() {
        return permissionRepo.findAll();
    }

    /**
     * Tìm permission theo id
     */
    public Permission findById(UUID id) {
        return permissionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
    }

    /**
     * Xóa permission
     */
    public void delete(UUID id) {
        permissionRepo.deleteById(id);
    }
}