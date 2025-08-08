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

import br.com.segueme.entity.Dirigente;
import br.com.segueme.entity.Pasta;
import br.com.segueme.entity.Trabalhador;
import br.com.segueme.service.DirigenteService;
import br.com.segueme.service.PastaService;
import br.com.segueme.service.TrabalhadorService;

@Named
@ViewScoped
public class DirigenteController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private DirigenteService dirigenteService;

    @Inject
    private TrabalhadorService trabalhadorService;

    @Inject
    private PastaService pastaService;

    private List<Dirigente> dirigentes;
    private Dirigente dirigente;
    private Dirigente dirigenteSelecionado;

    private List<Trabalhador> trabalhadores;
    private List<Pasta> pastas;

    @PostConstruct
    public void init() {
        carregarDirigentes();
        carregarTrabalhadores();
        carregarPastas();
        limpar();
    }

    public void carregarDirigentes() {
        dirigentes = dirigenteService.buscarTodos();
    }

    public void carregarTrabalhadores() {
        trabalhadores = trabalhadorService.buscarTodosDistintos();
        //trabalhadores.sort((t1, t2) -> t1.getPessoa().getNome().compareToIgnoreCase(t2.getPessoa().getNome()));
    }

    public void carregarPastas() {
        pastas = pastaService.buscarVigentes();
    }

    public void limpar() {
        dirigente = new Dirigente();
        dirigente.setAtivo(true);
        dirigente.setDataInicio(LocalDate.now());
        // Definir data de fim como 2 anos após a data de início (mandato padrão)
        dirigente.setDataFim(LocalDate.now().plusYears(2));
    }

    public String salvar() {
        try {
            if (dirigente.getId() == null) {
                dirigenteService.salvar(dirigente);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Dirigente cadastrado com sucesso!"));
            } else {
                dirigenteService.atualizar(dirigente);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Dirigente atualizado com sucesso!"));
            }

            carregarDirigentes();
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
            if (dirigente.getId() == null) {
                dirigenteService.salvar(dirigente);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Dirigente cadastrado com sucesso!"));
            } else {
                dirigenteService.atualizar(dirigente);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Dirigente atualizado com sucesso!"));
            }

            carregarDirigentes();
            limpar();
            return "lista?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
            return null;
        }
    }

    public String visualizar(Dirigente dirigente) {
        this.dirigenteSelecionado = dirigente;
        return "visualizar?faces-redirect=true&id=" + dirigente.getId();
    }

    public String editar(Dirigente dirigente) {
        this.dirigente = dirigente;
        return "cadastro?faces-redirect=true&id=" + dirigente.getId();
    }

    public void prepararExclusao(Dirigente dirigente) {
        this.dirigenteSelecionado = dirigente;
    }

    public void excluir() {
        try {
            dirigenteService.remover(dirigenteSelecionado.getId());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Dirigente excluído com sucesso!"));
            carregarDirigentes();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
    }

    public void carregarDirigente() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null && !idParam.isEmpty()) {
            Long id = Long.valueOf(idParam);
            dirigenteService.buscarPorId(id).ifPresent(d -> this.dirigente = d);
        }
    }

    // Getters e Setters

    public List<Dirigente> getDirigentes() {
        return dirigentes;
    }

    public void setDirigentes(List<Dirigente> dirigentes) {
        this.dirigentes = dirigentes;
    }

    public Dirigente getDirigente() {
        return dirigente;
    }

    public void setDirigente(Dirigente dirigente) {
        this.dirigente = dirigente;
    }

    public Dirigente getDirigenteSelecionado() {
        return dirigenteSelecionado;
    }

    public void setDirigenteSelecionado(Dirigente dirigenteSelecionado) {
        this.dirigenteSelecionado = dirigenteSelecionado;
    }

    public List<Trabalhador> getTrabalhadores() {
        return trabalhadores;
    }

    public void setTrabalhadores(List<Trabalhador> trabalhadores) {
        this.trabalhadores = trabalhadores;
    }

    public List<Pasta> getPastas() {
        return pastas;
    }

    public void setPastas(List<Pasta> pastas) {
        this.pastas = pastas;
    }
}
