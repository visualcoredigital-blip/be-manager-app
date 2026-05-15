package com.manager.app.controller;

import com.manager.app.model.Contact;
import com.manager.app.service.ContactService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    @Mock
    private ContactService contactService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ContactController contactController;

    @Test
    void getAllReturnsPaginatedContacts() {
        Contact contact = new Contact();
        contact.setId("1");
        contact.setNombre("Contacto");
        contact.setFecha(new Date());

        Page<Contact> page = new PageImpl<>(Collections.singletonList(contact), PageRequest.of(0, 50, Sort.by("fecha").descending()), 1);
        when(contactService.getPaginatedContacts(0, 50)).thenReturn(page);

        ResponseEntity<Page<Contact>> response = contactController.getAll(0, 50);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("Contacto", response.getBody().getContent().get(0).getNombre());
        verify(contactService, times(1)).getPaginatedContacts(0, 50);
    }

    @Test
    void updateStatusReturnsUpdatedContact() {
        Contact contact = new Contact();
        contact.setId("abc");
        contact.setEstado("nuevo");

        when(contactService.updateStatus(eq("abc"), eq("enviado"))).thenReturn(contact);

        Map<String, String> body = new HashMap<>();
        body.put("estado", "enviado");

        ResponseEntity<Contact> response = contactController.updateStatus("abc", body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(contact, response.getBody());
        verify(contactService, times(1)).updateStatus("abc", "enviado");
    }

    @Test
    void loginProxyReturnsAuthServiceResponse() {
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("username", "test");
        loginRequest.put("password", "pass");

        ResponseEntity<Object> authResponse = new ResponseEntity<>(Collections.singletonMap("token", "abc"), HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class))).thenReturn(authResponse);

        ResponseEntity<?> response = contactController.loginProxy(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void loginProxyReturnsBadGatewayWhenAuthServiceFails() {
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("username", "test");
        loginRequest.put("password", "pass");

        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.BAD_GATEWAY, "Bad Gateway", HttpHeaders.EMPTY, "error".getBytes(), null);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class))).thenThrow(exception);

        ResponseEntity<?> response = contactController.loginProxy(loginRequest);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("error", response.getBody());
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void healthCheckReturnsOk() {
        ResponseEntity<String> response = contactController.healthCheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
    }

    @Test
    void homeReturnsRunningMessage() {
        ResponseEntity<String> response = contactController.home();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Backend Manager is Running", response.getBody());
    }
}
