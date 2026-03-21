package com.manager.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class EmailService {

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${EMAIL_USER}")
    private String emailUser;

    @Value("${APP_FRONTEND_URL}")
    private String frontendUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    public void sendPasswordRecoveryEmail(String destinationEmail, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", brevoApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construcción del cuerpo del mensaje para Brevo
        Map<String, Object> body = new HashMap<>();
        
        // Configuración del remitente
        Map<String, String> sender = new HashMap<>();
        sender.put("name", "VisualCoreDigital");
        sender.put("email", emailUser); 
        body.put("sender", sender);

        // Configuración del destinatario
        Map<String, String> to = new HashMap<>();
        to.put("email", destinationEmail);
        body.put("to", Collections.singletonList(to));

        body.put("subject", "Recuperación de Contraseña - VisualCoreDigital");
        
        // Link dinámico: frontendUrl vendrá de tu .env
        // Nota: Asegúrate de que el path coincida con tu ruta en React
        String link = frontendUrl + "/reset-password?token=" + token;
        
        body.put("htmlContent", 
            "<div style='font-family: Arial, sans-serif; color: #333;'>" +
            "<h3>Hola,</h3>" +
            "<p>Has solicitado restablecer tu contraseña en <strong>VisualCoreDigital</strong>. Haz clic en el siguiente botón para continuar:</p>" +
            "<div style='margin: 30px 0;'>" +
            "  <a href='" + link + "' style='padding: 12px 25px; background-color: #007bff; color: #ffffff; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>" +
            "    Restablecer Contraseña" +
            "  </a>" +
            "</div>" +
            "<p style='font-size: 0.9em; color: #666;'>Si el botón no funciona, copia y pega este enlace en tu navegador:</p>" +
            "<p style='font-size: 0.8em; color: #007bff;'>" + link + "</p>" +
            "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>" +
            "<p style='font-size: 0.8em; color: #999;'>Si no solicitaste este cambio, puedes ignorar este correo de forma segura.</p>" +
            "</div>");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            System.out.println("📧 Enviando correo de recuperación a: " + destinationEmail);
            restTemplate.postForEntity(BREVO_URL, entity, String.class);
            System.out.println("✅ Correo enviado correctamente a través de Brevo.");
        } catch (Exception e) {
            System.err.println("❌ Error al conectar con Brevo: " + e.getMessage());
        }
    }
}