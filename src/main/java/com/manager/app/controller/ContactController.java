package com.manager.app.controller;

import com.manager.app.model.Contact;
import com.manager.app.service.ContactService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${MS_AUTH_SERVICE:http://auth-service:8001}")
    private String authServiceUrl;

    private static final String LOGIN_PATH = "/api/auth/login";

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<Page<Contact>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Page<Contact> contacts = contactService.getPaginatedContacts(page, size);
        return ResponseEntity.ok(contacts);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<Map<String, String>> exportToPdf(@RequestBody(required = false) Map<String, Object> filter) {
        
        String exportId = contactService.sendExportEvent(filter);

        Map<String, String> response = new HashMap<>();
        response.put("status", "PROCESSING");
        response.put("message", "La generación del archivo PDF ha iniciado en segundo plano.");
        response.put("exportId", exportId);
        response.put("fileNamePattern", "contacts-" + exportId + "-YYYY-MM-DD-[hash].pdf");

        return ResponseEntity.accepted().body(response);
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
            String loginUrl = authServiceUrl + LOGIN_PATH;
            System.out.println("AUTH URL -> " + loginUrl);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(loginRequest, headers);

            ResponseEntity<Object> response =
                    restTemplate.postForEntity(loginUrl, request, Object.class);

            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());

        } catch (HttpStatusCodeException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

        } catch (Exception e) {
            System.err.println("PROXY ERROR -> " + e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body("Auth service unreachable");
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