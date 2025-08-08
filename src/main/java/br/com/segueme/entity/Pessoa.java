package br.com.segueme.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.com.segueme.enums.Escolaridade;
import br.com.segueme.enums.Sacramento;

@Entity
@Table(name = "pessoa", schema = "public")
public class Pessoa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "cpf", nullable = true, length = 14, unique = true)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "endereco", length = 200)
    private String endereco;

    @Column(name = "telefone", length = 15, unique = true)
    private String telefone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "sexo", length = 1)
    private Character sexo;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "foto")
    private String foto;

    @Column(name = "idade")
    private Integer idade;

    @ElementCollection(targetClass = Sacramento.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "pessoa_sacramento", joinColumns = @JoinColumn(name = "pessoa_id"))
    @Column(name = "sacramento", nullable = true)
    private List<Sacramento> sacramentos;

    @Column(name = "naturalidade", length = 100)
    private String naturalidade;

    @Column(name = "filiacao_pai", length = 100)
    private String filiacaoPai;

    @Column(name = "filiacao_mae", length = 100)
    private String filiacaoMae;

    @Enumerated(EnumType.STRING)
    @Column(name = "escolaridade", length = 50)
    private Escolaridade escolaridade;

    @Column(name = "curso", length = 100) // Se estudante ou para formação
    private String curso;

    @Column(name = "instituicao_ensino", length = 100) // Se estudante ou para formação
    private String instituicaoEnsino;

    @Column(name = "religiao", length = 100)
    private String religiao;

    @Column(name = "igreja_frequenta", length = 100)
    private String igrejaFrequenta;

    @Column(name = "movimento_participou", length = 200) // Pode ser mais de um, separado por vírgula, ou uma descrição
    private String movimentoParticipou;

    // Construtores
    public Pessoa() {
        this.dataCadastro = LocalDateTime.now();
        this.ativo = true;
        this.sacramentos = new ArrayList<>(); // <<< ADICIONAR ESTA LINHA
    }

    public Pessoa(String nome, String cpf, LocalDate dataNascimento) {
        this();
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Character getSexo() {
        return sexo;
    }

    public void setSexo(Character sexo) {
        this.sexo = sexo;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public List<Sacramento> getSacramentos() {
		return sacramentos;
	}

	public void setSacramentos(List<Sacramento> sacramentos) {
		this.sacramentos = sacramentos;
	}

	public String getNaturalidade() {
		return naturalidade;
	}

	public void setNaturalidade(String naturalidade) {
		this.naturalidade = naturalidade;
	}

	public String getFiliacaoPai() {
		return filiacaoPai;
	}

	public void setFiliacaoPai(String filiacaoPai) {
		this.filiacaoPai = filiacaoPai;
	}

	public String getFiliacaoMae() {
		return filiacaoMae;
	}

	public void setFiliacaoMae(String filiacaoMae) {
		this.filiacaoMae = filiacaoMae;
	}

    public Escolaridade getEscolaridade() {
		return escolaridade;
	}

	public void setEscolaridade(Escolaridade escolaridade) {
		this.escolaridade = escolaridade;
	}

	public String getCurso() {
		return curso;
	}

	public void setCurso(String curso) {
		this.curso = curso;
	}

	public String getInstituicaoEnsino() {
		return instituicaoEnsino;
	}

	public void setInstituicaoEnsino(String instituicaoEnsino) {
		this.instituicaoEnsino = instituicaoEnsino;
	}

	public String getReligiao() {
		return religiao;
	}

	public void setReligiao(String religiao) {
		this.religiao = religiao;
	}

	public String getIgrejaFrequenta() {
		return igrejaFrequenta;
	}

	public void setIgrejaFrequenta(String igrejaFrequenta) {
		this.igrejaFrequenta = igrejaFrequenta;
	}

	public String getMovimentoParticipou() {
		return movimentoParticipou;
	}

	public void setMovimentoParticipou(String movimentoParticipou) {
		this.movimentoParticipou = movimentoParticipou;
	}

	@Transient
    public String getFotoUrl() {
        if (this.getFoto() != null && !this.getFoto().isEmpty()) {
            return "/api/pessoas/" + this.getId() + "/foto";
        }
        return null;
    }

    public void calcularIdade() {
        if (this.dataNascimento != null) {
            this.idade = java.time.Period.between(this.dataNascimento, java.time.LocalDate.now()).getYears();
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
        Pessoa pessoa = (Pessoa) o;
        return Objects.equals(id, pessoa.id) &&
                Objects.equals(cpf, pessoa.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cpf);
    }

    @Override
	public String toString() {
		return "Pessoa [id=" + id + ", nome=" + nome + ", cpf=" + cpf + ", dataNascimento=" + dataNascimento
				+ ", endereco=" + endereco + ", telefone=" + telefone + ", email=" + email + ", sexo=" + sexo
				+ ", dataCadastro=" + dataCadastro + ", ativo=" + ativo + ", foto=" + foto + ", idade=" + idade + "]";
	}
}

