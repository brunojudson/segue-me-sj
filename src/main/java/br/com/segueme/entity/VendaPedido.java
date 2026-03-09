package br.com.segueme.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import br.com.segueme.entity.enums.StatusPedido;

/**
 * Entidade que representa um pedido/conta de venda.
 * Um pedido pode ficar aberto e ir recebendo itens ao longo do tempo.
 */
@Entity
@Table(name = "venda_pedido", schema = "public")
public class VendaPedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 20, message = "Número do pedido deve ter no máximo 20 caracteres")
    @Column(name = "numero_pedido", length = 20, unique = true)
    private String numeroPedido; // Será gerado automaticamente pelo banco via trigger

    @NotNull(message = "Encontro é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @NotNull(message = "Trabalhador responsável é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabalhador_responsavel_id", nullable = false)
    private Trabalhador trabalhadorResponsavel;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusPedido status = StatusPedido.ABERTO;

    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor total não pode ser negativo")
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Valor pago não pode ser negativo")
    @Column(name = "valor_pago", precision = 10, scale = 2)
    private BigDecimal valorPago;

    @Size(max = 50, message = "Forma de pagamento deve ter no máximo 50 caracteres")
    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fechado_por_trabalhador_id")
    private Trabalhador fechadoPorTrabalhador;

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    @JsonbTransient
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<VendaItemPedido> itens = new HashSet<>();

    @JsonbTransient
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<VendaPedidoPessoa> pessoasAssociadas = new HashSet<>();

    // Lifecycle hooks
    @PrePersist
    protected void onCreate() {
        this.dataAbertura = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusPedido.ABERTO;
        }
        if (this.valorTotal == null) {
            this.valorTotal = BigDecimal.ZERO;
        }
    }

    // Construtores
    public VendaPedido() {
    }

    public VendaPedido(Encontro encontro, Trabalhador trabalhadorResponsavel) {
        this.encontro = encontro;
        this.trabalhadorResponsavel = trabalhadorResponsavel;
        this.dataAbertura = LocalDateTime.now();
        this.status = StatusPedido.ABERTO;
        this.valorTotal = BigDecimal.ZERO;
    }

    // Métodos de negócio
    
    /**
     * Adiciona um item ao pedido
     */
    public void adicionarItem(VendaItemPedido item) {
        if (!this.status.permiteAlteracao()) {
            throw new IllegalStateException("Não é possível adicionar itens a um pedido com status " + this.status);
        }
        this.itens.add(item);
        item.setPedido(this);
    }

    /**
     * Remove um item do pedido
     */
    public void removerItem(VendaItemPedido item) {
        if (!this.status.permiteAlteracao()) {
            throw new IllegalStateException("Não é possível remover itens de um pedido com status " + this.status);
        }
        this.itens.remove(item);
        item.setPedido(null);
    }

    /**
     * Associa uma pessoa ao pedido
     */
    public void associarPessoa(VendaPedidoPessoa associacao) {
        this.pessoasAssociadas.add(associacao);
        associacao.setPedido(this);
    }

    /**
     * Remove uma associação de pessoa do pedido
     */
    public void removerAssociacaoPessoa(VendaPedidoPessoa associacao) {
        this.pessoasAssociadas.remove(associacao);
        associacao.setPedido(null);
    }

    /**
     * Fecha o pedido para inclusão de novos itens
     */
    public void fechar(Trabalhador fechadoPor, boolean pago, String formaPagamento) {
        if (this.status != StatusPedido.ABERTO) {
            throw new IllegalStateException("Apenas pedidos abertos podem ser fechados");
        }
        
        if (this.itens.isEmpty()) {
            throw new IllegalStateException("Não é possível fechar um pedido sem itens");
        }
        
        this.dataFechamento = LocalDateTime.now();
        this.fechadoPorTrabalhador = fechadoPor;
        
        if (pago) {
            this.status = StatusPedido.PAGO;
            this.valorPago = this.valorTotal;
            this.formaPagamento = formaPagamento;
        } else {
            this.status = StatusPedido.AGUARDO_PAGAMENTO;
        }
    }

    /**
     * Marca o pedido como pago
     */
    public void marcarComoPago(BigDecimal valorPago, String formaPagamento) {
        if (this.status != StatusPedido.AGUARDO_PAGAMENTO) {
            throw new IllegalStateException("Apenas pedidos aguardando pagamento podem ser marcados como pagos");
        }
        
        this.status = StatusPedido.PAGO;
        this.valorPago = valorPago;
        this.formaPagamento = formaPagamento;
    }

    /**
     * Cancela o pedido
     */
    public void cancelar() {
        if (this.status.isFinalizado()) {
            throw new IllegalStateException("Pedidos finalizados não podem ser cancelados");
        }
        
        this.status = StatusPedido.CANCELADO;
        this.dataFechamento = LocalDateTime.now();
    }

    /**
     * Reabre o pedido (apenas se estiver aguardando pagamento)
     */
    public void reabrir() {
        if (this.status != StatusPedido.AGUARDO_PAGAMENTO) {
            throw new IllegalStateException("Apenas pedidos aguardando pagamento podem ser reabertos");
        }
        
        this.status = StatusPedido.ABERTO;
        this.dataFechamento = null;
        this.fechadoPorTrabalhador = null;
    }

    /**
     * Retorna a quantidade total de itens no pedido
     */
    public int getQuantidadeTotalItens() {
        return this.itens.stream()
                .mapToInt(VendaItemPedido::getQuantidade)
                .sum();
    }

    /**
     * Retorna o saldo pendente (valor_total - valor_pago)
     */
    public BigDecimal getSaldoPendente() {
        if (this.valorPago == null) {
            return this.valorTotal;
        }
        return this.valorTotal.subtract(this.valorPago);
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public void setEncontro(Encontro encontro) {
        this.encontro = encontro;
    }

    public Trabalhador getTrabalhadorResponsavel() {
        return trabalhadorResponsavel;
    }

    public void setTrabalhadorResponsavel(Trabalhador trabalhadorResponsavel) {
        this.trabalhadorResponsavel = trabalhadorResponsavel;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(LocalDateTime dataFechamento) {
        this.dataFechamento = dataFechamento;
    }
    
    // Métodos formatadores para uso no JSF
    public String getDataAberturaFormatada() {
        if (dataAbertura == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dataAbertura.format(formatter);
    }
    
    public String getDataFechamentoFormatada() {
        if (dataFechamento == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dataFechamento.format(formatter);
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public Trabalhador getFechadoPorTrabalhador() {
        return fechadoPorTrabalhador;
    }

    public void setFechadoPorTrabalhador(Trabalhador fechadoPorTrabalhador) {
        this.fechadoPorTrabalhador = fechadoPorTrabalhador;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Set<VendaItemPedido> getItens() {
        return itens;
    }

    public void setItens(Set<VendaItemPedido> itens) {
        this.itens = itens;
    }

    public Set<VendaPedidoPessoa> getPessoasAssociadas() {
        return pessoasAssociadas;
    }

    public void setPessoasAssociadas(Set<VendaPedidoPessoa> pessoasAssociadas) {
        this.pessoasAssociadas = pessoasAssociadas;
    }

    // equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VendaPedido)) return false;
        VendaPedido that = (VendaPedido) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "VendaPedido{" +
                "id=" + id +
                ", numeroPedido='" + numeroPedido + '\'' +
                ", status=" + status +
                ", valorTotal=" + valorTotal +
                ", dataAbertura=" + dataAbertura +
                '}';
    }
}
