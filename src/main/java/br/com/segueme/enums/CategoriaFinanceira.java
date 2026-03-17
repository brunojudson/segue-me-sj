package br.com.segueme.enums;

/**
 * Categorias de movimento financeiro do encontro.
 */
public enum CategoriaFinanceira {
    INSCRICAO("Inscrição"),
    DOACAO("Doação"),
    EVENTO("Evento"),
    CONTRIBUICAO("Contribuição"),
    ALIMENTACAO("Alimentação"),
    TRANSPORTE("Transporte"),
    MATERIAL("Material"),
    LOCACAO("Locação"),
    DECORACAO("Decoração"),
    IMPRESSAO("Impressão"),
    COMUNICACAO("Comunicação"),
    OUTROS("Outros");

    private final String descricao;

    CategoriaFinanceira(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
