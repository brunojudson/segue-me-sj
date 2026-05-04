package br.com.segueme.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Dirigente representa um trabalhador que exerce um cargo de liderança em uma pasta.
 * 
 * Regras de mandato:
 * - Mandato inicial de 1 ano (365 dias)
 * - Pode ser prorrogado por mais 1 ano (total máximo de 2 anos / ~730 dias)
 * - Pode ser encerrado antecipadamente por renúncia, destituição, falecimento, etc.
 * - Um mesmo trabalhador pode exercer mandatos diferentes na mesma pasta em períodos distintos
 */
@Entity
@Table(name = "dirigente")
public class Dirigente implements Serializable, Auditavel {

	
	private static final long serialVersionUID = 1L;

	/** Duração padrão de 1 mandato: 1 ano */
	public static final int DURACAO_MANDATO_DIAS = 365;
	/** Duração máxima com prorrogação: 2 anos */
	public static final int DURACAO_MAXIMA_DIAS = 731;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message = "Trabalhador é obrigatório")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trabalhador_id", nullable = false)
	private Trabalhador trabalhador;
	
	@NotNull(message = "Pasta é obrigatória")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pasta_id", nullable = false)
	private Pasta pasta;
	
	@NotNull(message = "Data de início é obrigatória")
	@Column(name = "data_inicio", nullable = false)
	private LocalDate dataInicio;
	
	@NotNull(message = "Data de fim prevista é obrigatória")
	@Column(name = "data_fim", nullable = false)
	private LocalDate dataFim;
	
	@Column(name = "data_encerramento_efetivo")
	private LocalDate dataEncerramentoEfetivo;
	
	@NotNull(message = "Status do mandato é obrigatório")
	@Enumerated(EnumType.STRING)
	@Column(name = "status_mandato", nullable = false, length = 30)
	private StatusMandato statusMandato;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "motivo_encerramento", length = 30)
	private MotivoEncerramentoMandato motivoEncerramento;
	
	@Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
	@Column(name = "observacoes")
	private String observacoes;
	
	@Column(name = "prorrogado", nullable = false)
	private boolean prorrogado = false;
	
	@Column(name = "data_prorrogacao")
	private LocalDate dataProrrogacao;
	
	@Column(name = "data_cadastro")
	private LocalDateTime dataCadastro;
	
	@Column(name = "ativo", nullable = false)
	private boolean ativo = true;
	
	// Construtores
	public Dirigente() {
		this.dataCadastro = LocalDateTime.now();
		this.ativo = true;
		this.statusMandato = StatusMandato.ATIVO;
		this.prorrogado = false;
	}
	
	/**
	 * Cria um dirigente com mandato padrão de 1 ano.
	*/
	public Dirigente(Trabalhador trabalhador, Pasta pasta, LocalDate dataInicio) {
		this();
		this.trabalhador = trabalhador;
		this.pasta = pasta;
		this.dataInicio = dataInicio;
		this.dataFim = dataInicio.plusYears(1);
	}
	
	/**
	 * Cria um dirigente com datas de início e fim específicas.
	 * A duração não pode ultrapassar o máximo permitido.
	*/
	public Dirigente(Trabalhador trabalhador, Pasta pasta, LocalDate dataInicio, LocalDate dataFim) {
		this();
		this.trabalhador = trabalhador;
		this.pasta = pasta;
		this.dataInicio = dataInicio;
		this.dataFim = dataFim;
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
	}
	
	public LocalDate getDataFim() {
		return dataFim;
	}
	
	public void setDataFim(LocalDate dataFim) {
		this.dataFim = dataFim;
	}
	
	public LocalDate getDataEncerramentoEfetivo() {
		return dataEncerramentoEfetivo;
	}
	
	public void setDataEncerramentoEfetivo(LocalDate dataEncerramentoEfetivo) {
		this.dataEncerramentoEfetivo = dataEncerramentoEfetivo;
	}
	
	public StatusMandato getStatusMandato() {
		return statusMandato;
	}
	
	public void setStatusMandato(StatusMandato statusMandato) {
		this.statusMandato = statusMandato;
	}
	
	public MotivoEncerramentoMandato getMotivoEncerramento() {
		return motivoEncerramento;
	}
	
	public void setMotivoEncerramento(MotivoEncerramentoMandato motivoEncerramento) {
		this.motivoEncerramento = motivoEncerramento;
	}
	
	public boolean isProrrogado() {
		return prorrogado;
	}
	
	public void setProrrogado(boolean prorrogado) {
		this.prorrogado = prorrogado;
	}
	
	public LocalDate getDataProrrogacao() {
		return dataProrrogacao;
	}
	
	public void setDataProrrogacao(LocalDate dataProrrogacao) {
		this.dataProrrogacao = dataProrrogacao;
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
	
	// Métodos de negócio
	
	/**
	 * Verifica se a duração do mandato está dentro do limite permitido.
	 * Mandato padrão: até 1 ano (366 dias, para contemplar anos bissextos).
	 * Mandato prorrogado: até 2 anos (~731 dias).
	*/
	public boolean verificarDuracaoMandatoValida() {
		if (dataInicio == null || dataFim == null) {
			return false;
		}
		long dias = ChronoUnit.DAYS.between(dataInicio, dataFim);
		if (dias <= 0) {
			return false;
		}
		int limiteMaximo = prorrogado ? DURACAO_MAXIMA_DIAS : DURACAO_MANDATO_DIAS + 1; // +1 para bissexto
		return dias <= limiteMaximo;
	}
	
	/**
	 * @deprecated Use {@link #verificarDuracaoMandatoValida()} em vez deste método.
	*/
	@Deprecated
	public boolean verificarMandatoDoisAnos() {
		return verificarDuracaoMandatoValida();
	}
	
	public long calcularDuracaoMandato() {
		if (dataInicio != null && dataFim != null) {
			return ChronoUnit.DAYS.between(dataInicio, dataFim);
		}
		return 0;
	}
	
	/**
	 * Retorna a data efetiva de término do mandato.
	 * Se foi encerrado antecipadamente, retorna a data de encerramento efetivo.
	 * Caso contrário, retorna a data de fim planejada.
	 */
	public LocalDate getDataTerminoEfetivo() {
		return dataEncerramentoEfetivo != null ? dataEncerramentoEfetivo : dataFim;
	}

	/**
	 * Verifica se o mandato está vigente na data informada.
	*/
	public boolean isMandatoVigente(LocalDate data) {
		if (statusMandato != null && statusMandato.isEncerrado()) {
			return false;
		}
		if (dataInicio == null || dataFim == null || data == null) {
			return false;
		}
		return !data.isBefore(dataInicio) && !data.isAfter(getDataTerminoEfetivo());
	}
	
	/**
	 * Verifica se o mandato está vigente hoje.
	*/
	public boolean isMandatoVigenteHoje() {
		return isMandatoVigente(LocalDate.now());
	}
	
	/**
	 * Verifica se o mandato pode ser prorrogado.
	 * Condições: não foi prorrogado antes, está ativo e não foi encerrado.
	*/
	public boolean podeProrrogar() {
		return !prorrogado && statusMandato != null && statusMandato == StatusMandato.ATIVO;
	}
	
	/**
	 * Prorroga o mandato por mais 1 ano a partir da data de fim atual.
	 */
	public void prorrogar() {
		if (!podeProrrogar()) {
			throw new IllegalStateException("Mandato não pode ser prorrogado. "
			+ "Verifique se já foi prorrogado anteriormente ou se está ativo.");
		}
		this.dataFim = this.dataFim.plusYears(1);
		this.prorrogado = true;
		this.dataProrrogacao = LocalDate.now();
		this.statusMandato = StatusMandato.PRORROGADO;
	}
	
	/**
	 * Encerra o mandato antecipadamente com o motivo informado.
	*/
	public void encerrarMandato(MotivoEncerramentoMandato motivo, String observacao) {
		if (statusMandato != null && statusMandato.isEncerrado()) {
			throw new IllegalStateException("Mandato já está encerrado.");
		}
		this.dataEncerramentoEfetivo = LocalDate.now();
		this.motivoEncerramento = motivo;
		this.ativo = false;
		
		switch (motivo) {
			case RENUNCIA:
				this.statusMandato = StatusMandato.RENUNCIADO;
				break;
				case DESTITUICAO:
					this.statusMandato = StatusMandato.DESTITUIDO;
					break;
					case TERMINO_NATURAL:
						this.statusMandato = StatusMandato.ENCERRADO_NATURAL;
			break;
			default:
				this.statusMandato = StatusMandato.ENCERRADO_OUTROS;
			break;
		}

		if (observacao != null && !observacao.isEmpty()) {
			this.observacoes = (this.observacoes != null ? this.observacoes + " | " : "") + observacao;
		}
	}
	
	/**
	 * Atalho para renúncia.
	*/
	public void renunciar(String observacao) {
		encerrarMandato(MotivoEncerramentoMandato.RENUNCIA, observacao);
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
		+ (trabalhador != null ? trabalhador.getNomeExibicao() : "null") + ", pasta="
		+ (pasta != null ? pasta.getNome() : "null") + ", dataInicio=" + dataInicio + ", dataFim=" + dataFim
		+ ", status=" + statusMandato
		+ ", prorrogado=" + prorrogado
		+ '}';
	}
	@Override
	public String toAuditString() {
		return "id=" + id
			+ " | trabalhador=" + (trabalhador != null ? trabalhador.getId() + "/" + trabalhador.getNomeExibicao() : null)
			+ " | pasta=" + (pasta != null ? pasta.getId() + "/" + pasta.getNome() : null)
			+ " | status=" + statusMandato
			+ " | dataInicio=" + dataInicio
			+ " | dataFim=" + dataFim
			+ " | dataEncerramentoEfetivo=" + dataEncerramentoEfetivo
			+ " | prorrogado=" + prorrogado
			+ " | dataProrrogacao=" + dataProrrogacao
			+ " | motivoEncerramento=" + motivoEncerramento
			+ " | observacoes=" + observacoes
			+ " | ativo=" + ativo;
	}
}