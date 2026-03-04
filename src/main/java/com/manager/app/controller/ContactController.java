package com.manager.app.controller;

import com.manager.app.model.Contact;
import com.manager.app.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <--- NUEVO IMPORT
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "http://localhost:5173")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private RestTemplate restTemplate;

    // 1. Ambos roles pueden ver la lista
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public List<Contact> getAll() {
        return contactService.getAllContacts();
    }

    // 2. Solo el ADMIN puede cambiar el estado
    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // <--- RESTRICCIÓN AQUÍ
    public ResponseEntity<Contact> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        try {
            String nuevoEstado = body.get("estado");
            Contact actualizado = contactService.updateStatus(id, nuevoEstado);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/auth/login-proxy")
    public ResponseEntity<String> loginProxy(@RequestBody Object loginRequest) {
        String authServiceUrl = "http://localhost:8081/api/auth/login";
        try {
            return restTemplate.postForEntity(authServiceUrl, loginRequest, String.class);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Error de comunicación: " + e.getMessage());
        }
    }
}