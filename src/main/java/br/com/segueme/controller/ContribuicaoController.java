package br.com.segueme.controller;

import br.com.segueme.entity.Contribuicao;
import br.com.segueme.entity.Trabalhador;
import br.com.segueme.service.ContribuicaoService;
import br.com.segueme.service.TrabalhadorService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Named
@ViewScoped
public class ContribuicaoController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private ContribuicaoService contribuicaoService;
    
    @Inject
    private TrabalhadorService trabalhadorService;
    
    private List<Contribuicao> contribuicoes;
    private Contribuicao contribuicao;
    private Contribuicao contribuicaoSelecionada;
    
    private List<Trabalhador> trabalhadores;
    
    @PostConstruct
    public void init() {
        carregarContribuicoes();
        carregarTrabalhadores();
        limpar();
    }
    
    public void carregarContribuicoes() {
        contribuicoes = contribuicaoService.buscarTodos();
    }
    
    public void carregarTrabalhadores() {
        trabalhadores = trabalhadorService.buscarTodosAtivos();
    }
    
    public void limpar() {
        contribuicao = new Contribuicao();
        contribuicao.setDataPagamento(LocalDate.now());
    }
    
    public String salvar() {
        try {
            if (contribuicao.getId() == null) {
                contribuicaoService.salvar(contribuicao);
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Contribuição cadastrada com sucesso!"));
            } else {
                contribuicaoService.atualizar(contribuicao);
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Contribuição atualizada com sucesso!"));
            }
            
            carregarContribuicoes();
            limpar();
            return "lista?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
            return null;
        }
    }
    
    public String salvarEContinuar() {
        try {
            if (contribuicao.getId() == null) {
                contribuicaoService.salvar(contribuicao);
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Contribuição cadastrada com sucesso!"));
            } else {
                contribuicaoService.atualizar(contribuicao);
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Contribuição atualizada com sucesso!"));
            }
            
            carregarContribuicoes();
            limpar();
            return "";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
            return null;
        }
    }
    
    public String visualizar(Contribuicao contribuicao) {
        this.contribuicaoSelecionada = contribuicao;
        return "visualizar?faces-redirect=true&id=" + contribuicao.getId();
    }
    
    public String editar(Contribuicao contribuicao) {
        this.contribuicao = contribuicao;
        return "cadastro?faces-redirect=true&id=" + contribuicao.getId();
    }
    
    public void prepararExclusao(Contribuicao contribuicao) {
        this.contribuicaoSelecionada = contribuicao;
    }
    
    public void excluir() {
        try {
            contribuicaoService.remover(contribuicaoSelecionada.getId());
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Contribuição excluída com sucesso!"));
            carregarContribuicoes();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
    }
    
    public void carregarContribuicao() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null && !idParam.isEmpty()) {
            Long id = Long.valueOf(idParam);
            contribuicaoService.buscarPorId(id).ifPresent(c -> this.contribuicao = c);
        }
    }
    
    // Getters e Setters
    
    public List<Contribuicao> getContribuicoes() {
        return contribuicoes;
    }

    public void setContribuicoes(List<Contribuicao> contribuicoes) {
        this.contribuicoes = contribuicoes;
    }

    public Contribuicao getContribuicao() {
        return contribuicao;
    }

    public void setContribuicao(Contribuicao contribuicao) {
        this.contribuicao = contribuicao;
    }

    public Contribuicao getContribuicaoSelecionada() {
        return contribuicaoSelecionada;
    }

    public void setContribuicaoSelecionada(Contribuicao contribuicaoSelecionada) {
        this.contribuicaoSelecionada = contribuicaoSelecionada;
    }
    
    public List<Trabalhador> getTrabalhadores() {
        return trabalhadores;
    }

    public void setTrabalhadores(List<Trabalhador> trabalhadores) {
        this.trabalhadores = trabalhadores;
    }
}
