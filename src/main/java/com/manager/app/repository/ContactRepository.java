package com.manager.app.repository;

import com.manager.app.model.Contact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends MongoRepository<Contact, String> {
    // MongoDB usa String para los IDs por defecto (ObjectIDs)
}