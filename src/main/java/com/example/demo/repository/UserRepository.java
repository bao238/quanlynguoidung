package com.example.demo.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findAllByDeletedAtIsNull();
    Optional<User> findByIdAndDeletedAtIsNull(UUID id);
    Optional<User> findByUsernameIgnoreCaseAndDeletedAtIsNull(String username);
    Optional<User> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);
    boolean existsByUsernameIgnoreCaseAndDeletedAtIsNull(String username);
    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);
}
