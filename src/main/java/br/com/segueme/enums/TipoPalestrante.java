package br.com.segueme.enums;

public enum TipoPalestrante {
    INDIVIDUAL("Individual"),
    CASAL("Casal");

	private String nomeExibicao;
    
    TipoPalestrante(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }
    
    public String getNomeExibicao() {
        return nomeExibicao;
    }
}