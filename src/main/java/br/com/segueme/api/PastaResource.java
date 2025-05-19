package br.com.segueme.api;

import br.com.segueme.entity.Pasta;
import br.com.segueme.service.PastaService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/pastas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PastaResource {

    @Inject
    private PastaService pastaService;

    @GET
    public List<Pasta> listarTodos() {
        return pastaService.buscarTodos();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") Long id) {
        Optional<Pasta> pasta = pastaService.buscarPorId(id);
        return pasta.map(Response::ok)
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                    .build();
    }
}