package br.com.segueme.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.Encontrista;
import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Palestra;
import br.com.segueme.enums.Circulo;
import br.com.segueme.service.AuditoriaService;
import br.com.segueme.service.EncontristaService;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.EquipeService;
import br.com.segueme.service.TrabalhadorService;
import br.com.segueme.service.UsuarioService;

@Named
@ViewScoped
public class EncontroController implements Serializable {

	private static final long serialVersionUID = 1L;

	@PersistenceContext
    private EntityManager entityManager;

	@Inject
	private transient EncontroService encontroService;

	@Inject
	private TrabalhadorService trabalhadorService;

	@Inject
	private EquipeService equipeService;

	@Inject
	private EncontristaService encontristaService;

	@Inject
	private AuditoriaService auditoriaService;

	@Inject
	private UsuarioService usuarioService;

	private List<Encontro> encontros;
	private Encontro encontro;
	private Encontro encontroSelecionado;

	private Encontro encontroSelecionadoCirculos;
	private List<Encontrista> integrantesSelecionados;
    private Circulo circuloSelecionado;
	
	private List<Palestra> palestrasPorData;

	@PostConstruct
	public void init() {
		carregarEncontros();
		limpar();
	}

	public void carregarEncontros() {
		encontros = encontroService.buscarTodos();
	}

	public void limpar() {
		encontro = new Encontro();
	}

	// Método para desativar o encontro, trabalhadores e equipes
	public void desativar() {

		try {
			// Desativar o encontro
			encontro.setAtivo(false);
			encontroService.atualizar(encontro);

			// Desativar trabalhadores associados ao encontro
			trabalhadorService.desativarPorEncontro(encontro.getId());

			// Desativar equipes associadas ao encontro
			equipeService.desativarPorEncontro(encontro.getId());

			// Desativar encontristas associados ao encontro
			encontristaService.desativarPorEncontro(encontro.getId());

			auditoriaService.registrar("Encontro", encontro.getId(), "DESATIVADO",
					usuarioService.getUsuarioLogadoNome(),
					"Encontro desativado juntamento com seus trabalhadores, equipes e encontristas");

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
					"Encontro e relacionados desativados com sucesso!"));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao desativar: " + e.getMessage()));
		}
	}

	public String salvar() {
		try {
			if (encontro.getId() == null) {
				encontroService.salvar(encontro);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Encontro cadastrado com sucesso!"));
			} else {
				encontroService.atualizar(encontro);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Encontro atualizado com sucesso!"));
			}

			carregarEncontros();
			limpar();
			return "lista?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
			return null;
		}
	}

	public String visualizar(Encontro encontro) {
		this.encontroSelecionado = encontro;
		return "visualizar?faces-redirect=true&id=" + encontro.getId();
	}

	public String editar(Encontro encontro) {
		this.encontro = encontro;
		return "cadastro?faces-redirect=true&id=" + encontro.getId();
	}

	public void prepararExclusao(Encontro encontro) {
		this.encontroSelecionado = encontro;
	}

	public void excluir() {
		try {
			encontroService.remover(encontroSelecionado.getId());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Encontro excluído com sucesso!"));
			carregarEncontros();
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public void carregarEncontro() {
		String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		if (idParam != null && !idParam.isEmpty()) {
			Long id = Long.valueOf(idParam);
			encontroService.buscarPorId(id).ifPresent(e -> {
	            this.encontro = e;
	            this.encontroSelecionadoCirculos = e;
	            carregarPalestrasPorData();
			});
		}
		this.encontroSelecionado = this.encontro;
	}
	
	// Método para carregar e ordenar as palestras
	private void carregarPalestrasPorData() {
		if (encontro != null && encontro.getPalestras() != null) {
			palestrasPorData = new ArrayList<>(encontro.getPalestras());
		
		} else {
			palestrasPorData = new ArrayList<>();
		}
	}

	//INICIO DA INFORMAÇÃO DE CIRCULOS
    // Retorna todos os círculos
    public Circulo[] getCirculos() {
        return Circulo.values();
    }

    // Busca os integrantes do círculo selecionado no encontro selecionado
    public void selecionarCirculo(Circulo circulo) {
        this.circuloSelecionado = circulo;
        if (encontroSelecionadoCirculos != null && circulo != null) {
        	integrantesSelecionados = entityManager.createQuery(
        		    "SELECT e FROM Encontrista e JOIN FETCH e.pessoa JOIN FETCH e.encontro WHERE e.encontro = :encontro AND e.circulo = :circulo ORDER BY e.pessoa.nome", 
        		    Encontrista.class)
        		    .setParameter("encontro", encontroSelecionadoCirculos)
        		    .setParameter("circulo", circulo)
        		    .getResultList();
        } else {
            integrantesSelecionados = new ArrayList<>();
        }
    }

    // Busca todos os encontristas do encontro selecionado
    public long contarIntegrantesPorCirculo(Circulo circulo) {
        if (encontroSelecionadoCirculos == null) return 0;
        Long count = entityManager.createQuery(
            "SELECT COUNT(e) FROM Encontrista e WHERE e.encontro = :encontro AND e.circulo = :circulo", 
            Long.class)
            .setParameter("encontro", encontroSelecionadoCirculos)
            .setParameter("circulo", circulo)
            .getSingleResult();
        return count != null ? count : 0;
    }

    // Getters e setters
    public Encontro getEncontroSelecionadoCirculos() {
        return encontroSelecionadoCirculos;
    }

    public void setEncontroSelecionadoCirculos(Encontro encontroSelecionadoCirculos) {
        this.encontroSelecionadoCirculos = encontroSelecionadoCirculos;
    }

    public List<Encontrista> getIntegrantesSelecionados() {
        return integrantesSelecionados;
    }

    public Circulo getCirculoSelecionado() {
        return circuloSelecionado;
    }
    //FIM DA INFORMAÇÃO DE CIRCULOS

	// Getter para palestrasPorData
	public List<Palestra> getPalestrasPorData() {
		if (palestrasPorData == null && encontro != null) {
			carregarPalestrasPorData();
		}
		return palestrasPorData;
	}

	// Getters e Setters

	public List<Encontro> getEncontros() {
		return encontros;
	}

	public void setEncontros(List<Encontro> encontros) {
		this.encontros = encontros;
	}

	public Encontro getEncontro() {
		return encontro;
	}

	public void setEncontro(Encontro encontro) {
		this.encontro = encontro;
	}

	public Encontro getEncontroSelecionado() {
		return encontroSelecionado;
	}

	public void setEncontroSelecionado(Encontro encontroSelecionado) {
		this.encontroSelecionado = encontroSelecionado;
	}
}
