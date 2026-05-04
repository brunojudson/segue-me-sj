package br.com.segueme.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
@Table(name = "contribuicao",schema="public")
public class Contribuicao implements Serializable, Auditavel {

    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Trabalhador é obrigatório")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trabalhador_id", nullable = false)
    private Trabalhador trabalhador;
    
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;
    
    @NotBlank(message = "Forma de pagamento é obrigatória")
    @Size(max = 50, message = "Forma de pagamento deve ter no máximo 50 caracteres")
    @Column(name = "forma_pagamento", nullable = false, length = 50)
    private String formaPagamento;
    
    @Size(max = 255, message = "URL do comprovante deve ter no máximo 255 caracteres")
    @Column(name = "comprovante_url", length = 255)
    private String comprovanteUrl;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    @Column(name = "observacoes")
    private String observacoes;
    
    // Construtores
    public Contribuicao() {
        this.dataPagamento = LocalDateTime.now();
    }

    public Contribuicao(Trabalhador trabalhador, BigDecimal valor, String formaPagamento) {
        this();
        this.trabalhador = trabalhador;
        this.valor = valor;
        this.formaPagamento = formaPagamento;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public Trabalhador getTrabalhador() {
        return trabalhador;
    }
    
    public void setTrabalhador(Trabalhador trabalhador) {
        this.trabalhador = trabalhador;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    
    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }
    
    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }
    
    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getComprovanteUrl() {
        return comprovanteUrl;
    }
    
    public void setComprovanteUrl(String comprovanteUrl) {
        this.comprovanteUrl = comprovanteUrl;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contribuicao that = (Contribuicao) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Contribuicao{" +
        "id=" + id +
        ", trabalhador=" + (trabalhador != null ? trabalhador.getNomeExibicao() : "null") +
                ", valor=" + valor +
                ", dataPagamento=" + dataPagamento +
                ", formaPagamento='" + formaPagamento + '\'' +
                '}';
    }

    public void setDataPagamento(LocalDate now) {
        if (now != null) {
            this.dataPagamento = now.atStartOfDay();
        } else {
            this.dataPagamento = null;
        }
    }
    @Override
    public String toAuditString() {
        return "id=" + id
            + " | trabalhador=" + (trabalhador != null ? trabalhador.getId() + "/" + trabalhador.getNomeExibicao() : null)
            + " | valor=" + valor
            + " | formaPagamento=" + formaPagamento
            + " | dataPagamento=" + dataPagamento
            + " | comprovanteUrl=" + comprovanteUrl
            + " | observacoes=" + observacoes;
    }
}
