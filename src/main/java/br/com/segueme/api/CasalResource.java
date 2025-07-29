package br.com.segueme.api;

import br.com.segueme.entity.Casal;
import br.com.segueme.service.CasalService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/casais")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CasalResource {

    @Inject
    private CasalService casalService;

    @GET
    public List<Casal> listarTodos() {
        return casalService.buscarTodos();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") Long id) {
        Optional<Casal> casal = casalService.buscarPorId(id);
        return casal.map(Response::ok)
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                    .build();
    }

    @GET
    @Path("/{id}/foto")
    @Produces({"image/jpeg", "image/png"})
    public Response buscarFoto(@PathParam("id") Long id) {
        Optional<Casal> casal = casalService.buscarPorId(id);
        if (casal.isPresent() && casal.get().getFoto() != null) {
            String caminho = "C:\\Desenvovilmento\\fotos\\" + casal.get().getFoto();
            java.io.File file = new java.io.File(caminho);
            if (file.exists()) {
                return Response.ok(file).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}