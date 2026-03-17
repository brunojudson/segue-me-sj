package br.com.segueme.entity;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "tipo_equipe",schema="public")
public class TipoEquipe implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome do tipo de equipe é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;
    
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Column(name = "descricao")
    private String descricao;
    
    @Column(name = "eh_dirigente")
    private Boolean ehDirigente;
    
    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;
    
    @OneToMany(mappedBy = "tipoEquipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Equipe> equipes = new HashSet<>();
    
    // Construtores
    public TipoEquipe() {
        this.ehDirigente = false;
    }
    
    public TipoEquipe(String nome) {
        this();
        this.nome = nome;
    }
    
    public TipoEquipe(String nome, Boolean ehDirigente) {
        this(nome);
        this.ehDirigente = ehDirigente;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getEhDirigente() {
        return ehDirigente;
    }

    public void setEhDirigente(Boolean ehDirigente) {
        this.ehDirigente = ehDirigente;
    }
    
    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    
    public Set<Equipe> getEquipes() {
        return equipes;
    }

    public void setEquipes(Set<Equipe> equipes) {
        this.equipes = equipes;
    }
    
    // Métodos auxiliares
    public void adicionarEquipe(Equipe equipe) {
        equipes.add(equipe);
        equipe.setTipoEquipe(this);
    }
    
    public void removerEquipe(Equipe equipe) {
        equipes.remove(equipe);
        equipe.setTipoEquipe(null);
    }
    
    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoEquipe that = (TipoEquipe) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "TipoEquipe{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", ehDirigente=" + ehDirigente +
                '}';
    }
}