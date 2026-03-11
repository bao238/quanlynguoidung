package com.example.demo.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
}
