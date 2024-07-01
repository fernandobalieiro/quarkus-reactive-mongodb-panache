package org.acme.mongodb.panache;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PersonRepository implements ReactivePanacheMongoRepository<Person> {
}
