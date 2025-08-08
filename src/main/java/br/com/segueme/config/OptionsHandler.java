package br.com.segueme.config;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/")
public class OptionsHandler {

    @OPTIONS
    @Path("{path: .*}")
    public Response handleOptions(@HeaderParam("Origin") String origin) {
        return Response.ok()
                .header("Access-Control-Allow-Origin", origin != null ? origin : "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization")
                .build();
    }
}

