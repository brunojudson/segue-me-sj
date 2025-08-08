package br.com.segueme.api;

import br.com.segueme.entity.Encontro;
import br.com.segueme.service.EncontroService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/encontros")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EncontroResource {

    @Inject
    private EncontroService encontroService;

    @GET
    public List<Encontro> listarTodos() {
        return encontroService.buscarTodos();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") Long id) {
        Optional<Encontro> encontro = encontroService.buscarPorId(id);
        return encontro
                .map(e -> Response.ok(e).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }
}