package br.com.segueme.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "dirigente", uniqueConstraints = { @UniqueConstraint(columnNames = { "trabalhador_id", "pasta_id" }) })
public class Dirigente implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trabalhador_id", nullable = false)
	private Trabalhador trabalhador;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pasta_id", nullable = false)
	private Pasta pasta;

	@Column(name = "data_inicio", nullable = false)
	private LocalDate dataInicio;

	@Column(name = "data_fim", nullable = false)
	private LocalDate dataFim;

	@Column(name = "observacoes")
	private String observacoes;

	@Column(name = "data_cadastro")
	private LocalDateTime dataCadastro;

	@Column(name = "ativo", nullable = false)
	private boolean ativo = true;

	// Construtores
	public Dirigente() {
		this.dataCadastro = LocalDateTime.now();
		this.ativo = true;
	}

	public Dirigente(Trabalhador trabalhador, Pasta pasta, LocalDate dataInicio, LocalDate dataFim) {
		this.trabalhador = trabalhador;
		this.pasta = pasta;
		this.dataInicio = dataInicio;
		this.dataFim = dataFim;

		// Verificar mandato de 2 anos
		if (!verificarMandatoDoisAnos()) {
			throw new IllegalArgumentException(
					"Mandato de dirigente deve ser de aproximadamente 2 anos (720-732 dias)");
		}
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

	public Pasta getPasta() {
		return pasta;
	}

	public void setPasta(Pasta pasta) {
		this.pasta = pasta;
	}

	public LocalDate getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(LocalDate dataInicio) {
		this.dataInicio = dataInicio;
		// Verificar mandato de 2 anos se dataFim já estiver definida
		if (dataFim != null && !verificarMandatoDoisAnos()) {
			throw new IllegalArgumentException(
					"Mandato de dirigente deve ser de aproximadamente 2 anos (720-732 dias)");
		}
	}

	public LocalDate getDataFim() {
		return dataFim;
	}

	public void setDataFim(LocalDate dataFim) {
		this.dataFim = dataFim;
		// Verificar mandato de 2 anos se dataInicio já estiver definida
		if (dataInicio != null && !verificarMandatoDoisAnos()) {
			throw new IllegalArgumentException(
					"Mandato de dirigente deve ser de aproximadamente 2 anos (720-732 dias)");
		}
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
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

	// Métodos auxiliares
	public boolean verificarMandatoDoisAnos() {
		if (dataInicio != null && dataFim != null) {
			long dias = java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataFim);
			return dias <= 732; // Permite até 732 dias (aproximadamente 2 anos)
		}
		return false;
	}

	 public long calcularDuracaoMandato() {
        if (dataInicio != null && dataFim != null) {
            return ChronoUnit.DAYS.between(dataInicio, dataFim);
        }
        return 0; // Retorna 0 se as datas não estiverem definidas
    }

	// Equals e HashCode
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Dirigente dirigente = (Dirigente) o;
		return Objects.equals(id, dirigente.id)
				|| (Objects.equals(trabalhador, dirigente.trabalhador) && Objects.equals(pasta, dirigente.pasta));
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, trabalhador, pasta);
	}

	@Override
	public String toString() {
		return "Dirigente{" + "id=" + id + ", trabalhador="
				+ (trabalhador != null ? trabalhador.getPessoa().getNome() : "null") + ", pasta="
				+ (pasta != null ? pasta.getNome() : "null") + ", dataInicio=" + dataInicio + ", dataFim=" + dataFim
				+ '}';
	}
}