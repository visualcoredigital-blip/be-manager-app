package com.manager.app.service;

import com.manager.app.model.Contact;
import com.manager.app.repository.ContactRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public List<Contact> getAllContacts() {
        // LOG DE PRUEBA
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
}
    