package com.example.demo.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator   // Hibernate 6+
    @Column(columnDefinition = "UNIQUEIDENTIFIER",
            updatable = false,
            nullable = false)
    private UUID id;

    private String username;
    private String password;
    private String status;

    public User() {}

    public User(String username, String password, String status) {
        this.username = username;
        this.password = password;
        this.status = status;
    }

    // ===== Getter Setter =====
    public UUID getId() {  return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // ===== GETTER =====
    public Set<Role> getRoles() { return roles; }
    // ===== SETTER =====
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}
