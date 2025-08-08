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

@Entity
@Table(name = "pasta", schema="public")
public class Pasta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @JsonbTransient
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

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
        return 0; // Retorna 0 se as datas não estiverem definidas
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

    public boolean verificarMandatoDoisAnos() {
        if (dataInicio != null && dataFim != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataFim);
            return dias >= 720 && dias <= 732; // Entre 720 e 732 dias (aproximadamente 2 anos)
        }
        return false;
    }

    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
}