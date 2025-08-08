package br.com.segueme.api;

import br.com.segueme.entity.Pessoa;
import br.com.segueme.service.PessoaService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/pessoas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PessoaResource {

    @Inject
    private PessoaService pessoaService;

    @GET
    public List<Pessoa> listarTodos() {
        return pessoaService.buscarTodos();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") Long id) {
        Optional<Pessoa> pessoa = pessoaService.buscarPorId(id);
        return pessoa.map(Response::ok)
                     .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                     .build();
    }
    
    @GET
    @Path("/{id}/foto")
    @Produces({"image/jpeg", "image/png"})
    public Response buscarFoto(@PathParam("id") Long id) {
        Optional<Pessoa> pessoa = pessoaService.buscarPorId(id);
        if (pessoa.isPresent() && pessoa.get().getFoto() != null) {
            String caminho = "C:\\Desenvovilmento\\fotos\\" + pessoa.get().getFoto();
            java.io.File file = new java.io.File(caminho);
            if (file.exists()) {
                return Response.ok(file).build();
            }
        }
        // Retorna 404 se não encontrar a foto
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}