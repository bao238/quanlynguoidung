package com.example.demo.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findAllByDeletedAtIsNull();
    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdAndDeletedAtIsNullFetchRoles(@Param("id") UUID id);

    Optional<User> findByUsernameIgnoreCaseAndDeletedAtIsNull(String username);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE LOWER(u.username) = LOWER(:username) AND u.deletedAt IS NULL")
    Optional<User> findByUsernameIgnoreCaseAndDeletedAtIsNullFetchRoles(@Param("username") String username);
    Optional<User> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);
    boolean existsByUsernameIgnoreCaseAndDeletedAtIsNull(String username);
    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);
}
