package br.com.segueme.api;

import br.com.segueme.entity.Equipe;
import br.com.segueme.service.EquipeService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/equipes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EquipeResource {

    @Inject
    private EquipeService equipeService;

    @GET
    public List<Equipe> listarTodos() {
        return equipeService.buscarTodos();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") Long id) {
        Optional<Equipe> equipe = equipeService.buscarPorId(id);
        return equipe.map(Response::ok)
                     .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                     .build();
    }
}