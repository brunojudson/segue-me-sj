package br.com.segueme.api;

import br.com.segueme.api.dto.TestRequest;
import br.com.segueme.service.AzureDevOpsService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class APITest {

    // Constantes para evitar duplicação de literais
    private static final String STATUS_KEY = "status";
    private static final String MESSAGE_KEY = "message";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String SUCCESS_VALUE = "success";
    private static final String ERROR_VALUE = "error";

    @Inject
    private AzureDevOpsService azureDevOpsService;

    /**
     * Método OPTIONS para lidar com preflight requests CORS
     */
    @OPTIONS
    @Path("/{path:.*}")
    public Response handleCorsPreflightRequest() {
        return Response.ok()
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
            .header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, Origin")
            .header("Access-Control-Max-Age", "3600")
            .build();
    }

    /**
     * Endpoint simples para testar se a API está funcionando
     */
    @GET
    @Path("/health")
    public Response healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put(STATUS_KEY, "UP");
        response.put(TIMESTAMP_KEY, LocalDateTime.now().toString());
        response.put("service", "APITest");

        return Response.ok(response).build();
    }

    /**
     * Endpoint POST que também simula delay
     * Aceita parâmetros repositorio e tecnologia
     */
    @POST
    @Path("/delay")
    public Response testePostComDelay(TestRequest request) {
        LocalDateTime inicio = LocalDateTime.now();

        try {
            // Simular processamento de 10 segundos
            Thread.sleep(10000);

            LocalDateTime fim = LocalDateTime.now();

            Map<String, Object> response = new HashMap<>();
            response.put(STATUS_KEY, SUCCESS_VALUE);
            response.put(MESSAGE_KEY, "POST processado após 10 segundos");
            response.put("iniciadoEm", inicio.toString());
            response.put("finalizadoEm", fim.toString());
            response.put("duracaoSegundos", 10);

            // Incluir os parâmetros recebidos
            if (request != null) {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("repositorio", request.getRepositorio());
                parametros.put("tecnologia", request.getTecnologia());
                response.put("parametrosRecebidos", parametros);
            } else {
                response.put("parametrosRecebidos", null);
            }

            return Response.ok(response).build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(STATUS_KEY, ERROR_VALUE);
            errorResponse.put(MESSAGE_KEY, "Processamento foi interrompido: " + e.getMessage());
            errorResponse.put(TIMESTAMP_KEY, LocalDateTime.now().toString());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse).build();
        }
    }

    /**
     * Endpoint com delay customizável
     */
    @GET
    @Path("/delay/{segundos}")
    public Response testeComDelayCustomizado(@PathParam("segundos") int segundos) {
        if (segundos < 0 || segundos > 60) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(STATUS_KEY, ERROR_VALUE);
            errorResponse.put(MESSAGE_KEY, "Segundos deve estar entre 0 e 60");
            errorResponse.put(TIMESTAMP_KEY, LocalDateTime.now().toString());

            return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse).build();
        }

        LocalDateTime inicio = LocalDateTime.now();

        try {
            // Simular processamento pelo tempo especificado
            Thread.sleep(segundos * 1000L);

            LocalDateTime fim = LocalDateTime.now();

            Map<String, Object> response = new HashMap<>();
            response.put(STATUS_KEY, SUCCESS_VALUE);
            response.put(MESSAGE_KEY, "Processamento concluído após " + segundos + " segundos");
            response.put("iniciadoEm", inicio.toString());
            response.put("finalizadoEm", fim.toString());
            response.put("duracaoSegundos", segundos);

            return Response.ok(response).build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(STATUS_KEY, ERROR_VALUE);
            errorResponse.put(MESSAGE_KEY, "Processamento foi interrompido: " + e.getMessage());
            errorResponse.put(TIMESTAMP_KEY, LocalDateTime.now().toString());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse).build();
        }
    }

    /**
     * Endpoint para buscar todos os repositórios da organização Caixa
     */
    @GET
    @Path("/repositorios")
    public Response buscarRepositoriosCaixa() {
        LocalDateTime inicio = LocalDateTime.now();

        try {
            // Buscar repositórios usando o serviço Azure DevOps
            String repositoriosJson = azureDevOpsService.buscarRepositoriosCaixa();

            Map<String, Object> response = new HashMap<>();
            response.put(STATUS_KEY, SUCCESS_VALUE);
            response.put(MESSAGE_KEY, "Repositórios obtidos com sucesso");
            response.put(TIMESTAMP_KEY, inicio.toString());
            response.put("data", repositoriosJson);

            return Response.ok(response).build();

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(STATUS_KEY, ERROR_VALUE);
            errorResponse.put(MESSAGE_KEY, "Erro ao buscar repositórios: " + e.getMessage());
            errorResponse.put(TIMESTAMP_KEY, LocalDateTime.now().toString());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse).build();
        }
    }

    /**
     * Endpoint para testar a conectividade com Azure DevOps
     */
    @GET
    @Path("/azure-conectividade")
    public Response testarConectividadeAzure() {
        LocalDateTime inicio = LocalDateTime.now();

        try {
            // Testar conectividade usando o serviço Azure DevOps
            String resultadoTeste = azureDevOpsService.testarConectividade();

            Map<String, Object> response = new HashMap<>();
            response.put(STATUS_KEY, SUCCESS_VALUE);
            response.put(MESSAGE_KEY, "Teste de conectividade realizado");
            response.put(TIMESTAMP_KEY, inicio.toString());
            response.put("resultado", resultadoTeste);

            return Response.ok(response).build();

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(STATUS_KEY, ERROR_VALUE);
            errorResponse.put(MESSAGE_KEY, "Erro no teste de conectividade: " + e.getMessage());
            errorResponse.put(TIMESTAMP_KEY, LocalDateTime.now().toString());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse).build();
        }
    }

    /**
     * Endpoint para testar conectividade com SSL desabilitado (apenas HTTP)
     */
    @GET
    @Path("/azure-conectividade-http")
    public Response testarConectividadeHttpOnly() {
        LocalDateTime inicio = LocalDateTime.now();

        try {
            // Testar apenas com HTTP (sem SSL)
            String endpoint = "http://devops.caixa/_apis/projects?api-version=7.1";
            String resultado = azureDevOpsService.executarRequisicaoGetPublic(endpoint);

            Map<String, Object> response = new HashMap<>();
            response.put(STATUS_KEY, SUCCESS_VALUE);
            response.put(MESSAGE_KEY, "Teste HTTP realizado com sucesso");
            response.put(TIMESTAMP_KEY, inicio.toString());
            response.put("endpoint", endpoint);
            response.put("resultado", resultado.substring(0, Math.min(500, resultado.length())) + "...");

            return Response.ok(response).build();

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(STATUS_KEY, ERROR_VALUE);
            errorResponse.put(MESSAGE_KEY, "Erro no teste HTTP: " + e.getMessage());
            errorResponse.put(TIMESTAMP_KEY, LocalDateTime.now().toString());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse).build();
        }
    }

    

}
