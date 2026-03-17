package br.com.segueme.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import br.com.segueme.enums.CategoriaFinanceira;
import br.com.segueme.enums.TipoMovimento;

/**
 * Entidade para gestão financeira dos encontros.
 * Registra receitas e despesas por encontro, com categorias e balanço.
 */
@Entity
@Table(name = "movimento_financeiro", schema = "public")
public class MovimentoFinanceiro implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Tipo de movimento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TipoMovimento tipo;

    @NotNull(message = "Categoria é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 20)
    private CategoriaFinanceira categoria;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    @Column(name = "descricao", nullable = false)
    private String descricao;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "Data do movimento é obrigatória")
    @Column(name = "data_movimento", nullable = false)
    private LocalDate dataMovimento;

    @NotNull(message = "Encontro é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    @Column(name = "observacoes")
    private String observacoes;

    @Size(max = 100, message = "Responsável deve ter no máximo 100 caracteres")
    @Column(name = "responsavel", length = 100)
    private String responsavel;

    @Size(max = 255, message = "URL do comprovante deve ter no máximo 255 caracteres")
    @Column(name = "comprovante_url", length = 255)
    private String comprovanteUrl;

    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    // Construtores
    public MovimentoFinanceiro() {
        this.dataMovimento = LocalDate.now();
        this.dataCriacao = LocalDateTime.now();
    }

    public MovimentoFinanceiro(TipoMovimento tipo, CategoriaFinanceira categoria, String descricao,
                               BigDecimal valor, LocalDate dataMovimento, Encontro encontro) {
        this();
        this.tipo = tipo;
        this.categoria = categoria;
        this.descricao = descricao;
        this.valor = valor;
        this.dataMovimento = dataMovimento;
        this.encontro = encontro;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoMovimento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimento tipo) {
        this.tipo = tipo;
    }

    public CategoriaFinanceira getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaFinanceira categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getDataMovimento() {
        return dataMovimento;
    }

    public void setDataMovimento(LocalDate dataMovimento) {
        this.dataMovimento = dataMovimento;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public void setEncontro(Encontro encontro) {
        this.encontro = encontro;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getComprovanteUrl() {
        return comprovanteUrl;
    }

    public void setComprovanteUrl(String comprovanteUrl) {
        this.comprovanteUrl = comprovanteUrl;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovimentoFinanceiro that = (MovimentoFinanceiro) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MovimentoFinanceiro{" +
                "id=" + id +
                ", tipo=" + tipo +
                ", categoria=" + categoria +
                ", descricao='" + descricao + '\'' +
                ", valor=" + valor +
                ", dataMovimento=" + dataMovimento +
                ", encontro=" + (encontro != null ? encontro.getNome() : "null") +
                ", responsavel='" + responsavel + '\'' +
                '}';
    }
}
