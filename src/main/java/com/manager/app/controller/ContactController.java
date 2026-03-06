package com.manager.app.controller;

import com.manager.app.model.Contact;
import com.manager.app.service.ContactService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${MS_AUTH_SERVICE:http://auth-service:8001/api/auth/login}")
    private String authServiceUrl;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
    public List<Contact> getAll() {
        return contactService.getAllContacts();
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Contact> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {

        try {
            String nuevoEstado = body.get("estado");
            Contact actualizado = contactService.updateStatus(id, nuevoEstado);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/auth/login-proxy")
    public ResponseEntity<?> loginProxy(@RequestBody Map<String, Object> loginRequest) {

        try {

            System.out.println("AUTH URL -> " + authServiceUrl);

            return restTemplate.postForEntity(
                    authServiceUrl,
                    loginRequest,
                    Object.class
            );

        } catch (HttpStatusCodeException e) {

            System.err.println("AUTH STATUS: " + e.getStatusCode());
            System.err.println("AUTH BODY: " + e.getResponseBodyAsString());

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

        } catch (Exception e) {

            System.err.println("AUTH CONNECTION ERROR: " + e.getMessage());

            return ResponseEntity
                    .status(504)
                    .body("Auth service no disponible");
        }
    }

    @GetMapping("/public/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Backend Manager is Running");
    }
}