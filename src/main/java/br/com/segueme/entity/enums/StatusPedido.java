package br.com.segueme.entity.enums;

/**
 * Enum para representar os possíveis status de um pedido de venda
 */
public enum StatusPedido {
    
    /**
     * Pedido aberto - pode adicionar/remover itens
     */
    ABERTO("Aberto", "Pedido em andamento, itens podem ser adicionados ou removidos"),
    
    /**
     * Pedido fechado aguardando pagamento
     */
    AGUARDO_PAGAMENTO("Aguardando Pagamento", "Pedido fechado mas ainda não foi pago"),
    
    /**
     * Pedido pago e finalizado
     */
    PAGO("Pago", "Pedido quitado, não pode mais ser alterado"),
    
    /**
     * Pedido cancelado
     */
    CANCELADO("Cancelado", "Pedido cancelado, não será contabilizado");
    
    private final String descricao;
    private final String descricaoDetalhada;
    
    StatusPedido(String descricao, String descricaoDetalhada) {
        this.descricao = descricao;
        this.descricaoDetalhada = descricaoDetalhada;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public String getDescricaoDetalhada() {
        return descricaoDetalhada;
    }
    
    /**
     * Retorna true se o pedido permite alteração de itens
     */
    public boolean permiteAlteracao() {
        return this == ABERTO;
    }
    
    /**
     * Retorna true se o pedido está finalizado (pago ou cancelado)
     */
    public boolean isFinalizado() {
        return this == PAGO || this == CANCELADO;
    }
    
    /**
     * Retorna true se o pedido deve ser considerado em relatórios financeiros
     */
    public boolean contabilizarRelatorios() {
        return this == PAGO || this == AGUARDO_PAGAMENTO;
    }
}
