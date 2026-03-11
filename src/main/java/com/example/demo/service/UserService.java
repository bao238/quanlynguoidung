package com.example.demo.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepo;

    private final RoleRepository roleRepo;

    // @Autowired
    // private PasswordEncoder encoder;

    public UserService(UserRepository userRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    public User createUser(String username, String password) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setStatus("ACTIVE");
        
        return userRepo.save(u);
    }

    public User assignRole(UUID userId, UUID roleId) {
        User u = userRepo.findById(userId).orElseThrow();
        Role r = roleRepo.findById(roleId).orElseThrow();
        u.getRoles().add(r);
        return userRepo.save(u);
    }

    public List<User> findAll() {
        return userRepo.findAll();
    }
}

