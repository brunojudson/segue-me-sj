package br.com.segueme.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.com.segueme.enums.TipoPalestrante;

@Entity
@Table(name = "palestrante")
public class Palestrante implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_palestrante", nullable = false)
    private TipoPalestrante tipoPalestrante;

    // Para INDIVIDUAL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id") // Nullable se for CASAL ou GRUPO
    private Pessoa pessoaIndividual;

    // Para CASAL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casal_id", unique = true) // Nullable se for INDIVIDUAL ou GRUPO
    private Casal casal;

    // Para GRUPO
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "palestrante_grupo_pessoa",
        joinColumns = @JoinColumn(name = "palestrante_id"),
        inverseJoinColumns = @JoinColumn(name = "pessoa_id")
    )
    private Set<Pessoa> membrosGrupo = new HashSet<>(); // Nullable se for INDIVIDUAL ou CASAL

    // ManyToMany relationship with Palestra
    // A Palestrante can give multiple Palestras
    @ManyToMany(mappedBy = "palestrantes")
    private Set<Palestra> palestras = new HashSet<>();

    // Construtores
    public Palestrante() {
    }

    // Construtor para INDIVIDUAL
    public Palestrante(Pessoa pessoaIndividual) {
        this.tipoPalestrante = TipoPalestrante.INDIVIDUAL;
        this.pessoaIndividual = pessoaIndividual;
    }

    // Construtor para CASAL
    public Palestrante(Casal casal) {
        this.tipoPalestrante = TipoPalestrante.CASAL;
        this.casal = casal;
    }

	/*
	 * // Construtor para GRUPO public Palestrante(Set<Pessoa> membrosGrupo) { if
	 * (membrosGrupo == null || membrosGrupo.size() <= 1) { throw new
	 * IllegalArgumentException("Grupo deve ter pelo menos 2 membros."); }
	 * //this.tipoPalestrante = TipoPalestrante.GRUPO; this.membrosGrupo =
	 * membrosGrupo; }
	 */

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoPalestrante getTipoPalestrante() {
        return tipoPalestrante;
    }

    public void setTipoPalestrante(TipoPalestrante tipoPalestrante) {
        this.tipoPalestrante = tipoPalestrante;
    }

    public Pessoa getPessoaIndividual() {
        return pessoaIndividual;
    }

    public void setPessoaIndividual(Pessoa pessoaIndividual) {
        this.pessoaIndividual = pessoaIndividual;
    }

    public Casal getCasal() {
        return casal;
    }

    public void setCasal(Casal casal) {
        this.casal = casal;
    }

    public Set<Pessoa> getMembrosGrupo() {
        return membrosGrupo;
    }

    public void setMembrosGrupo(Set<Pessoa> membrosGrupo) {
        this.membrosGrupo = membrosGrupo;
    }

    public Set<Palestra> getPalestras() {
        return palestras;
    }

    public void setPalestras(Set<Palestra> palestras) {
        this.palestras = palestras;
    }

    // Métodos auxiliares para gerenciar a relação ManyToMany com Palestra
    public void adicionarPalestra(Palestra palestra) {
        this.palestras.add(palestra);
        palestra.getPalestrantes().add(this);
    }

    public void removerPalestra(Palestra palestra) {
        this.palestras.remove(palestra);
        palestra.getPalestrantes().remove(this);
    }
    
    // Método para obter o nome do palestrante (simplificado)
    public String getNomeDisplay() {
        // Verificar se tipoPalestrante é null
        if (tipoPalestrante == null) {
            return "Tipo não definido";
        }
        
        switch (tipoPalestrante) {
            case INDIVIDUAL:
                return pessoaIndividual != null ? pessoaIndividual.getNome() : "Indefinido";
            case CASAL:
                return casal != null ? "Casal: " + casal.getPessoa1().getNome() + " e " + casal.getPessoa2().getNome() : "Indefinido";
			/*
			 * case GRUPO: StringBuilder nomes = new StringBuilder("Grupo: "); if
			 * (membrosGrupo != null && !membrosGrupo.isEmpty()) { int count = 0; for
			 * (Pessoa p : membrosGrupo) { nomes.append(p.getNome()); if (++count <
			 * membrosGrupo.size()) { nomes.append(", "); } } } else {
			 * nomes.append("Indefinido"); } return nomes.toString();
			 */
            default:
                return "Tipo desconhecido";
        }
    }

    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Palestrante that = (Palestrante) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Palestrante{" +
                "id=" + id +
                ", tipoPalestrante=" + tipoPalestrante +
                ", nomeDisplay=\'" + getNomeDisplay() + "\'" +
                '}';
    }
}

