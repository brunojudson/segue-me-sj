package br.com.segueme.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Entidade que representa um artigo/produto disponível para venda
 */
@Entity
@Table(name = "venda_artigo", schema = "public")
public class VendaArtigo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50, message = "Código deve ter no máximo 50 caracteres")
    @Column(name = "codigo", length = 50, unique = true)
    private String codigo;

    @NotBlank(message = "Nome do artigo é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Column(name = "descricao", length = 500)
    private String descricao;

    @NotNull(message = "Preço base é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Preço base não pode ser negativo")
    @Column(name = "preco_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoBase;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Size(max = 50, message = "Categoria deve ter no máximo 50 caracteres")
    @Column(name = "categoria", length = 50)
    private String categoria;

    @Min(value = 0, message = "Estoque atual não pode ser negativo")
    @Column(name = "estoque_atual")
    private Integer estoqueAtual = 0;

    @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
    @Column(name = "estoque_minimo")
    private Integer estoqueMinimo = 0;

    @Size(max = 255, message = "URL da foto deve ter no máximo 255 caracteres")
    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @JsonbTransient
    @OneToMany(mappedBy = "artigo", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    private Set<VendaItemPedido> itensPedido = new HashSet<>();

    // Lifecycle hooks
    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
        if (this.ativo == null) {
            this.ativo = true;
        }
        if (this.estoqueAtual == null) {
            this.estoqueAtual = 0;
        }
        if (this.estoqueMinimo == null) {
            this.estoqueMinimo = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Construtores
    public VendaArtigo() {
    }

    public VendaArtigo(String nome, BigDecimal precoBase) {
        this.nome = nome;
        this.precoBase = precoBase;
        this.ativo = true;
        this.estoqueAtual = 0;
        this.estoqueMinimo = 0;
    }

    // Métodos de negócio
    
    /**
     * Verifica se o artigo está disponível para venda
     */
    public boolean isDisponivel() {
        return this.ativo && (this.estoqueAtual == null || this.estoqueAtual > 0);
    }

    /**
     * Verifica se o estoque está abaixo do mínimo
     */
    public boolean isEstoqueBaixo() {
        return this.estoqueAtual != null && this.estoqueMinimo != null 
               && this.estoqueAtual < this.estoqueMinimo;
    }

    /**
     * Debita quantidade do estoque
     */
    public void debitarEstoque(int quantidade) {
        if (this.estoqueAtual != null) {
            this.estoqueAtual -= quantidade;
            if (this.estoqueAtual < 0) {
                this.estoqueAtual = 0;
            }
        }
    }

    /**
     * Credita quantidade no estoque
     */
    public void creditarEstoque(int quantidade) {
        if (this.estoqueAtual == null) {
            this.estoqueAtual = 0;
        }
        this.estoqueAtual += quantidade;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPrecoBase() {
        return precoBase;
    }

    public void setPrecoBase(BigDecimal precoBase) {
        this.precoBase = precoBase;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getEstoqueAtual() {
        return estoqueAtual;
    }

    public void setEstoqueAtual(Integer estoqueAtual) {
        this.estoqueAtual = estoqueAtual;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Set<VendaItemPedido> getItensPedido() {
        return itensPedido;
    }

    public void setItensPedido(Set<VendaItemPedido> itensPedido) {
        this.itensPedido = itensPedido;
    }

    // equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VendaArtigo)) return false;
        VendaArtigo that = (VendaArtigo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "VendaArtigo{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nome='" + nome + '\'' +
                ", precoBase=" + precoBase +
                ", ativo=" + ativo +
                ", categoria='" + categoria + '\'' +
                '}';
    }
}
