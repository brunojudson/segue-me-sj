package br.com.segueme.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Entidade que representa um item de um pedido de venda
 */
@Entity
@Table(name = "venda_item_pedido", schema = "public")
public class VendaItemPedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Pedido é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private VendaPedido pedido;

    @NotNull(message = "Artigo é obrigatório")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "artigo_id", nullable = false)
    private VendaArtigo artigo;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @NotNull(message = "Valor unitário é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor unitário não pode ser negativo")
    @Column(name = "valor_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    @NotNull(message = "Valor total do item é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor total do item não pode ser negativo")
    @Column(name = "valor_total_item", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotalItem;

    @Size(max = 255, message = "Observações devem ter no máximo 255 caracteres")
    @Column(name = "observacoes", length = 255)
    private String observacoes;

    @Column(name = "data_inclusao", nullable = false)
    private LocalDateTime dataInclusao;

    // Lifecycle hooks
    @PrePersist
    protected void onCreate() {
        this.dataInclusao = LocalDateTime.now();
        calcularValorTotalItem();
    }

    @PreUpdate
    protected void onUpdate() {
        calcularValorTotalItem();
    }

    // Construtores
    public VendaItemPedido() {
    }

    public VendaItemPedido(VendaPedido pedido, VendaArtigo artigo, Integer quantidade) {
        this.pedido = pedido;
        this.artigo = artigo;
        this.quantidade = quantidade;
        this.valorUnitario = artigo.getPrecoBase();
        calcularValorTotalItem();
    }

    public VendaItemPedido(VendaPedido pedido, VendaArtigo artigo, Integer quantidade, BigDecimal valorUnitario) {
        this.pedido = pedido;
        this.artigo = artigo;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        calcularValorTotalItem();
    }

    // Métodos de negócio
    
    /**
     * Calcula o valor total do item baseado na quantidade e valor unitário
     */
    public void calcularValorTotalItem() {
        if (this.quantidade != null && this.valorUnitario != null) {
            this.valorTotalItem = this.valorUnitario.multiply(new BigDecimal(this.quantidade));
        }
    }

    /**
     * Atualiza a quantidade e recalcula o valor total
     */
    public void atualizarQuantidade(Integer novaQuantidade) {
        if (novaQuantidade == null || novaQuantidade < 1) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        this.quantidade = novaQuantidade;
        calcularValorTotalItem();
    }

    /**
     * Atualiza o valor unitário e recalcula o valor total
     */
    public void atualizarValorUnitario(BigDecimal novoValorUnitario) {
        if (novoValorUnitario == null || novoValorUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor unitário não pode ser negativo");
        }
        this.valorUnitario = novoValorUnitario;
        calcularValorTotalItem();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VendaPedido getPedido() {
        return pedido;
    }

    public void setPedido(VendaPedido pedido) {
        this.pedido = pedido;
    }

    public VendaArtigo getArtigo() {
        return artigo;
    }

    public void setArtigo(VendaArtigo artigo) {
        this.artigo = artigo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
        calcularValorTotalItem();
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
        calcularValorTotalItem();
    }

    public BigDecimal getValorTotalItem() {
        return valorTotalItem;
    }

    public void setValorTotalItem(BigDecimal valorTotalItem) {
        this.valorTotalItem = valorTotalItem;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getDataInclusao() {
        return dataInclusao;
    }

    public void setDataInclusao(LocalDateTime dataInclusao) {
        this.dataInclusao = dataInclusao;
    }

    // equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VendaItemPedido)) return false;
        VendaItemPedido that = (VendaItemPedido) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "VendaItemPedido{" +
                "id=" + id +
                ", artigo=" + (artigo != null ? artigo.getNome() : null) +
                ", quantidade=" + quantidade +
                ", valorUnitario=" + valorUnitario +
                ", valorTotalItem=" + valorTotalItem +
                '}';
    }
}
