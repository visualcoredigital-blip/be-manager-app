package com.manager.app.controller;

import com.manager.app.service.AuthServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthServiceClient authServiceClient;

    public UserController(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUsers() {
        Object users = authServiceClient.getUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody Object createUserRequest) {
        // Usamos Object o un DTO específico para recibir el JSON del frontend
        Object response = authServiceClient.createUser(createUserRequest);
        return ResponseEntity.ok(response);
    }    

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        authServiceClient.deleteUser(id);
        return ResponseEntity.ok().build(); // Devolvemos un 200 limpio
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Object updateUserRequest) {
        Object response = authServiceClient.updateUser(id, updateUserRequest);
        return ResponseEntity.ok(response);
    }    
}