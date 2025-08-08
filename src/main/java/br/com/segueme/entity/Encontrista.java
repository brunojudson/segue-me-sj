package br.com.segueme.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import br.com.segueme.enums.Circulo;

@Entity
@Table(name = "encontrista", schema="public",uniqueConstraints = {
    @UniqueConstraint(columnNames = {"pessoa_id", "encontro_id"})
})
public class Encontrista implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @Column(name = "data_inscricao")
    private LocalDateTime dataInscricao;

    @Column(name = "valor_pago", precision = 10, scale = 2)
    private BigDecimal valorPago;

    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento;

    @Column(name = "observacoes")
    private String observacoes;
    
    @Column(name = "ativo")
    private Boolean ativo = true;

    @OneToOne(mappedBy = "encontrista", cascade = CascadeType.ALL, orphanRemoval = false)
    private Trabalhador trabalhador;
    
    @Column(name = "idade")
    private Integer idade;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "circulo", length = 10)
    private Circulo circulo;

    // Construtores
    public Encontrista() {
        this.dataInscricao = LocalDateTime.now();
        this.ativo = true;
    }

    public Encontrista(Pessoa pessoa, Encontro encontro) {
        this();
        this.pessoa = pessoa;
        this.encontro = encontro;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public void setEncontro(Encontro encontro) {
        this.encontro = encontro;
    }

    public LocalDateTime getDataInscricao() {
        return dataInscricao;
    }

    public void setDataInscricao(LocalDateTime dataInscricao) {
        this.dataInscricao = dataInscricao;
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

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Trabalhador getTrabalhador() {
        return trabalhador;
    }

    public void setTrabalhador(Trabalhador trabalhador) {
        this.trabalhador = trabalhador;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }
    
    public Circulo getCirculo() {
        return circulo;
    }

    public void setCirculo(Circulo circulo) {
        this.circulo = circulo;
    }

    public void calcularIdade() {
        if (this.pessoa != null && this.pessoa.getDataNascimento() != null) {
            this.idade = java.time.Period.between(
                this.pessoa.getDataNascimento(),
                java.time.LocalDate.now()
            ).getYears();
        } else {
            this.idade = null;
        }
    }
    
    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Encontrista that = (Encontrista) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(pessoa, that.pessoa) &&
               Objects.equals(encontro, that.encontro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pessoa, encontro);
    }

    @Override
	public String toString() {
		return "Encontrista [id=" + id + ", pessoa=" + pessoa + ", encontro=" + encontro + ", dataInscricao="
				+ dataInscricao + ", valorPago=" + valorPago + ", formaPagamento=" + formaPagamento + ", observacoes="
				+ observacoes + ", ativo=" + ativo + ", trabalhador=" + trabalhador + ", idade=" + idade + ", circulo="
				+ circulo + "]";
	}
}
