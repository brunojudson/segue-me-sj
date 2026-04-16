package br.com.segueme.enums;

public enum StatusTransferencia {

    ATIVO_ORIGEM("Ativo na Paróquia de Origem"),
    TRANSFERIDO_SAIDA("Transferido para outra Paróquia"),
    TRANSFERIDO_ENTRADA("Recebido de outra Paróquia"),
    RETORNADO("Retornou à Paróquia de Origem");

    private final String descricao;

    StatusTransferencia(String descricao) {
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
