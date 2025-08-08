package br.com.segueme.entity;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.*;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "trabalhador", schema = "public", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "pessoa_id", "equipe_id", "encontro_id" })
})
public class Trabalhador implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id")
    private Encontro encontro;

    @Column(name = "eh_coordenador")
    private Boolean ehCoordenador;

    @Column(name = "foi_encontrista")
    private Boolean foiEncontrista;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontrista_id")
    private Encontrista encontrista;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "observacoes")
    private String observacoes;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @OneToMany(mappedBy = "trabalhador", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Contribuicao> contribuicoes = new HashSet<>();

    @OneToMany(mappedBy = "trabalhador", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Dirigente> cargos = new HashSet<>();

    @Column(name = "idade")
    private Integer idade;

    @Transient
    private boolean ehCasal;

    public boolean isEhCasal() {
        return ehCasal;
    }

    public void setEhCasal(boolean ehCasal) {
        this.ehCasal = ehCasal;
    }

    // Construtores
    public Trabalhador() {
        this.ehCoordenador = false;
        this.foiEncontrista = false;
        this.dataInicio = LocalDate.now();
    }

    public Trabalhador(Pessoa pessoa, Equipe equipe) {
        this();
        this.pessoa = pessoa;
        this.equipe = equipe;
    }

    public Trabalhador(Pessoa pessoa, Equipe equipe, Encontro encontro) {
        this(pessoa, equipe);
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

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public void setEncontro(Encontro encontro) {
        this.encontro = encontro;
    }

    public Boolean getEhCoordenador() {
        return ehCoordenador;
    }

    public void setEhCoordenador(Boolean ehCoordenador) {
        this.ehCoordenador = ehCoordenador;
    }

    public Boolean getFoiEncontrista() {
        return foiEncontrista;
    }

    public void setFoiEncontrista(Boolean foiEncontrista) {
        this.foiEncontrista = foiEncontrista;
    }

    public Encontrista getEncontrista() {
        return encontrista;
    }

    public void setEncontrista(Encontrista encontrista) {
        this.encontrista = encontrista;
        if (encontrista != null) {
            this.foiEncontrista = true;
        }
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

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Set<Contribuicao> getContribuicoes() {
        return contribuicoes;
    }

    public void setContribuicoes(Set<Contribuicao> contribuicoes) {
        this.contribuicoes = contribuicoes;
    }

    public Set<Dirigente> getCargos() {
        return cargos;
    }

    public void setCargos(Set<Dirigente> cargos) {
        this.cargos = cargos;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    // Métodos auxiliares
    public void adicionarContribuicao(Contribuicao contribuicao) {
        contribuicoes.add(contribuicao);
        contribuicao.setTrabalhador(this);
    }

    public void removerContribuicao(Contribuicao contribuicao) {
        contribuicoes.remove(contribuicao);
        contribuicao.setTrabalhador(null);
    }

    public void adicionarCargo(Dirigente dirigente) {
        cargos.add(dirigente);
        dirigente.setTrabalhador(this);
    }

    public void removerCargo(Dirigente dirigente) {
        cargos.remove(dirigente);
        dirigente.setTrabalhador(null);
    }
    // ...existing code...

    public void calcularIdade() {
        if (this.pessoa != null && this.pessoa.getDataNascimento() != null && this.dataInicio != null) {
            this.idade = java.time.Period.between(this.pessoa.getDataNascimento(), this.dataInicio).getYears();
        } else {
            this.idade = null;
        }
    }

    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Trabalhador that = (Trabalhador) o;
        return Objects.equals(id, that.id) ||
                (Objects.equals(pessoa, that.pessoa) &&
                        Objects.equals(equipe, that.equipe) &&
                        Objects.equals(encontro, that.encontro));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pessoa, equipe, encontro);
    }

    @Override
	public String toString() {
		return "Trabalhador [pessoa=" + pessoa + ", equipe=" + equipe + ", encontro=" + encontro + ", ehCoordenador="
				+ ehCoordenador + ", foiEncontrista=" + foiEncontrista + ", encontrista=" + encontrista
				+ ", dataInicio=" + dataInicio + ", dataFim=" + dataFim + ", observacoes=" + observacoes + ", ativo="
				+ ativo + ", contribuicoes=" + contribuicoes + ", cargos=" + cargos + ", idade=" + idade + ", ehCasal="
				+ ehCasal + "]";
	}
    
    
}
