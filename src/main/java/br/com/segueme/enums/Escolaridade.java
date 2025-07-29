package br.com.segueme.enums;

public enum Escolaridade {
    ANALFABETO("Analfabeto"),
    FUNDAMENTAL_INCOMPLETO("Fundamental Incompleto"),
    FUNDAMENTAL_COMPLETO("Fundamental Completo"),
    MEDIO_INCOMPLETO("Médio Incompleto"),
    MEDIO_COMPLETO("Médio Completo"),
    SUPERIOR_INCOMPLETO("Superior Incompleto"),
    SUPERIOR_COMPLETO("Superior Completo"),
    POS_GRADUACAO("Pós-Graduação"),
    MESTRADO("Mestrado"),
    DOUTORADO("Doutorado"),
    NAO_INFORMADO("Não Informado");

    private String descricao;

    Escolaridade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
