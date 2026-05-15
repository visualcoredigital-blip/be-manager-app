package com.manager.app.service;

import com.manager.app.model.Contact;
import com.manager.app.repository.ContactRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    @Test
    void getPaginatedContactsReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("fecha").descending());
        Contact contact = new Contact();
        contact.setId("1");
        contact.setNombre("Prueba");
        contact.setFecha(new Date());

        Page<Contact> expectedPage = new PageImpl<>(Collections.singletonList(contact), pageable, 1);
        when(contactRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Contact> actualPage = contactService.getPaginatedContacts(0, 10);

        assertNotNull(actualPage);
        assertEquals(1, actualPage.getTotalElements());
        assertEquals("Prueba", actualPage.getContent().get(0).getNombre());
        verify(contactRepository, times(1)).findAll(pageable);
    }

    @Test
    void updateStatusUpdatesExistingContact() {
        Contact contact = new Contact();
        contact.setId("1");
        contact.setEstado("nuevo");

        when(contactRepository.findById("1")).thenReturn(Optional.of(contact));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contact updated = contactService.updateStatus("1", "enviado");

        assertNotNull(updated);
        assertEquals("enviado", updated.getEstado());
        verify(contactRepository, times(1)).findById("1");
        verify(contactRepository, times(1)).save(contact);
    }

    @Test
    void updateStatusThrowsWhenContactNotFound() {
        when(contactRepository.findById("2")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> contactService.updateStatus("2", "enviado"));

        assertTrue(exception.getMessage().contains("Contacto no encontrado"));
        verify(contactRepository, times(1)).findById("2");
        verify(contactRepository, never()).save(any());
    }
}
