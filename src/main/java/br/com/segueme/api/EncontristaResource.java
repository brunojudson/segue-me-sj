package br.com.segueme.api;

import br.com.segueme.entity.Encontrista;
import br.com.segueme.service.EncontristaService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/encontristas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EncontristaResource {

    @Inject
    private EncontristaService encontristaService;

    @GET
    public List<Encontrista> listarTodos() {
        return encontristaService.buscarTodos();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") Long id) {
        Optional<Encontrista> encontrista = encontristaService.buscarPorId(id);
        return encontrista.map(Response::ok)
                          .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                          .build();
    }

    @GET
    @Path("/{id}/foto")
    @Produces({"image/jpeg", "image/png"})
    public Response buscarFoto(@PathParam("id") Long id) {
        Optional<Encontrista> encontrista = encontristaService.buscarPorId(id);
        if (encontrista.isPresent() && encontrista.get().getPessoa() != null && encontrista.get().getPessoa().getFoto() != null) {
            String caminho = "C:\\Desenvovilmento\\fotos\\" + encontrista.get().getPessoa().getFoto();
            java.io.File file = new java.io.File(caminho);
            if (file.exists()) {
                return Response.ok(file).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

}
