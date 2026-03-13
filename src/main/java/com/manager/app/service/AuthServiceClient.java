package com.manager.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceClient {

    @Value("${MS_AUTH_SERVICE:http://auth-service:8001}")
    private String authServiceUrl;
    
    private static final String USERS_PATH = "/api/users";
    private final RestTemplate restTemplate = new RestTemplate();

    // --- MÉTODOS PÚBLICOS ---

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

    /**
     * Centraliza la lógica de intercambio, headers y manejo de errores.
     */
    private Object executeRequest(String path, HttpMethod method, Object body, String actionName) {
        String url = authServiceUrl + path;
        HttpHeaders headers = createHeaders();
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        try {
            System.out.println("🚀 Llamando a Auth-Service [" + method + "] " + path);
            
            // Usamos String.class para recibir la respuesta cruda y evitar errores de parseo JSON
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
            
            // Si el método es DELETE o la respuesta es exitosa pero vacía, devolvemos null o el body
            if (method == HttpMethod.DELETE || response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            return response.getBody();

        } catch (HttpClientErrorException.Forbidden e) {
            logError(actionName, "403 Forbidden");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para " + actionName);
        } catch (HttpClientErrorException.Unauthorized e) {
            logError(actionName, "401 Unauthorized");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida o expirada");
        } catch (Exception e) {
            // Log detallado del error para saber exactamente qué falló
            System.err.println("❌ Detalle técnico del error: " + e.getMessage());
            logError(actionName, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al " + actionName);
        }
    }

    /**
     * Extrae automáticamente el token del contexto de la petición actual.
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            String token = attributes.getRequest().getHeader("Authorization");
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", token.trim());
                // Log opcional para debug
                String logToken = token.length() > 15 ? token.substring(0, 15) : token;
                System.out.println("🔑 Token propagado: " + logToken + "...");
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