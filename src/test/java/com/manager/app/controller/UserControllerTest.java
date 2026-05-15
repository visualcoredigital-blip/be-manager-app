package com.manager.app.controller;

import com.manager.app.dto.ResetPasswordDTO;
import com.manager.app.service.AuthServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private UserController userController;

    @Test
    void forgotPasswordDelegatesToAuthServiceClient() {
        Map<String, String> body = new HashMap<>();
        body.put("email", "test@example.com");
        when(authServiceClient.processForgotPassword("test@example.com")).thenReturn("sent");

        ResponseEntity<?> response = userController.forgotPassword(body);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("sent", response.getBody());
        verify(authServiceClient, times(1)).processForgotPassword("test@example.com");
    }

    @Test
    void getUsersReturnsListFromAuthServiceClient() {
        List<String> users = List.of("admin", "user");
        when(authServiceClient.getUsers()).thenReturn(users);

        ResponseEntity<?> response = userController.getUsers();

        assertEquals(200, response.getStatusCodeValue());
        assertSame(users, response.getBody());
        verify(authServiceClient, times(1)).getUsers();
    }

    @Test
    void deleteUserReturnsOkWhenDelegated() {
        doNothing().when(authServiceClient).deleteUser(123L);

        ResponseEntity<?> response = userController.deleteUser(123L);

        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(authServiceClient, times(1)).deleteUser(123L);
    }

    @Test
    void resetPasswordDelegatesToAuthServiceClient() {
        ResetPasswordDTO request = new ResetPasswordDTO();
        request.setToken("token123");
        request.setNewPassword("newpassword");

        when(authServiceClient.processResetPassword(request)).thenReturn("ok");

        ResponseEntity<?> response = userController.resetPassword(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("ok", response.getBody());
        verify(authServiceClient, times(1)).processResetPassword(request);
    }
}

