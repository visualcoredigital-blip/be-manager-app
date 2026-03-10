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
}