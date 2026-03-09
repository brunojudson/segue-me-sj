package br.com.segueme.entity.enums;

/**
 * Enum para representar o tipo de associação de uma pessoa a um pedido
 */
public enum TipoAssociacaoPedido {
    
    /**
     * Pessoa que consumiu os itens do pedido
     */
    CONSUMIDOR("Consumidor", "Pessoa que consumiu/utilizou os artigos"),
    
    /**
     * Pessoa que está pagando o pedido (pode ser diferente do consumidor)
     */
    PAGADOR("Pagador", "Pessoa responsável pelo pagamento"),
    
    /**
     * Pedido compartilhado entre múltiplas pessoas
     */
    COMPARTILHADO("Compartilhado", "Conta compartilhada/dividida entre pessoas");
    
    private final String descricao;
    private final String descricaoDetalhada;
    
    TipoAssociacaoPedido(String descricao, String descricaoDetalhada) {
        this.descricao = descricao;
        this.descricaoDetalhada = descricaoDetalhada;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public String getDescricaoDetalhada() {
        return descricaoDetalhada;
    }
}
