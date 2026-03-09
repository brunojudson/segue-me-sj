package br.com.segueme.enums;

/**
 * Tipo de movimento financeiro.
 */
public enum TipoMovimento {
    RECEITA("Receita"),
    DESPESA("Despesa");

    private final String descricao;

    TipoMovimento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
