package br.com.segueme.entity;

import java.io.Serializable;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import br.com.segueme.enums.StatusTransferencia;

/**
 * Registra cada evento de transferência de um seguidor entre paróquias.
 *
 * Fluxos suportados:
 *  - SAIDA   → seguidor desta paróquia transferido para outra (fica TRANSFERIDO_SAIDA)
 *  - ENTRADA → seguidor de outra paróquia aceito aqui       (fica TRANSFERIDO_ENTRADA)
 *  - RETORNO → seguidor transferido que volta à origem       (fica RETORNADO)
 */
@Entity
@Table(name = "transferencia", schema = "public")
public class Transferencia implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Pessoa é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    /** Paróquia de onde a pessoa sai. */
    @NotNull(message = "Paróquia de origem é obrigatória")
    @Size(max = 150, message = "Paróquia de origem deve ter no máximo 150 caracteres")
    @Column(name = "paroquia_origem", nullable = false, length = 150)
    private String paroquiaOrigem;

    /** Paróquia para onde a pessoa vai (pode ser a própria quando é retorno). */
    @NotNull(message = "Paróquia de destino é obrigatória")
    @Size(max = 150, message = "Paróquia de destino deve ter no máximo 150 caracteres")
    @Column(name = "paroquia_destino", nullable = false, length = 150)
    private String paroquiaDestino;

    /**
     * Tipo do evento de transferência:
     * TRANSFERIDO_SAIDA   → saiu desta paróquia
     * TRANSFERIDO_ENTRADA → foi aceito nesta paróquia vindo de outra
     * RETORNADO           → voltou para a paróquia de origem
     */
    @NotNull(message = "Tipo de transferência é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transferencia", nullable = false, length = 30)
    private StatusTransferencia tipoTransferencia;

    @Column(name = "data_solicitacao")
    private LocalDate dataSolicitacao;

    @Column(name = "data_efetivacao")
    private LocalDate dataEfetivacao;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Size(max = 100, message = "Registrado por deve ter no máximo 100 caracteres")
    @Column(name = "registrado_por", length = 100)
    private String registradoPor;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    // Construtores

    public Transferencia() {
        this.dataCadastro = LocalDateTime.now();
        this.dataSolicitacao = LocalDate.now();
    }

    public Transferencia(Pessoa pessoa, String paroquiaOrigem, String paroquiaDestino,
                         StatusTransferencia tipoTransferencia) {
        this();
        this.pessoa = pessoa;
        this.paroquiaOrigem = paroquiaOrigem;
        this.paroquiaDestino = paroquiaDestino;
        this.tipoTransferencia = tipoTransferencia;
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

    public String getParoquiaOrigem() {
        return paroquiaOrigem;
    }

    public void setParoquiaOrigem(String paroquiaOrigem) {
        this.paroquiaOrigem = paroquiaOrigem;
    }

    public String getParoquiaDestino() {
        return paroquiaDestino;
    }

    public void setParoquiaDestino(String paroquiaDestino) {
        this.paroquiaDestino = paroquiaDestino;
    }

    public StatusTransferencia getTipoTransferencia() {
        return tipoTransferencia;
    }

    public void setTipoTransferencia(StatusTransferencia tipoTransferencia) {
        this.tipoTransferencia = tipoTransferencia;
    }

    public LocalDate getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDate dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public LocalDate getDataEfetivacao() {
        return dataEfetivacao;
    }

    public void setDataEfetivacao(LocalDate dataEfetivacao) {
        this.dataEfetivacao = dataEfetivacao;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(String registradoPor) {
        this.registradoPor = registradoPor;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    // Equals e HashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transferencia that = (Transferencia) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transferencia [id=" + id +
                ", pessoa=" + (pessoa != null ? pessoa.getNome() : "null") +
                ", paroquiaOrigem=" + paroquiaOrigem +
                ", paroquiaDestino=" + paroquiaDestino +
                ", tipoTransferencia=" + tipoTransferencia +
                ", dataSolicitacao=" + dataSolicitacao +
                ", dataEfetivacao=" + dataEfetivacao + "]";
    }
}
