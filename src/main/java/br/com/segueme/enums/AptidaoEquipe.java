package br.com.segueme.enums;

public enum AptidaoEquipe {
    ANIMACAO("Animação"),
    CANTO("Canto"),
    COZINHA("Cozinha"),
    ESTACIONAMENTO("Estacionamento"),
    FAXINA("Faxina"),
    GRAFICA("Gráfica"),
    LANCHE("Lanche"),
    LITURGIA("Liturgia"),
    MINI_MERCADO("Mini Mercado"),
    SALA("Sala"),
    VIGILIA_E_LITURGIA("Vigília e Liturgia"),
    VIGILIA_PAROQUIAL("Vigília Paroquial");

    private final String descricao;

    AptidaoEquipe(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
