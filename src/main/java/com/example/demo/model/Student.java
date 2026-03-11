package com.example.demo.model;

import jakarta.persistence.*;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue
    @UuidGenerator   // Hibernate 6+
    @Column(columnDefinition = "UNIQUEIDENTIFIER",
            updatable = false,
            nullable = false)
    private UUID id;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String email;
        // ===== Constructor =====
    public Student() {}

    public Student(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // ===== Getter Setter =====
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
