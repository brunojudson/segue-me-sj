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

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "encontrista", schema="public",uniqueConstraints = {
    @UniqueConstraint(columnNames = {"pessoa_id", "encontro_id"})
})
public class Encontrista implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Pessoa é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @NotNull(message = "Encontro é obrigatório")
    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @Column(name = "data_inscricao")
    private LocalDateTime dataInscricao;

    @DecimalMin(value = "0.0", inclusive = true, message = "Valor pago não pode ser negativo")
    @Column(name = "valor_pago", precision = 10, scale = 2)
    private BigDecimal valorPago;

    @Size(max = 50, message = "Forma de pagamento deve ter no máximo 50 caracteres")
    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
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

    @Column(name = "token_ficha", length = 36, unique = true)
    private String tokenFicha;

    // === Saúde e Emergência ===

    @Size(max = 500, message = "Alergias devem ter no máximo 500 caracteres")
    @Column(name = "alergias", length = 500)
    private String alergias;

    @Size(max = 500, message = "Restrições alimentares devem ter no máximo 500 caracteres")
    @Column(name = "restricoes_alimentares", length = 500)
    private String restricoesAlimentares;

    @Size(max = 500, message = "Medicamentos devem ter no máximo 500 caracteres")
    @Column(name = "medicamentos", length = 500)
    private String medicamentos;

    @Size(max = 500, message = "Condição médica deve ter no máximo 500 caracteres")
    @Column(name = "condicao_medica", length = 500)
    private String condicaoMedica;

    @Size(max = 100, message = "Nome do contato de emergência deve ter no máximo 100 caracteres")
    @Column(name = "contato_emergencia_nome", length = 100)
    private String contatoEmergenciaNome;

    @Size(max = 20, message = "Telefone de emergência deve ter no máximo 20 caracteres")
    @Column(name = "contato_emergencia_telefone", length = 20)
    private String contatoEmergenciaTelefone;

    @Size(max = 100, message = "Nome do responsável deve ter no máximo 100 caracteres")
    @Column(name = "responsavel_nome", length = 100)
    private String responsavelNome;

    @Size(max = 20, message = "Telefone do responsável deve ter no máximo 20 caracteres")
    @Column(name = "responsavel_telefone", length = 20)
    private String responsavelTelefone;

    @Column(name = "autorizacao_responsavel")
    private Boolean autorizacaoResponsavel;

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

    public String getTokenFicha() {
        return tokenFicha;
    }

    public void setTokenFicha(String tokenFicha) {
        this.tokenFicha = tokenFicha;
    }

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }

    public String getRestricaoAlimentar() { return restricoesAlimentares; }
    public void setRestricaoAlimentar(String restricoesAlimentares) { this.restricoesAlimentares = restricoesAlimentares; }

    public String getMedicamentos() { return medicamentos; }
    public void setMedicamentos(String medicamentos) { this.medicamentos = medicamentos; }

    public String getCondicaoMedica() { return condicaoMedica; }
    public void setCondicaoMedica(String condicaoMedica) { this.condicaoMedica = condicaoMedica; }

    public String getContatoEmergenciaNome() { return contatoEmergenciaNome; }
    public void setContatoEmergenciaNome(String contatoEmergenciaNome) { this.contatoEmergenciaNome = contatoEmergenciaNome; }

    public String getContatoEmergenciaTelefone() { return contatoEmergenciaTelefone; }
    public void setContatoEmergenciaTelefone(String contatoEmergenciaTelefone) { this.contatoEmergenciaTelefone = contatoEmergenciaTelefone; }

    public String getResponsavelNome() { return responsavelNome; }
    public void setResponsavelNome(String responsavelNome) { this.responsavelNome = responsavelNome; }

    public String getResponsavelTelefone() { return responsavelTelefone; }
    public void setResponsavelTelefone(String responsavelTelefone) { this.responsavelTelefone = responsavelTelefone; }

    public Boolean getAutorizacaoResponsavel() { return autorizacaoResponsavel; }
    public void setAutorizacaoResponsavel(Boolean autorizacaoResponsavel) { this.autorizacaoResponsavel = autorizacaoResponsavel; }

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
				+ circulo + ", contatoEmergenciaNome=" + contatoEmergenciaNome + "]";
	}
}
