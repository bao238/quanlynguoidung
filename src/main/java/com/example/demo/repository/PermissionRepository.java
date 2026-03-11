package com.example.demo.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Permission;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
}
