package br.com.segueme.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Dirigente;
import br.com.segueme.entity.Equipe;
import br.com.segueme.entity.Pasta;
import br.com.segueme.service.DirigenteService;
import br.com.segueme.service.EquipeService;
import br.com.segueme.service.PastaService;

@Named
@ViewScoped
public class PastaController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PastaService pastaService;

    @Inject
    private EquipeService equipeService;

    @Inject
    private DirigenteService dirigenteService;

    private List<Pasta> pastas;
    private Pasta pasta;
    private Pasta pastaSelecionada;

    private List<Equipe> equipesDirigentes;

    @PostConstruct
    public void init() {
        carregarPastas();
        carregarEquipesDirigentes();
        limpar();

    }

    public void carregarPastas() {
        pastas = pastaService.buscarTodos();
    }

    public void carregarEquipesDirigentes() {
        List<Equipe> todasEquipes = equipeService.buscarTodos();

        // Filtrar apenas uma equipe dirigente, se existir
        equipesDirigentes = todasEquipes.stream()
            .filter(Equipe::ehEquipeDirigente)
            .limit(1) // Limitar a apenas uma equipe
            .collect(Collectors.toList());
    }

    public void limpar() {
        pasta = new Pasta();
        pasta.setAtivo(true);
        pasta.setDataInicio(LocalDate.now());
        // Definir data de fim como 2 anos após a data de início (mandato padrão)
        pasta.setDataFim(LocalDate.now().plusYears(2));
    }

    public String salvar() {
    	System.out.println(pasta.isAtivo());
        try {
            if (pasta.getId() == null) {
                pastaService.salvar(pasta);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pasta cadastrada com sucesso!"));
            } else {
                pastaService.atualizar(pasta);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pasta atualizada com sucesso!"));
            }

            carregarPastas();
            limpar();
            return "lista?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
            return null;
        }
    }

    public String desativar() {
        try {
            // Desativar a pasta
            pasta.setAtivo(false);
            pastaService.atualizar(pasta);

            // Desativar todos os dirigentes relacionados à pasta
            List<Dirigente> dirigentesRelacionados = dirigenteService.buscarPorPasta(pasta.getId());
            for (Dirigente dirigente : dirigentesRelacionados) {
                dirigente.setAtivo(false);
                dirigenteService.atualizar(dirigente);
            }

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pasta e dirigentes finalizados com sucesso!"));

            carregarPastas();
            limpar();
            return "lista?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
            return null;
        }
    }

    public String visualizar(Pasta pasta) {
        this.pastaSelecionada = pasta;
        return "visualizar?faces-redirect=true&id=" + pasta.getId();
    }

    public String editar(Pasta pasta) {
        this.pasta = pasta;
        return "cadastro?faces-redirect=true&id=" + pasta.getId();
    }

    public void prepararExclusao(Pasta pasta) {
        this.pastaSelecionada = pasta;
    }

    public void excluir() {
        try {
            pastaService.remover(pastaSelecionada.getId());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pasta excluída com sucesso!"));
            carregarPastas();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
    }

    public void carregarPasta() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null && !idParam.isEmpty()) {
            Long id = Long.valueOf(idParam);
            pastaService.buscarPorId(id).ifPresent(p -> this.pasta = p);
        }
    }

    // Getters e Setters

    public List<Pasta> getPastas() {
        return pastas;
    }

    public void setPastas(List<Pasta> pastas) {
        this.pastas = pastas;
    }

    public Pasta getPasta() {
        return pasta;
    }

    public void setPasta(Pasta pasta) {
        this.pasta = pasta;
    }

    public Pasta getPastaSelecionada() {
        return pastaSelecionada;
    }

    public void setPastaSelecionada(Pasta pastaSelecionada) {
        this.pastaSelecionada = pastaSelecionada;
    }

    public List<Equipe> getEquipesDirigentes() {
        return equipesDirigentes;
    }

    public void setEquipesDirigentes(List<Equipe> equipesDirigentes) {
        this.equipesDirigentes = equipesDirigentes;
    }
}
