package com.manager.app.service;

import com.manager.app.model.Contact;
import com.manager.app.repository.ContactRepository;
import com.google.cloud.spring.pubsub.core.PubSubTemplate; // Import de Google Cloud
import com.fasterxml.jackson.databind.ObjectMapper;       // Para convertir a JSON

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private PubSubTemplate pubSubTemplate; // Inyectamos la herramienta de Pub/Sub

    @Autowired
    private ObjectMapper objectMapper;     // Inyectamos el mapeador JSON

    private static final String TOPIC_NAME = "contact-export-topic";

    public Page<Contact> getPaginatedContacts(int page, int size) {
        // Ordenamos por fecha descendente para que los nuevos aparezcan primero
        Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        return contactRepository.findAll(pageable);
    }

    public List<Contact> getAllContacts() {
        List<Contact> contacts = contactRepository.findAll();
        return contacts;
    }

    public Contact updateStatus(String id, String nuevoEstado) {
        // Buscamos el contacto por ID
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contacto no encontrado con ID: " + id));
        
        // Cambiamos el estado
        contact.setEstado(nuevoEstado);
        
        // Guardamos y retornamos
        return contactRepository.save(contact);
    }

    public String sendExportEvent(Map<String, Object> filter) {
        // Generamos un identificador base único corto para el reporte (ej: req-a1b2c3d4)
        String exportId = "req-" + UUID.randomUUID().toString().substring(0, 8);

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("exportId", exportId);
            payload.put("filter", filter != null ? filter : new HashMap<>());

            String jsonMessage = objectMapper.writeValueAsString(payload);

            this.pubSubTemplate.publish(TOPIC_NAME, jsonMessage);
            System.out.println("🚀 Evento encolado en Pub/Sub de forma exitosa. ID: " + exportId);

            return exportId;
        } catch (Exception e) {
            System.err.println("❌ Fallo crítico al enviar evento a Pub/Sub: " + e.getMessage());
            throw new RuntimeException("No se pudo iniciar el proceso de generación del PDF", e);
        }
    }
}