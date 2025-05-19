package br.com.segueme.entity;

import java.io.Serializable;
import java.time.LocalDate;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "equipe", schema="public")
public class Equipe implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_equipe_id", nullable = false)
    private TipoEquipe tipoEquipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id")
    private Encontro encontro;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @JsonbTransient
    @OneToMany(mappedBy = "equipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Trabalhador> trabalhadores = new HashSet<>();

    @JsonbTransient
    @OneToMany(mappedBy = "equipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Pasta> pastas = new HashSet<>();

    // Construtores
    public Equipe() {
        this.ativo = true;
    }

    public Equipe(String nome, TipoEquipe tipoEquipe) {
        this();
        this.nome = nome;
        this.tipoEquipe = tipoEquipe;
    }

    public Equipe(String nome, TipoEquipe tipoEquipe, Encontro encontro) {
        this(nome, tipoEquipe);
        this.encontro = encontro;
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

    public TipoEquipe getTipoEquipe() {
        return tipoEquipe;
    }

    public void setTipoEquipe(TipoEquipe tipoEquipe) {
        this.tipoEquipe = tipoEquipe;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public void setEncontro(Encontro encontro) {
        this.encontro = encontro;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Set<Trabalhador> getTrabalhadores() {
        return trabalhadores;
    }

    public void setTrabalhadores(Set<Trabalhador> trabalhadores) {
        this.trabalhadores = trabalhadores;
    }

    public Set<Pasta> getPastas() {
        return pastas;
    }

    public void setPastas(Set<Pasta> pastas) {
        this.pastas = pastas;
    }

    // Métodos auxiliares
    public void adicionarTrabalhador(Trabalhador trabalhador) {
        trabalhadores.add(trabalhador);
        trabalhador.setEquipe(this);
    }

    public void removerTrabalhador(Trabalhador trabalhador) {
        trabalhadores.remove(trabalhador);
        trabalhador.setEquipe(null);
    }

    public void adicionarPasta(Pasta pasta) {
        pastas.add(pasta);
        pasta.setEquipe(this);
    }

    public void removerPasta(Pasta pasta) {
        pastas.remove(pasta);
        pasta.setEquipe(null);
    }

    public boolean ehEquipeDirigente() {
        return tipoEquipe != null && tipoEquipe.getEhDirigente();
    }

    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipe equipe = (Equipe) o;
        return Objects.equals(id, equipe.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Equipe{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", tipoEquipe=" + (tipoEquipe != null ? tipoEquipe.getNome() : "null") +
                ", encontro=" + (encontro != null ? encontro.getNome() : "null") +
                '}';
    }
}
