package com.example.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Student;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    List<Student> findByNameContainingIgnoreCase(String name);
}
