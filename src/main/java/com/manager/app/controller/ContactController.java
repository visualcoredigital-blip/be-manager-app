package com.manager.app.controller;

import com.manager.app.model.Contact;
import com.manager.app.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // <--- IMPORTANTE
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
// Eliminamos @CrossOrigin de aquí porque ya lo manejamos globalmente en SecurityConfig
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private RestTemplate restTemplate;

    // Leemos la URL del Auth Service desde las variables de entorno
    // Si no existe (en Docker local), usa el valor por defecto
    @Value("${MS_AUTH_SERVICE:http://auth-service:8001/api/auth/login}")
    private String authServiceUrl;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public List<Contact> getAll() {
        return contactService.getAllContacts();
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
    public ResponseEntity<?> loginProxy(@RequestBody Map<String, Object> loginRequest) {
        try {
            System.out.println("DEBUG: Enviando POST a: " + authServiceUrl);
            return restTemplate.postForEntity(authServiceUrl, loginRequest, Object.class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // Esto captura errores 401, 403, 500 del Auth Service
            System.err.println("AUTH SERVICE DIJO: " + e.getRawStatusCode());
            System.err.println("BODY: " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            // Esto captura Timeouts o fallos de conexión
            System.err.println("FALLO CRÍTICO PROXY: " + e.getMessage());
            return ResponseEntity.status(504).body("Servicio Auth no disponible");
        }
    }
    @GetMapping("/public/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    // Esto es para que Render reciba un "OK" cuando haga el test de vida
    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Backend Manager is Running");
    }
}