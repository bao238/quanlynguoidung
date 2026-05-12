package com.example.demo.repository;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByCodeIgnoreCase(String code);
}
