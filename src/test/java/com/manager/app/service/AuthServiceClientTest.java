package com.manager.app.service;

import com.manager.app.dto.ResetPasswordDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private AuthServiceClient authServiceClient;

    @BeforeEach
    void setup() {
        authServiceClient = new AuthServiceClient();
        ReflectionTestUtils.setField(authServiceClient, "authServiceUrl", "http://auth-service:8001");
        ReflectionTestUtils.setField(authServiceClient, "restTemplate", restTemplate);
    }

    @Test
    void processForgotPasswordDelegatesToAuthService() {
        when(restTemplate.exchange(eq("http://auth-service:8001/api/users/forgot-password"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("sent", HttpStatus.OK));

        Object result = authServiceClient.processForgotPassword("test@example.com");

        assertEquals("sent", result);
        verify(restTemplate, times(1)).exchange(eq("http://auth-service:8001/api/users/forgot-password"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void getUsersDelegatesToAuthService() {
        when(restTemplate.exchange(eq("http://auth-service:8001/api/users"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("[\"admin\"]", HttpStatus.OK));

        Object result = authServiceClient.getUsers();

        assertEquals("[\"admin\"]", result);
        verify(restTemplate, times(1)).exchange(eq("http://auth-service:8001/api/users"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void processResetPasswordDelegatesToAuthService() {
        ResetPasswordDTO request = new ResetPasswordDTO();
        request.setToken("token123");
        request.setNewPassword("newpassword");

        when(restTemplate.exchange(eq("http://auth-service:8001/api/users/reset-password"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        Object result = authServiceClient.processResetPassword(request);

        assertEquals("ok", result);
        verify(restTemplate, times(1)).exchange(eq("http://auth-service:8001/api/users/reset-password"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void deleteUserDelegatesToAuthService() {
        when(restTemplate.exchange(eq("http://auth-service:8001/api/users/123"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        authServiceClient.deleteUser(123L);
        verify(restTemplate, times(1)).exchange(eq("http://auth-service:8001/api/users/123"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void checkEmailExistsDelegatesToAuthService() {
        when(restTemplate.exchange(eq("http://auth-service:8001/api/users/exists?email=test@example.com"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("true", HttpStatus.OK));

        Object result = authServiceClient.checkEmailExists("test@example.com", null);

        assertEquals("true", result);
        verify(restTemplate, times(1)).exchange(eq("http://auth-service:8001/api/users/exists?email=test@example.com"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void processForgotPasswordThrowsWhenForbidden() {
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Forbidden", HttpHeaders.EMPTY, "forbidden".getBytes(), null);
        when(restTemplate.exchange(eq("http://auth-service:8001/api/users/forgot-password"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(exception);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> authServiceClient.processForgotPassword("test@example.com"));
        verify(restTemplate, times(1)).exchange(eq("http://auth-service:8001/api/users/forgot-password"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }
}
