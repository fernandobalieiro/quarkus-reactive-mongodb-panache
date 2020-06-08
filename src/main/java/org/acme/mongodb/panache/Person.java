package org.acme.mongodb.panache;

import io.quarkus.mongodb.panache.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@MongoEntity(collection="people")
public class Person {
    public ObjectId id;
    public String name;

    @BsonProperty("birth")
    public LocalDateTime birthDate = LocalDateTime.now();

    public Status status;
}
