package com.manager.app.service;

import com.manager.app.dto.PdfRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class CloudPdfService {

    @Value("${cloud.service.pdf.url}")
    private String cloudServiceUrl;

    private final RestTemplate restTemplate;

    public CloudPdfService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generarReporteContactos(String exportId, Map<String, Object> filtros) {
        // 1. Instanciamos el DTO con la acción fija que espera Node.js
        PdfRequestDTO requestPayload = new PdfRequestDTO("DOWNLOAD_PDF", exportId, filtros);

        // 2. Configurar las cabeceras HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. Empaquetar la petición
        HttpEntity<PdfRequestDTO> entity = new HttpEntity<>(requestPayload, headers);

        try {
            // 4. Realizar la llamada POST a la URL de Cloud Run
            ResponseEntity<String> response = restTemplate.postForEntity(cloudServiceUrl, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            // Log de error técnico en consola de Spring Boot
            System.err.println("❌ Error en comunicación con Cloud Run: " + e.getMessage());
            throw new RuntimeException("No se pudo procesar el reporte en la nube de Google: " + e.getMessage());
        }
    }
}