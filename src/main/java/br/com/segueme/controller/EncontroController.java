package br.com.segueme.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Encontro;
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
            
            auditoriaService.registrar("Encontro", encontro.getId() , "DESATIVADO", usuarioService.getUsuarioLogadoNome(),
            		"Encontro desativado juntamento com seus trabalhadores, equipes e encontristas"); 

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Encontro e relacionados desativados com sucesso!"));
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
            encontroService.buscarPorId(id).ifPresent(e -> this.encontro = e);
        }
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
