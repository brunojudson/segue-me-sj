package br.com.segueme.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import br.com.segueme.enums.Escolaridade;
import br.com.segueme.enums.Sacramento;
import br.com.segueme.enums.StatusTransferencia;

@Entity
@Table(name = "pessoa", schema = "public")
public class Pessoa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "cpf", nullable = true, length = 14, unique = true)
    private String cpf;

    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve ser no passado")
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    @Column(name = "endereco", length = 200)
    private String endereco;

    @Pattern(regexp = "\\(\\d{2}\\) \\d{4,5}-\\d{4}", message = "Telefone deve estar no formato (00) 00000-0000")
    @Column(name = "telefone", length = 15, unique = true)
    private String telefone;

    @Email(message = "Email deve ser válido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
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

    @Transient
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

    /** Relacionamento com o pai (se cadastrado como pessoa). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pai_id")
    private Pessoa pai;

    /** Relacionamento com a mãe (se cadastrada como pessoa). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mae_id")
    private Pessoa mae;

    /** Lista de filhos onde esta pessoa é o pai. */
    @OneToMany(mappedBy = "pai", fetch = FetchType.LAZY)
    private Set<Pessoa> filhosDoPai = new HashSet<>();

    /** Lista de filhos onde esta pessoa é a mãe. */
    @OneToMany(mappedBy = "mae", fetch = FetchType.LAZY)
    private Set<Pessoa> filhosDaMae = new HashSet<>();

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

    /**
     * Status de transferência do seguidor.
     * - ATIVO_ORIGEM       : pertence a esta paróquia e pode trabalhar no encontro.
     * - TRANSFERIDO_SAIDA  : transferido para outra paróquia; NÃO pode trabalhar no encontro.
     * - TRANSFERIDO_ENTRADA: aceito vindo de outra paróquia; pode trabalhar no encontro.
     * - RETORNADO          : voltou para a paróquia de origem; pode trabalhar no encontro.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_transferencia", length = 30, nullable = false)
    private StatusTransferencia statusTransferencia = StatusTransferencia.ATIVO_ORIGEM;

    /** Nome da paróquia de origem quando o seguidor veio de outra paróquia. */
    @Size(max = 150, message = "Paróquia de origem deve ter no máximo 150 caracteres")
    @Column(name = "paroquia_origem", length = 150)
    private String paroquiaOrigem;

    /** Histórico de transferências deste seguidor. */
    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Transferencia> transferencias = new ArrayList<>();

    // Construtores
    public Pessoa() {
        this.dataCadastro = LocalDateTime.now();
        this.ativo = true;
        this.sacramentos = new ArrayList<>();
        this.transferencias = new ArrayList<>();
        this.statusTransferencia = StatusTransferencia.ATIVO_ORIGEM;
        this.filhosDoPai = new HashSet<>();
        this.filhosDaMae = new HashSet<>();
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
        if (this.dataNascimento != null) {
            return java.time.Period.between(this.dataNascimento, java.time.LocalDate.now()).getYears();
        }
        return null;
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

	public StatusTransferencia getStatusTransferencia() {
		return statusTransferencia;
	}

	public void setStatusTransferencia(StatusTransferencia statusTransferencia) {
		this.statusTransferencia = statusTransferencia;
	}

	public String getParoquiaOrigem() {
		return paroquiaOrigem;
	}

	public void setParoquiaOrigem(String paroquiaOrigem) {
		this.paroquiaOrigem = paroquiaOrigem;
	}

	public List<Transferencia> getTransferencias() {
		return transferencias;
	}

	public void setTransferencias(List<Transferencia> transferencias) {
		this.transferencias = transferencias;
	}

	public Pessoa getPai() {
		return pai;
	}

	public void setPai(Pessoa pai) {
		this.pai = pai;
	}

	public Pessoa getMae() {
		return mae;
	}

	public void setMae(Pessoa mae) {
		this.mae = mae;
	}

	public Set<Pessoa> getFilhosDoPai() {
		return filhosDoPai;
	}

	public void setFilhosDoPai(Set<Pessoa> filhosDoPai) {
		this.filhosDoPai = filhosDoPai;
	}

	public Set<Pessoa> getFilhosDaMae() {
		return filhosDaMae;
	}

	public void setFilhosDaMae(Set<Pessoa> filhosDaMae) {
		this.filhosDaMae = filhosDaMae;
	}

	/**
	 * Indica se este seguidor pode ser inscrito como trabalhador em um encontro.
	 * Seguidores com status TRANSFERIDO_SAIDA (que saíram desta paróquia)
	 * não podem trabalhar em nenhum encontro.
	 */
	@Transient
	public boolean podeTrabalharNoEncontro() {
		return statusTransferencia != StatusTransferencia.TRANSFERIDO_SAIDA;
	}

	/**
	 * Retorna todos os filhos desta pessoa (tanto do lado paterno quanto materno).
	 */
	@Transient
	public Set<Pessoa> getTodosFilhos() {
		Set<Pessoa> todosFilhos = new HashSet<>();
		if (filhosDoPai != null) {
			todosFilhos.addAll(filhosDoPai);
		}
		if (filhosDaMae != null) {
			todosFilhos.addAll(filhosDaMae);
		}
		return todosFilhos;
	}

	/**
	 * Verifica se esta pessoa tem parentesco próximo com outra pessoa.
	 * Considera: pai/mãe, filho(a) ou irmão(ã).
	 * 
	 * @param outraPessoa A pessoa a ser verificada
	 * @return true se houver parentesco próximo, false caso contrário
	 */
	@Transient
	public boolean ehParenteProximoDe(Pessoa outraPessoa) {
		if (outraPessoa == null || outraPessoa.getId() == null) {
			return false;
		}
		
		// Não pode ser a mesma pessoa
		if (this.id != null && this.id.equals(outraPessoa.getId())) {
			return false;
		}
		
		// Verifica se esta pessoa é pai/mãe da outra
		if (outraPessoa.getPai() != null && outraPessoa.getPai().getId() != null 
				&& this.id != null && this.id.equals(outraPessoa.getPai().getId())) {
			return true;
		}
		if (outraPessoa.getMae() != null && outraPessoa.getMae().getId() != null 
				&& this.id != null && this.id.equals(outraPessoa.getMae().getId())) {
			return true;
		}
		
		// Verifica se esta pessoa é filho(a) da outra
		if (this.pai != null && this.pai.getId() != null 
				&& this.pai.getId().equals(outraPessoa.getId())) {
			return true;
		}
		if (this.mae != null && this.mae.getId() != null 
				&& this.mae.getId().equals(outraPessoa.getId())) {
			return true;
		}
		
		// Verifica se são irmãos (mesmo pai)
		if (this.pai != null && this.pai.getId() != null 
				&& outraPessoa.getPai() != null && outraPessoa.getPai().getId() != null
				&& this.pai.getId().equals(outraPessoa.getPai().getId())) {
			return true;
		}
		
		// Verifica se são irmãos (mesma mãe)
		if (this.mae != null && this.mae.getId() != null 
				&& outraPessoa.getMae() != null && outraPessoa.getMae().getId() != null
				&& this.mae.getId().equals(outraPessoa.getMae().getId())) {
			return true;
		}
		
		return false;
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

