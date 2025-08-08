package br.com.segueme.entity;

import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "encontro", schema="public")
public class Encontro implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "tema", length = 200)
    private String tema;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "local", nullable = false, length = 200)
    private String local;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "capacidade_maxima")
    private Integer capacidadeMaxima;

    @Column(name = "valor_inscricao", precision = 10, scale = 2)
    private BigDecimal valorInscricao;

    @Column(name = "valor_contribuicao_trabalhador", precision = 10, scale = 2)
    private BigDecimal valorContribuicaoTrabalhador;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @JsonbTransient
    @OneToMany(mappedBy = "encontro", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Encontrista> encontristas = new HashSet<>();
    
    @JsonbTransient
    @OneToMany(mappedBy = "encontro", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Equipe> equipes = new HashSet<>();

    @JsonbTransient
    @OneToMany(mappedBy = "encontro", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Trabalhador> trabalhadores = new HashSet<>();
    
    @JsonbTransient
    @OneToMany(mappedBy = "encontro", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Palestra> palestras = new HashSet<>();
	

    // Construtores
    public Encontro() {
        this.capacidadeMaxima = 60;
        this.ativo = true;
    }

    public Encontro(String nome, LocalDate dataInicio, LocalDate dataFim, String local) {
        this();
        this.nome = nome;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.local = local;
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

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
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

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public void setCapacidadeMaxima(Integer capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public BigDecimal getValorInscricao() {
        return valorInscricao;
    }

    public void setValorInscricao(BigDecimal valorInscricao) {
        this.valorInscricao = valorInscricao;
    }

    public BigDecimal getValorContribuicaoTrabalhador() {
        return valorContribuicaoTrabalhador;
    }

    public void setValorContribuicaoTrabalhador(BigDecimal valorContribuicaoTrabalhador) {
        this.valorContribuicaoTrabalhador = valorContribuicaoTrabalhador;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Set<Encontrista> getEncontristas() {
        return encontristas;
    }

    public void setEncontristas(Set<Encontrista> encontristas) {
        this.encontristas = encontristas;
    }

    public Set<Equipe> getEquipes() {
        return equipes;
    }

    public void setEquipes(Set<Equipe> equipes) {
        this.equipes = equipes;
    }

    public Set<Trabalhador> getTrabalhadores() {
        return trabalhadores;
    }

    public void setTrabalhadores(Set<Trabalhador> trabalhadores) {
        this.trabalhadores = trabalhadores;
    }

    public Set<Palestra> getPalestras() {
        return palestras;
    }

    public void setPalestras(Set<Palestra> palestras) {
        this.palestras = palestras;
    }

	// Métodos auxiliares
    public void adicionarEncontrista(Encontrista encontrista) {
        encontristas.add(encontrista);
        encontrista.setEncontro(this);
    }

    public void removerEncontrista(Encontrista encontrista) {
        encontristas.remove(encontrista);
        encontrista.setEncontro(null);
    }

    public void adicionarEquipe(Equipe equipe) {
        equipes.add(equipe);
        equipe.setEncontro(this);
    }

    public void removerEquipe(Equipe equipe) {
        equipes.remove(equipe);
        equipe.setEncontro(null);
    }

    public void adicionarTrabalhador(Trabalhador trabalhador) {
        trabalhadores.add(trabalhador);
        trabalhador.setEncontro(this);
    }

    public void removerTrabalhador(Trabalhador trabalhador) {
        trabalhadores.remove(trabalhador);
        trabalhador.setEncontro(null);
    }

    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Encontro encontro = (Encontro) o;
        return Objects.equals(id, encontro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Encontro{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                '}';
    }
}
