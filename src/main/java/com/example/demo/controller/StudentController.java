package com.example.demo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Student;
import com.example.demo.service.StudentService;

@RestController
@RequestMapping("/api/students")
@CrossOrigin // cho phép frontend gọi
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    // GET ALL
    @GetMapping
    public List<Student> getAll() {
        return service.getAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public Student getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    // CREATE
    @PostMapping
    public Student create(@RequestBody Student student) {
        return service.create(student);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Student update(@PathVariable UUID id,
                          @RequestBody Student student) {
        return service.update(id, student);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    // SEARCH BY NAME
    @GetMapping("/search")
    public List<Student> search(@RequestParam String name) {
        return service.search(name);
    }

}