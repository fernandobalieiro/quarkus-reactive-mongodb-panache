package org.acme.mongodb.panache;

import java.net.URI;
import java.util.List;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.jboss.resteasy.annotations.SseElementType;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.SERVER_SENT_EVENTS;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/people")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class PersonResource {

    private final PersonRepository personRepository;

    public PersonResource(final PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GET
    @Path("/stream")
    @Produces(SERVER_SENT_EVENTS)
    @SseElementType(APPLICATION_JSON)
    public Multi<Person> stream() {
        return personRepository.streamAll();
    }

    @GET
    public Uni<List<Person>> get() {
        return personRepository.findAll().list();
    }

    @GET
    @Path("{id}")
    public Uni<Response> getById(@PathParam("id") final String id) {
        return personRepository.findById(new ObjectId(id))
                .onItem().transform(person -> person != null && person.id != null ? Response.ok(person) : Response.status(NOT_FOUND))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @POST
    public Uni<Response> create(final Person person) {
        return personRepository.persist(person)
                .onItem().transform(e -> URI.create("/people/" + e.id))
                .onItem().transform(uri -> Response.created(uri).entity(person).build());
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(@PathParam("id") final String id, final Person person) {
        person.id = new ObjectId(id);

        return personRepository.update(person)
                .onItem().transform(item -> person.id != null ? Response.ok(person) : Response.status(NOT_FOUND))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@PathParam("id") final String id) {
        return personRepository.deleteById(new ObjectId(id))
                .onItem().transform(status -> Response.ok().build());
    }
}
