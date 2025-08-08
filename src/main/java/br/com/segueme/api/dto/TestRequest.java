package br.com.segueme.api.dto;

/**
 * DTO para request do teste POST
 */
public class TestRequest {
    private String repositorio;
    private String tecnologia;

    public TestRequest() {
    }

    public TestRequest(String repositorio, String tecnologia) {
        this.repositorio = repositorio;
        this.tecnologia = tecnologia;
    }

    public String getRepositorio() {
        return repositorio;
    }

    public void setRepositorio(String repositorio) {
        this.repositorio = repositorio;
    }

    public String getTecnologia() {
        return tecnologia;
    }

    public void setTecnologia(String tecnologia) {
        this.tecnologia = tecnologia;
    }

    @Override
    public String toString() {
        return "TestRequest{" +
                "repositorio='" + repositorio + '\'' +
                ", tecnologia='" + tecnologia + '\'' +
                '}';
    }
}
