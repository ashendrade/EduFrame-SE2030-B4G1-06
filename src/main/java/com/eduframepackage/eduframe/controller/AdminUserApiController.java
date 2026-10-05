package com.eduframepackage.eduframe.controller;

import com.eduframepackage.eduframe.quiz.entity.Role;
import com.eduframepackage.eduframe.quiz.entity.User;
import com.eduframepackage.eduframe.quiz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserApiController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> payload) {
        try {
            String fullName = payload.get("fullName");
            String email = payload.get("email");
            String password = payload.get("password");
            String roleStr = payload.getOrDefault("role", "STUDENT");

            Role role = Role.valueOf(roleStr.toUpperCase());
            User created = userService.register(fullName, email, password, role);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserFullDetails(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        try {
            String fullName = payload.get("fullName");
            String email = payload.get("email");
            String password = payload.get("password");
            String roleStr = payload.get("role");

            Role role = (roleStr != null && !roleStr.isEmpty()) ? Role.valueOf(roleStr.toUpperCase()) : null;
            User updated = userService.adminUpdateUser(id, fullName, email, password, role);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        try {
            String roleStr = payload.get("role");
            Role role = Role.valueOf(roleStr.toUpperCase());
            User updated = userService.updateRole(id, role);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        try {
            userService.deleteUser(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
