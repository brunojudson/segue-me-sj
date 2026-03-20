package br.com.segueme.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Equipe;
import br.com.segueme.entity.TipoEquipe;
import br.com.segueme.entity.Trabalhador;
import br.com.segueme.service.CasalService;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.EquipeService;
import br.com.segueme.service.TipoEquipeService;

@Named
@ViewScoped
public class EquipeController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EquipeService equipeService;

    @Inject
    private TipoEquipeService tipoEquipeService;
    
    @Inject
    private CasalService casalService;

    @Inject
    private EncontroService encontroService;

    private List<Equipe> equipes;
    private Equipe equipe;
    private Equipe equipeSelecionada;
    private Equipe equipeDetalhes;

    private List<TipoEquipe> tiposEquipe;
    private List<Encontro> encontros;

    @PostConstruct
    public void init() {
        carregarEquipes();
        carregarTiposEquipe();
        carregarEncontros();
        limpar();
    }

    public void carregarEquipes() {
        equipes = equipeService.buscarTodos();
    }

    public void carregarTiposEquipe() {
        tiposEquipe = tipoEquipeService.buscarTodos();
    }

    public void carregarEncontros() {
        encontros = encontroService.buscarAtivos();
    }

    /**
     * Atualiza o campo nome da equipe a partir do TipoEquipe selecionado.
     * Chamado por AJAX para garantir que o model seja preenchido antes
     * da validação/submissão do formulário.
     */
    public void atualizarNomePorTipo() {
        if (this.equipe == null) {
            this.equipe = new Equipe();
        }
        if (this.equipe.getTipoEquipe() != null) {
            this.equipe.setNome(this.equipe.getTipoEquipe().getNome());
        } else {
            this.equipe.setNome(null);
        }
    }

    public void limpar() {
        equipe = new Equipe();
        equipe.setAtivo(true);
    }
    
    public String salvar() {
        return salvarInterno(true);
    }
    
    public String salvarEContinuar() {
        return salvarInterno(false);
    }

    private String salvarInterno(boolean redirecionarParaLista) {
        try {
            // Derivar o nome da equipe a partir do TipoEquipe selecionado
            if (equipe.getTipoEquipe() != null) {
                equipe.setNome(equipe.getTipoEquipe().getNome());
            }

            if (equipe.getId() == null) {
                equipeService.salvar(equipe);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Equipe cadastrada com sucesso!"));
            } else {
                equipeService.atualizar(equipe);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Equipe atualizada com sucesso!"));
            }

            carregarEquipes();
            limpar();
            return redirecionarParaLista ? "lista?faces-redirect=true" : "";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
            return null;
        }
    }

    public String visualizar(Equipe equipe) {
        this.equipeSelecionada = equipe;
        return "visualizar?faces-redirect=true&id=" + equipe.getId();
    }

    public String editar(Equipe equipe) {
        this.equipe = equipe;
        return "cadastro?faces-redirect=true&id=" + equipe.getId();
    }

    public void prepararExclusao(Equipe equipe) {
        this.equipeSelecionada = equipe;
    }

    public void excluir() {
        try {
            equipeService.remover(equipeSelecionada.getId());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Equipe excluída com sucesso!"));
            carregarEquipes();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
    }


    public void carregarEquipe() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null && !idParam.isEmpty()) {
            Long id = Long.valueOf(idParam);
            equipeService.buscarPorId(id).ifPresent(e -> {
                this.equipe = e;
                // Se o nome não estiver preenchido (ou por segurança), derive do TipoEquipe
                if ((this.equipe.getNome() == null || this.equipe.getNome().trim().isEmpty()) && this.equipe.getTipoEquipe() != null) {
                    this.equipe.setNome(this.equipe.getTipoEquipe().getNome());
                }
                identificarCasais(); // Atualiza a informação de casais

                // Converte o Set para uma List, ordena e reconverte para LinkedHashSet para manter a ordem
                List<Trabalhador> trabalhadoresOrdenados = new ArrayList<>(this.equipe.getTrabalhadores());
                trabalhadoresOrdenados.sort(Comparator.comparing(Trabalhador::isEhCasal).reversed());
                this.equipe.setTrabalhadores(new LinkedHashSet<>(trabalhadoresOrdenados));
            });
        }
    }
    
    public void identificarCasais() {
        if (equipe != null && equipe.getTrabalhadores() != null) {
            for (Trabalhador trabalhador : equipe.getTrabalhadores()) {
                // Supondo que exista um método no serviço para verificar se a pessoa faz parte de um casal
                boolean ehCasal = casalService.verificarSePessoaEhCasal(trabalhador.getPessoa().getId());
                trabalhador.setEhCasal(ehCasal);
            }
        }
    }

    public void abrirDetalhes(Equipe equipe) {
        equipeService.buscarPorId(equipe.getId()).ifPresent(e -> {
            this.equipeDetalhes = e;
            // Identificar casais nos trabalhadores
            if (equipeDetalhes.getTrabalhadores() != null) {
                for (Trabalhador trabalhador : equipeDetalhes.getTrabalhadores()) {
                    boolean ehCasal = casalService.verificarSePessoaEhCasal(trabalhador.getPessoa().getId());
                    trabalhador.setEhCasal(ehCasal);
                }
                List<Trabalhador> trabalhadoresOrdenados = new ArrayList<>(equipeDetalhes.getTrabalhadores());
                trabalhadoresOrdenados.sort(Comparator.comparing(Trabalhador::isEhCasal).reversed());
                equipeDetalhes.setTrabalhadores(new LinkedHashSet<>(trabalhadoresOrdenados));
            }
        });
    }

    public void fecharDetalhes() {
        this.equipeDetalhes = null;
    }

    public Equipe getEquipeDetalhes() {
        return equipeDetalhes;
    }

    // Getters e Setters

    public List<Equipe> getEquipes() {
        return equipes;
    }

    public void setEquipes(List<Equipe> equipes) {
        this.equipes = equipes;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    public Equipe getEquipeSelecionada() {
        return equipeSelecionada;
    }

    public void setEquipeSelecionada(Equipe equipeSelecionada) {
        this.equipeSelecionada = equipeSelecionada;
    }

    public List<TipoEquipe> getTiposEquipe() {
        return tiposEquipe;
    }

    public void setTiposEquipe(List<TipoEquipe> tiposEquipe) {
        this.tiposEquipe = tiposEquipe;
    }

    public List<Encontro> getEncontros() {
        return encontros;
    }

    public void setEncontros(List<Encontro> encontros) {
        this.encontros = encontros;
    }
}
