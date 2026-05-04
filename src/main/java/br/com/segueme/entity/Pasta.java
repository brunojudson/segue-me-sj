package br.com.segueme.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "pasta", schema = "public")
public class Pasta implements Serializable, Auditavel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome da pasta é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @NotNull(message = "Equipe é obrigatória")
    @JsonbTransient
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Column(name = "descricao")
    private String descricao;

    @NotNull(message = "Data de início é obrigatória")
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    @JsonbTransient
    @OneToMany(mappedBy = "pasta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.Set<Dirigente> dirigentes = new java.util.HashSet<>();

    // Construtores
    public Pasta() {
        this.ativo = true;
    }

    public Pasta(String nome, Equipe equipe, LocalDate dataInicio, LocalDate dataFim) {
        this.nome = nome;
        this.equipe = equipe;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;

        // Verificar se a equipe é do tipo dirigente
        if (equipe != null && !equipe.ehEquipeDirigente()) {
            throw new IllegalArgumentException("Pasta só pode ser associada a uma equipe dirigente");
        }
    }

    public long calcularDuracaoMandato() {
        if (dataInicio != null && dataFim != null) {
            return ChronoUnit.DAYS.between(dataInicio, dataFim);
        }
        return 0;
    }

    /**
     * Verifica se a pasta está vigente na data informada.
     */
    public boolean isVigente(LocalDate data) {
        if (dataInicio == null || dataFim == null || data == null) {
            return false;
        }
        return !data.isBefore(dataInicio) && !data.isAfter(dataFim) && ativo;
    }

    /**
     * Verifica se a pasta está vigente hoje.
     */
    public boolean isVigenteHoje() {
        return isVigente(LocalDate.now());
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        // Verificar se a equipe é do tipo dirigente
        if (equipe != null && !equipe.ehEquipeDirigente()) {
            throw new IllegalArgumentException("Pasta só pode ser associada a uma equipe dirigente");
        }
        this.equipe = equipe;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public java.util.Set<Dirigente> getDirigentes() {
        return dirigentes;
    }

    public void setDirigentes(java.util.Set<Dirigente> dirigentes) {
        this.dirigentes = dirigentes;
    }

    // Métodos auxiliares
    public void adicionarDirigente(Dirigente dirigente) {
        dirigentes.add(dirigente);
        dirigente.setPasta(this);
    }

    public void removerDirigente(Dirigente dirigente) {
        dirigentes.remove(dirigente);
        dirigente.setPasta(null);
    }

    /**
     * Verifica se a duração da pasta está dentro dos limites aceitáveis.
     * A pasta pode ter duração de 1 a 2 anos (até ~731 dias).
     */
    public boolean verificarDuracaoValida() {
        if (dataInicio != null && dataFim != null) {
            long dias = ChronoUnit.DAYS.between(dataInicio, dataFim);
            return dias > 0 && dias <= 731;
        }
        return false;
    }

    /**
     * @deprecated Use {@link #verificarDuracaoValida()} em vez deste método.
     */
    @Deprecated
    public boolean verificarMandatoDoisAnos() {
        return verificarDuracaoValida();
    }

    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Pasta pasta = (Pasta) o;
        return Objects.equals(id, pasta.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pasta{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", equipe=" + (equipe != null ? equipe.getNome() : "null") +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                '}';
    }

    @Override
    public String toAuditString() {
        return "id=" + id
                + " | nome=" + nome
                + " | descricao=" + descricao
                + " | equipe=" + (equipe != null ? equipe.getId() + "/" + equipe.getNome() : null)
                + " | dataInicio=" + dataInicio
                + " | dataFim=" + dataFim
                + " | ativo=" + ativo;
    }
}