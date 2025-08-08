package br.com.segueme.api;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api") // Define o prefixo base da sua API, ex: http://localhost:8080/seuapp/api/encontros
public class RestApplication extends Application {
    // Não precisa implementar nada, só a anotação já registra todos os resources do pacote
}