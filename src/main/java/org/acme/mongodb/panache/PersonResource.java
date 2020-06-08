package org.acme.mongodb.panache;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;
import org.jboss.resteasy.annotations.SseElementType;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {

    @Inject
    PersonRepository personRepository;

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON)
    public Multi<Person> streamPersons() {
        return personRepository.streamAll();
    }

    @GET
    public Uni<List<Person>> get() {
        return personRepository.findAll().list();
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(@PathParam("id") final String id) {
        return personRepository.findById(new ObjectId(id))
                .onItem().apply(person -> person != null && person.id != null ? Response.ok(person) : Response.status(NOT_FOUND))
                .onItem().apply(Response.ResponseBuilder::build);
    }

    @POST
    public Uni<Response> create(final Person person) {
        return personRepository.persist(person)
                .onItem().apply(id -> URI.create("/persons/" + id))
                .onItem().apply(uri -> Response.created(uri).entity(person).build());
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(@PathParam("id") final String id, final Person person) {
        person.id = new ObjectId(id);

        return personRepository.update(person)
                .onItem().apply(item -> person.id != null ? Response.ok(person) : Response.status(NOT_FOUND))
                .onItem().apply(Response.ResponseBuilder::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@PathParam("id") String id) {
        return personRepository.deleteById(new ObjectId(id))
                .onItem().apply(status -> Response.status(Response.Status.OK).build());
    }
}
