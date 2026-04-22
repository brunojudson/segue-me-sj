package br.com.segueme.entity;

import java.io.Serializable;
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

import br.com.segueme.enums.TipoAtividade;

@Entity
@Table(name = "atividade_encontro", schema = "public")
public class AtividadeEncontro implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Encontro é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @NotNull(message = "Título é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    @Column(name = "titulo", length = 200, nullable = false)
    private String titulo;

    @NotNull(message = "Tipo de atividade é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_atividade", length = 30, nullable = false)
    private TipoAtividade tipoAtividade;

    @NotNull(message = "Data e hora de início são obrigatórios")
    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim")
    private LocalDateTime dataHoraFim;

    @Size(max = 100, message = "Responsável deve ter no máximo 100 caracteres")
    @Column(name = "responsavel", length = 100)
    private String responsavel;

    @Size(max = 200, message = "Local deve ter no máximo 200 caracteres")
    @Column(name = "local_atividade", length = 200)
    private String localAtividade;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Column(name = "descricao", length = 500)
    private String descricao;

    @Column(name = "ordem")
    private Integer ordem;

    /**
     * Vínculo opcional com uma Palestra cadastrada.
     * Preenchido quando tipoAtividade == PALESTRA.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "palestra_id")
    private Palestra palestra;

    // Construtores

    public AtividadeEncontro() {}

    // Helpers

    public String getDuracaoTexto() {
        if (dataHoraInicio == null || dataHoraFim == null) return "";
        long minutos = java.time.Duration.between(dataHoraInicio, dataHoraFim).toMinutes();
        if (minutos < 60) return minutos + " min";
        long horas = minutos / 60;
        long resto = minutos % 60;
        return resto == 0 ? horas + "h" : horas + "h " + resto + "min";
    }

    // Getters e Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Encontro getEncontro() { return encontro; }
    public void setEncontro(Encontro encontro) { this.encontro = encontro; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public TipoAtividade getTipoAtividade() { return tipoAtividade; }
    public void setTipoAtividade(TipoAtividade tipoAtividade) { this.tipoAtividade = tipoAtividade; }

    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public void setDataHoraInicio(LocalDateTime dataHoraInicio) { this.dataHoraInicio = dataHoraInicio; }

    public LocalDateTime getDataHoraFim() { return dataHoraFim; }
    public void setDataHoraFim(LocalDateTime dataHoraFim) { this.dataHoraFim = dataHoraFim; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getLocalAtividade() { return localAtividade; }
    public void setLocalAtividade(String localAtividade) { this.localAtividade = localAtividade; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public Palestra getPalestra() { return palestra; }
    public void setPalestra(Palestra palestra) { this.palestra = palestra; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtividadeEncontro that = (AtividadeEncontro) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
