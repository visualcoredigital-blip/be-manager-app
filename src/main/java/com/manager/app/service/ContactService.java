package com.manager.app.service;

import com.manager.app.model.Contact;
import com.manager.app.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

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
}
    