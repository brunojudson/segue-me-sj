package br.com.segueme.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

import br.com.segueme.enums.TemaPalestra;
import java.time.LocalDateTime;

@Entity
@Table(name = "palestra")
public class Palestra implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tema", nullable = false)
    private TemaPalestra tema;

    @Column(name = "descricao", length = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    // ManyToMany relationship with Palestrante
    // A Palestra can have multiple Palestrantes
    @ManyToMany()
    @JoinTable(
        name = "palestra_palestrante",
        joinColumns = @JoinColumn(name = "palestra_id"),
        inverseJoinColumns = @JoinColumn(name = "palestrante_id")
    )
    private Set<Palestrante> palestrantes = new HashSet<>();

    // Construtores
    public Palestra() {
    }

    public Palestra(String titulo, TemaPalestra tema, Encontro encontro) {
        this.titulo = titulo;
        this.tema = tema;
        this.encontro = encontro;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public TemaPalestra getTema() {
        return tema;
    }

    public void setTema(TemaPalestra tema) {
        this.tema = tema;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public void setEncontro(Encontro encontro) {
        this.encontro = encontro;
    }

    public Set<Palestrante> getPalestrantes() {
        return palestrantes;
    }

    public void setPalestrantes(Set<Palestrante> palestrantes) {
        this.palestrantes = palestrantes;
    }
    

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    // Métodos auxiliares para gerenciar a relação ManyToMany
    public void adicionarPalestrante(Palestrante palestrante) {
        this.palestrantes.add(palestrante);
        palestrante.getPalestras().add(this);
    }

    public void removerPalestrante(Palestrante palestrante) {
        this.palestrantes.remove(palestrante);
        palestrante.getPalestras().remove(this);
    }

    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Palestra palestra = (Palestra) o;
        return Objects.equals(id, palestra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Palestra{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", tema=" + tema +
                ", encontro=" + (encontro != null ? encontro.getNome() : "null") +
                '}';
    }
}

