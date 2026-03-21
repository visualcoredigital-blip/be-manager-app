package com.manager.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manager.app.dto.ResetPasswordDTO;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceClient {

    @Value("${MS_AUTH_SERVICE}")
    private String authServiceUrl;
    
    private static final String USERS_PATH = "/api/users";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper(); // Para procesar el JSON de respuesta

    @Autowired
    private EmailService emailService; // INYECCIÓN NECESARIA

    public Object processForgotPassword(String email) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        
        // 1. Llamada al MS-Auth para validar y generar el token
        Object response = executeRequest(USERS_PATH + "/forgot-password", HttpMethod.POST, body, "procesar recuperación");
        
        try {
            // 2. Extraer el token de la respuesta String que devuelve executeRequest
            // Asumimos que el MS-Auth responde algo como: {"token": "xyz123...", "message": "..."}
            JsonNode root = objectMapper.readTree(response.toString());
            String tokenParaEmail = root.path("token").asText();

            if (tokenParaEmail != null && !tokenParaEmail.isEmpty()) {
                // 3. Enviar el correo usando el nuevo servicio
                emailService.sendPasswordRecoveryEmail(email, tokenParaEmail);
            }
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo extraer el token o enviar el email: " + e.getMessage());
            // Opcional: podrías lanzar una excepción aquí si el envío es crítico
        }
        
        return response;
    }

    public Object processResetPassword(ResetPasswordDTO request) {
        // Reutilizamos executeRequest que ya maneja URL, cabeceras y errores
        return executeRequest(USERS_PATH + "/reset-password", HttpMethod.POST, request, "restablecer contraseña");
    }

    public Object getUsers() {
        return executeRequest(USERS_PATH, HttpMethod.GET, null, "obtener usuarios");
    }

    public Object createUser(Object createUserRequest) {
        return executeRequest(USERS_PATH + "/create", HttpMethod.POST, createUserRequest, "crear usuario");
    }

    public void deleteUser(Long id) {
        executeRequest(USERS_PATH + "/" + id, HttpMethod.DELETE, null, "eliminar usuario");
    }

    public Object updateUser(Long id, Object updateUserRequest) {
        return executeRequest(USERS_PATH + "/" + id, HttpMethod.PUT, updateUserRequest, "editar usuario");
    }

    private Object executeRequest(String path, HttpMethod method, Object body, String actionName) {
        String url = authServiceUrl + path;
        HttpHeaders headers = createHeaders();
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        try {
            System.out.println("🚀 Llamando a Auth-Service [" + method + "] " + path);
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
            return response.getBody();

        } catch (HttpClientErrorException.Forbidden e) {
            logError(actionName, "403 Forbidden");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para " + actionName);
        } catch (HttpClientErrorException.Unauthorized e) {
            logError(actionName, "401 Unauthorized");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida o expirada");
        } catch (Exception e) {
            System.err.println("❌ Detalle técnico del error: " + e.getMessage());
            logError(actionName, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al " + actionName);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            try {
                String token = attributes.getRequest().getHeader("Authorization");
                if (token != null && !token.isEmpty()) {
                    headers.set("Authorization", token.trim());
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo extraer el token de la cabecera.");
            }
        }
        
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private void logError(String action, String message) {
        System.err.println("❌ Error al " + action + ": " + message);
    }

}