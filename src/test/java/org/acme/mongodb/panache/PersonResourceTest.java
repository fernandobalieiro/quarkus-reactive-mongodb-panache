package org.acme.mongodb.panache;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import jakarta.inject.Inject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.acme.mongodb.panache.Status.ACTIVE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@QuarkusTest
@TestInstance(PER_CLASS)
class PersonResourceTest {

    @Inject
    PersonRepository personRepository;

    private Person person1;
    private Person person2;
    private Person personToDelete;

    @BeforeAll
    void setup() {
        person1 = new Person();
        person1.name = "Person 1";
        person1.status = ACTIVE;

        person2 = new Person();
        person2.name = "Person 2";
        person2.status = ACTIVE;

        personToDelete = new Person();
        personToDelete.name = "Person To Delete";
        personToDelete.status = ACTIVE;

        List<Person> people = new ArrayList<>();
        people.add(person1);
        people.add(person2);
        people.add(personToDelete);

        personRepository.persist(people).await().atMost(Duration.ofSeconds(2));
    }

    @AfterAll
    void tearDown() {
        personRepository.deleteAll().await().atMost(Duration.ofSeconds(2));
    }

    @Test
    void testGetAllEndpoint() {
        given()
                .when().get("/people")
                .then()
                .statusCode(200)
                .body("$.size()", is(2));
    }

    @Test
    void testGetByIdEndpoint() {
        given()
                .when().get("/people/{id}", person1.id.toString())
                .then()
                .statusCode(200)
                .body("name", is("Person 1"));
    }

    @Test
    void testCreateEndpoint() {
        Map<String, String> payload = new HashMap<>();
        payload.put("name", "Person 3");
        payload.put("status", "ACTIVE");

        given()
                .body(payload)
                .contentType(JSON)
                .when().post("/people")
                .then()
                .statusCode(201)
                .body("name", is("Person 3"));
    }

    @Test
    void testUpdateEndpoint() {
        Map<String, String> payload = new HashMap<>();
        payload.put("name", "Person 2 Updated");
        payload.put("status", "ACTIVE");

        given()
                .body(payload)
                .contentType(JSON)
                .when().put("/people/{id}", person2.id.toString())
                .then()
                .statusCode(200)
                .body("name", is("Person 2 Updated"));
    }

    @Test
    void testDeleteEndpoint() {
        given()
                .contentType(JSON)
                .when().delete("/people/{id}", personToDelete.id.toString())
                .then()
                .statusCode(200);
    }

}
