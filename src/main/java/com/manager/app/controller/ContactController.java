package com.manager.app.controller;

import com.manager.app.model.Contact;
import com.manager.app.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "http://localhost:5173") // Habilita el acceso desde el Frontend
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public List<Contact> getAll() {
        return contactService.getAllContacts();
    }

    @PostMapping("/auth/login-proxy")
    public ResponseEntity<String> loginProxy(@RequestBody Object loginRequest) {
        // Nota: Dentro de Docker, "localhost" suele referirse al propio contenedor.
        // Si el proxy falla, usa el nombre del servicio: "http://auth-service:8081/api/auth/login"
        String authServiceUrl = "http://localhost:8081/api/auth/login";
        try {
            return restTemplate.postForEntity(authServiceUrl, loginRequest, String.class);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Error de comunicación con Auth-Service: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Contact> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        try {
            String nuevoEstado = body.get("estado");
            // Llamamos al Service en lugar de al Repository
            Contact actualizado = contactService.updateStatus(id, nuevoEstado);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}