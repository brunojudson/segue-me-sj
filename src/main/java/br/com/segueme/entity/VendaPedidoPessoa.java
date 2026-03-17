package br.com.segueme.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

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
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import br.com.segueme.entity.enums.TipoAssociacaoPedido;

/**
 * Entidade que representa a associação entre um pedido e uma pessoa.
 * Permite que múltiplas pessoas sejam associadas ao mesmo pedido (conta compartilhada).
 */
@Entity
@Table(name = "venda_pedido_pessoa", schema = "public",
       uniqueConstraints = @UniqueConstraint(columnNames = {"pedido_id", "pessoa_id"}))
public class VendaPedidoPessoa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Pedido é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private VendaPedido pedido;

    @NotNull(message = "Pessoa é obrigatória")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @NotNull(message = "Tipo de associação é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_associacao", nullable = false, length = 30)
    private TipoAssociacaoPedido tipoAssociacao = TipoAssociacaoPedido.CONSUMIDOR;

    @DecimalMin(value = "0.0", inclusive = true, message = "Percentual de rateio não pode ser negativo")
    @DecimalMax(value = "100.0", inclusive = true, message = "Percentual de rateio não pode ser maior que 100")
    @Column(name = "percentual_rateio", precision = 5, scale = 2)
    private BigDecimal percentualRateio;

    @Size(max = 255, message = "Observações devem ter no máximo 255 caracteres")
    @Column(name = "observacoes", length = 255)
    private String observacoes;

    @Column(name = "data_associacao", nullable = false)
    private LocalDateTime dataAssociacao;

    // Lifecycle hooks
    @PrePersist
    protected void onCreate() {
        this.dataAssociacao = LocalDateTime.now();
        if (this.tipoAssociacao == null) {
            this.tipoAssociacao = TipoAssociacaoPedido.CONSUMIDOR;
        }
    }

    // Construtores
    public VendaPedidoPessoa() {
    }

    public VendaPedidoPessoa(VendaPedido pedido, Pessoa pessoa) {
        this.pedido = pedido;
        this.pessoa = pessoa;
        this.tipoAssociacao = TipoAssociacaoPedido.CONSUMIDOR;
    }

    public VendaPedidoPessoa(VendaPedido pedido, Pessoa pessoa, TipoAssociacaoPedido tipoAssociacao) {
        this.pedido = pedido;
        this.pessoa = pessoa;
        this.tipoAssociacao = tipoAssociacao;
    }

    public VendaPedidoPessoa(VendaPedido pedido, Pessoa pessoa, TipoAssociacaoPedido tipoAssociacao, BigDecimal percentualRateio) {
        this.pedido = pedido;
        this.pessoa = pessoa;
        this.tipoAssociacao = tipoAssociacao;
        this.percentualRateio = percentualRateio;
    }

    // Métodos de negócio
    
    /**
     * Calcula o valor correspondente ao percentual de rateio desta pessoa
     */
    public BigDecimal calcularValorRateio() {
        if (this.percentualRateio == null || this.pedido == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal valorTotal = this.pedido.getValorTotal();
        if (valorTotal == null) {
            return BigDecimal.ZERO;
        }
        
        return valorTotal.multiply(this.percentualRateio)
                         .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
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

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public TipoAssociacaoPedido getTipoAssociacao() {
        return tipoAssociacao;
    }

    public void setTipoAssociacao(TipoAssociacaoPedido tipoAssociacao) {
        this.tipoAssociacao = tipoAssociacao;
    }

    public BigDecimal getPercentualRateio() {
        return percentualRateio;
    }

    public void setPercentualRateio(BigDecimal percentualRateio) {
        this.percentualRateio = percentualRateio;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getDataAssociacao() {
        return dataAssociacao;
    }

    public void setDataAssociacao(LocalDateTime dataAssociacao) {
        this.dataAssociacao = dataAssociacao;
    }

    // equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VendaPedidoPessoa)) return false;
        VendaPedidoPessoa that = (VendaPedidoPessoa) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "VendaPedidoPessoa{" +
                "id=" + id +
                ", pessoa=" + (pessoa != null ? pessoa.getNome() : null) +
                ", tipoAssociacao=" + tipoAssociacao +
                ", percentualRateio=" + percentualRateio +
                '}';
    }
}
