package com.manager.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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

    public Object getUsers() {
        String url = authServiceUrl + USERS_PATH;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        String token = null;
        if (attributes != null) {
            token = attributes.getRequest().getHeader("Authorization");
        }

        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            String cleanToken = token.trim();
            headers.set("Authorization", cleanToken);
            // Log de seguridad para depuración
            String logToken = cleanToken.length() > 15 ? cleanToken.substring(0, 15) : cleanToken;
            System.out.println("✅ Reenviando token al Auth-Service: " + logToken + "...");
        }
        
        headers.set("Accept", "application/json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, Object.class).getBody();
        } catch (HttpClientErrorException.Forbidden e) {
            System.err.println("❌ El Auth-Service denegó el acceso (403). El usuario no tiene rol ADMIN.");
            // Lanzamos una excepción que Spring MVC entiende como un 403 real
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador");
        } catch (HttpClientErrorException.Unauthorized e) {
            System.err.println("❌ Token inválido o expirado (401).");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida o expirada");
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al contactar Auth-Service: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error en la comunicación entre servicios");
        }
    }
}