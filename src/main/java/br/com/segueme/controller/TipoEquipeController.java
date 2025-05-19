package br.com.segueme.controller;

import br.com.segueme.entity.TipoEquipe;
import br.com.segueme.service.TipoEquipeService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class TipoEquipeController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private TipoEquipeService tipoEquipeService;
    
    private List<TipoEquipe> tiposEquipe;
    private TipoEquipe tipoEquipe;
    private TipoEquipe tipoEquipeSelecionado;
    
    @PostConstruct
    public void init() {
        carregarTiposEquipe();
        limpar();
    }
    
    public void carregarTiposEquipe() {
        tiposEquipe = tipoEquipeService.buscarTodos();
    }
    
    public void limpar() {
        tipoEquipe = new TipoEquipe();
        tipoEquipe.setAtivo(true);
    }
    
    public String salvar() {
        try {
            if (tipoEquipe.getId() == null) {
                tipoEquipeService.salvar(tipoEquipe);
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Tipo de equipe cadastrado com sucesso!"));
            } else {
                tipoEquipeService.atualizar(tipoEquipe);
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Tipo de equipe atualizado com sucesso!"));
            }
            
            carregarTiposEquipe();
            limpar();
            return "lista?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
            return null;
        }
    }
    
    public String visualizar(TipoEquipe tipoEquipe) {
        this.tipoEquipeSelecionado = tipoEquipe;
        return "visualizar?faces-redirect=true&id=" + tipoEquipe.getId();
    }
    
    public String editar(TipoEquipe tipoEquipe) {
        this.tipoEquipe = tipoEquipe;
        return "cadastro?faces-redirect=true&id=" + tipoEquipe.getId();
    }
    
    public void prepararExclusao(TipoEquipe tipoEquipe) {
        this.tipoEquipeSelecionado = tipoEquipe;
    }
    
    public void excluir() {
        try {
            tipoEquipeService.remover(tipoEquipeSelecionado.getId());
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Tipo de equipe excluído com sucesso!"));
            carregarTiposEquipe();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
    }
    
    public void carregarTipoEquipe() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null && !idParam.isEmpty()) {
            Long id = Long.valueOf(idParam);
            tipoEquipeService.buscarPorId(id).ifPresent(t -> this.tipoEquipe = t);
        }
    }
    
    // Getters e Setters
    
    public List<TipoEquipe> getTiposEquipe() {
        return tiposEquipe;
    }

    public void setTiposEquipe(List<TipoEquipe> tiposEquipe) {
        this.tiposEquipe = tiposEquipe;
    }

    public TipoEquipe getTipoEquipe() {
        return tipoEquipe;
    }

    public void setTipoEquipe(TipoEquipe tipoEquipe) {
        this.tipoEquipe = tipoEquipe;
    }

    public TipoEquipe getTipoEquipeSelecionado() {
        return tipoEquipeSelecionado;
    }

    public void setTipoEquipeSelecionado(TipoEquipe tipoEquipeSelecionado) {
        this.tipoEquipeSelecionado = tipoEquipeSelecionado;
    }
}
