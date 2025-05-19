package br.com.segueme.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Encontrista;
import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Equipe;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.entity.Trabalhador;
import br.com.segueme.service.EncontristaService;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.EquipeService;
import br.com.segueme.service.PessoaService;
import br.com.segueme.service.TrabalhadorService;

@Named
@ViewScoped
public class TrabalhadorController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private TrabalhadorService trabalhadorService;

	@Inject
	private PessoaService pessoaService;

	@Inject
	private EquipeService equipeService;

	@Inject
	private transient EncontroService encontroService;

	@Inject
	private EncontristaService encontristaService;

	private List<Trabalhador> trabalhadores;
	private Trabalhador trabalhador;
	private Trabalhador trabalhadorSelecionado;

	private List<Pessoa> pessoas;
	private List<Equipe> equipes;
	private List<Encontro> encontros;

	@PostConstruct
	public void init() {
		carregarTrabalhadores();
		carregarPessoas();
		carregarEquipes();
		carregarEncontros();
		limpar();
	}

	public void carregarTrabalhadores() {
		trabalhadores = trabalhadorService.buscarTodos();
	}
	public void atualizarDataInicio() {
	    if (trabalhador.getEncontro() != null) {
	        // Define a data de início do encontro selecionado no trabalhador
	        trabalhador.setDataInicio(trabalhador.getEncontro().getDataInicio());
	    }
	}

	public void carregarPessoas() {
		pessoas = pessoaService.buscarTodosEcluindoEncotristasAtivos();
	}

	public void carregarEquipes() {
		equipes = equipeService.buscarAtivas();
	}

	public void carregarEncontros() {
		encontros = encontroService.buscarAtivos();
	}

	public void limpar() {
		trabalhador = new Trabalhador();
		trabalhador.setDataInicio(LocalDate.now());
	}

	public String salvar() {
		try {
			// Verificar se a pessoa já foi encontrista
			// Quando tudo estiver implementado, descomentar a linha abaixo
			if (trabalhador.getPessoa() != null) {
				List<Encontrista> encontristas = encontristaService.buscarPorPessoa(trabalhador.getPessoa().getId());
				trabalhador.setFoiEncontrista(!encontristas.isEmpty());
			}

			if (trabalhador.getId() == null) {
				trabalhadorService.salvar(trabalhador);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Trabalhador cadastrado com sucesso!"));
			} else {
				trabalhadorService.atualizar(trabalhador);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Trabalhador atualizado com sucesso!"));
			}

			carregarTrabalhadores();
			limpar();
			return "lista?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
			return null;
		}
	}

	public String visualizar(Trabalhador trabalhador) {
		this.trabalhadorSelecionado = trabalhador;
		return "visualizar?faces-redirect=true&id=" + trabalhador.getId();
	}

	public String editar(Trabalhador trabalhador) {
		this.trabalhador = trabalhador;
		return "cadastro?faces-redirect=true&id=" + trabalhador.getId();
	}

	public void prepararExclusao(Trabalhador trabalhador) {
		this.trabalhadorSelecionado = trabalhador;
	}

	public void excluir() {
		try {
			trabalhadorService.remover(trabalhadorSelecionado.getId());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Trabalhador excluído com sucesso!"));
			carregarTrabalhadores();
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public void carregarTrabalhador() {
		String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		if (idParam != null && !idParam.isEmpty()) {
			Long id = Long.valueOf(idParam);
			trabalhadorService.buscarPorId(id).ifPresent(t -> this.trabalhador = t);
		}
	}

	public void verificarSeEncontrista() {
		if (trabalhador.getPessoa() != null) {
			List<Encontrista> encontristas = encontristaService.buscarPorPessoa(trabalhador.getPessoa().getId());
			trabalhador.setFoiEncontrista(!encontristas.isEmpty());
		}
	}

	// Getters e Setters

	public List<Trabalhador> getTrabalhadores() {
		return trabalhadores;
	}

	public void setTrabalhadores(List<Trabalhador> trabalhadores) {
		this.trabalhadores = trabalhadores;
	}

	public Trabalhador getTrabalhador() {
		return trabalhador;
	}

	public void setTrabalhador(Trabalhador trabalhador) {
		this.trabalhador = trabalhador;
	}

	public Trabalhador getTrabalhadorSelecionado() {
		return trabalhadorSelecionado;
	}

	public void setTrabalhadorSelecionado(Trabalhador trabalhadorSelecionado) {
		this.trabalhadorSelecionado = trabalhadorSelecionado;
	}

	public List<Pessoa> getPessoas() {
		return pessoas;
	}

	public void setPessoas(List<Pessoa> pessoas) {
		this.pessoas = pessoas;
	}

	public List<Equipe> getEquipes() {
		return equipes;
	}

	public void setEquipes(List<Equipe> equipes) {
		this.equipes = equipes;
	}

	public List<Encontro> getEncontros() {
		return encontros;
	}

	public void setEncontros(List<Encontro> encontros) {
		this.encontros = encontros;
	}
}
