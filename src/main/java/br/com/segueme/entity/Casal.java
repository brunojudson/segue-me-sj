package br.com.segueme.entity;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "casal", schema="public", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"pessoa1_id", "pessoa2_id"})
})
public class Casal implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pessoa1_id", nullable = false)
    private Pessoa pessoa1;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pessoa2_id", nullable = false)
    private Pessoa pessoa2;
    
    @Column(name = "data_casamento")
    private LocalDate dataCasamento;
    
    @Column(name = "observacoes")
    private String observacoes;
    
    @Column(name = "foto")
    private String foto;
    
    // Construtores
    public Casal() {
    }
    
    public Casal(Pessoa pessoa1, Pessoa pessoa2) {
        this.pessoa1 = pessoa1;
        this.pessoa2 = pessoa2;
    }
    
    public Casal(Pessoa pessoa1, Pessoa pessoa2, LocalDate dataCasamento) {
        this(pessoa1, pessoa2);
        this.dataCasamento = dataCasamento;
    }
    // Casal.java
    @Transient
    public String getFotoUrl() {
        if (this.getFoto() != null && !this.getFoto().isEmpty()) {
            return "/api/casais/" + this.getId() + "/foto";
        }
        return null;
    }
    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pessoa getPessoa1() {
        return pessoa1;
    }

    public void setPessoa1(Pessoa pessoa1) {
        this.pessoa1 = pessoa1;
    }

    public Pessoa getPessoa2() {
        return pessoa2;
    }

    public void setPessoa2(Pessoa pessoa2) {
        this.pessoa2 = pessoa2;
    }

    public LocalDate getDataCasamento() {
        return dataCasamento;
    }

    public void setDataCasamento(LocalDate dataCasamento) {
        this.dataCasamento = dataCasamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
    

    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Casal casal = (Casal) o;
        return Objects.equals(id, casal.id) || 
               (Objects.equals(pessoa1, casal.pessoa1) && Objects.equals(pessoa2, casal.pessoa2)) ||
               (Objects.equals(pessoa1, casal.pessoa2) && Objects.equals(pessoa2, casal.pessoa1));
    }

    @Override
    public int hashCode() {
        // Ordem independente para pessoa1 e pessoa2
        return Objects.hash(id, 
                           (pessoa1 != null && pessoa2 != null) ? 
                           (pessoa1.hashCode() + pessoa2.hashCode()) : 0);
    }
    
    @Override
    public String toString() {
        return "Casal{" +
                "id=" + id +
                ", pessoa1=" + (pessoa1 != null ? pessoa1.getNome() : "null") +
                ", pessoa2=" + (pessoa2 != null ? pessoa2.getNome() : "null") +
                ", dataCasamento=" + dataCasamento +
                '}';
    }
}
