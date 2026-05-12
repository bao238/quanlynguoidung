package com.example.demo.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.Permission;
import com.example.demo.repository.PermissionRepository;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepo;

    public PermissionService(PermissionRepository permissionRepo) {
        this.permissionRepo = permissionRepo;
    }

    public java.util.List<Permission> findAll() {
        return permissionRepo.findAll();
    }

    public java.util.List<Permission> findPermissions(String q, String id, String name) {
        java.util.List<Permission> list = permissionRepo.findAll();
        if (hasText(id)) {
            String idKeyword = id.trim().toLowerCase(Locale.ROOT);
            list = list.stream()
                .filter(p -> p.getId() != null
                    && p.getId().toString().toLowerCase(Locale.ROOT).contains(idKeyword))
                .toList();
        }
        if (hasText(name)) {
            String nameKeyword = name.trim().toLowerCase(Locale.ROOT);
            list = list.stream()
                .filter(p -> containsIgnoreCase(p.getName(), nameKeyword)
                    || containsIgnoreCase(p.getCode(), nameKeyword))
                .toList();
        }
        if (hasText(q)) {
            String keyword = q.trim().toLowerCase(Locale.ROOT);
            list = list.stream()
                .filter(p -> containsIgnoreCase(p.getName(), keyword)
                    || containsIgnoreCase(p.getCode(), keyword)
                    || containsIgnoreCase(p.getDescription(), keyword)
                    || containsIgnoreCase(p.getModule(), keyword)
                    || (p.getId() != null
                        && p.getId().toString().toLowerCase(Locale.ROOT).contains(keyword)))
                .toList();
        }
        return list;
    }

    public Permission getPermission(UUID id) {
        return permissionRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found"));
    }

    public Permission create(Permission p) {
        return permissionRepo.save(p);
    }

    public Permission updatePermission(UUID id, Permission body) {
        Permission p = getPermission(id);
        if (body.getCode() != null) {
            p.setCode(body.getCode().trim());
        }
        if (body.getName() != null) {
            p.setName(body.getName().trim());
        }
        if (body.getModule() != null) {
            p.setModule(trimToNull(body.getModule()));
        }
        if (body.getDescription() != null) {
            p.setDescription(trimToNull(body.getDescription()));
        }
        if (body.getIsActive() != null) {
            p.setIsActive(body.getIsActive());
        }
        return permissionRepo.save(p);
    }

    public void deletePermission(UUID id) {
        Permission p = getPermission(id);
        permissionRepo.delete(p);
    }

    private boolean containsIgnoreCase(String text, String keywordLowerCase) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(keywordLowerCase);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
