package com.manager.app.controller;

import com.manager.app.service.CloudPdfService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacts/reports")
@CrossOrigin(origins = "*") 
public class ContactPdfController {

    private final CloudPdfService cloudPdfService;

    public ContactPdfController(CloudPdfService cloudPdfService) {
        this.cloudPdfService = cloudPdfService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> solicitarReporte(@RequestBody Map<String, Object> payload) {
        Map<String, String> response = new HashMap<>();
        
        String exportIdSimplificado = "vcd";

        @SuppressWarnings("unchecked")
        Map<String, Object> filter = (Map<String, Object>) payload.getOrDefault("filter", new HashMap<>());

        try {
            String resultadoCloud = cloudPdfService.generarReporteContactos(exportIdSimplificado, filter);
            
            response.put("status", "success");
            response.put("message", resultadoCloud);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}