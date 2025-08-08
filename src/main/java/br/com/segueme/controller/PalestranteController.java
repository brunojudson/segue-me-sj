package br.com.segueme.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Casal;
import br.com.segueme.entity.Palestrante;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.enums.TipoPalestrante;
import br.com.segueme.service.CasalService;
import br.com.segueme.service.PalestranteService;
import br.com.segueme.service.PessoaService;

@Named
@ViewScoped
public class PalestranteController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PalestranteService palestranteService;

    @Inject
    private PessoaService pessoaService;

    @Inject
    private CasalService casalService;

    private Palestrante palestrante;
    private List<Palestrante> palestrantes;
    private List<Pessoa> pessoasDisponiveis;
    private List<Casal> casaisDisponiveis;
    
    // Para seleção no formulário
    private Long pessoaSelecionadaId;
    private Long casalSelecionadoId;
    private List<Long> grupoSelecionadoIds;

    @PostConstruct
    public void init() {
        palestrante = new Palestrante();
        grupoSelecionadoIds = new ArrayList<>();
        carregarPalestrantes();
        //carregarPessoasDisponiveis();
        //carregarCasaisDisponiveis();
    }

    public void carregarPalestrantes() {
        palestrantes = palestranteService.buscarTodos();
    }

    public void carregarPessoasDisponiveis() {
        pessoasDisponiveis = pessoaService.buscarTodos(); // Idealmente, filtrar por pessoas ativas, etc.
    }

    public void carregarCasaisDisponiveis() {
        casaisDisponiveis = casalService.buscarTodos(); // Idealmente, filtrar por casais ativos, etc.
    }

    public void prepararNovoCadastro() {
    	carregarPessoasDisponiveis();
        carregarCasaisDisponiveis();
        
        try {
            // Criar um novo objeto palestrante
            this.palestrante = new Palestrante();
            // Definir um valor padrão para evitar NPE
            this.palestrante.setTipoPalestrante(null); 
            
            // Inicializar coleções vazias para evitar NPE
            this.palestrante.setMembrosGrupo(new HashSet<>());
            this.palestrante.setPalestras(new HashSet<>());
            
            // Limpar as seleções anteriores
            this.pessoaSelecionadaId = null;
            this.casalSelecionadoId = null;
            this.grupoSelecionadoIds = new ArrayList<>();
           
        } catch (Exception e) {
           
            e.printStackTrace();
        }
    }

    public void prepararEdicao(Long id) {
        palestrante = palestranteService.buscarPorId(id);
        if (palestrante != null) {
            // Preencher IDs selecionados com base no tipo
            switch (palestrante.getTipoPalestrante()) {
                case INDIVIDUAL:
                    pessoaSelecionadaId = palestrante.getPessoaIndividual() != null ? palestrante.getPessoaIndividual().getId() : null;
                    casalSelecionadoId = null;
                    grupoSelecionadoIds = new ArrayList<>();
                    break;
                case CASAL:
                    casalSelecionadoId = palestrante.getCasal() != null ? palestrante.getCasal().getId() : null;
                    pessoaSelecionadaId = null;
                    grupoSelecionadoIds = new ArrayList<>();
                    break;
				/*
				 * case GRUPO: grupoSelecionadoIds = palestrante.getMembrosGrupo().stream()
				 * .map(Pessoa::getId) .collect(Collectors.toList()); pessoaSelecionadaId =
				 * null; casalSelecionadoId = null; break;
				 */
            }
        } else {
            // Tratar caso não encontre
            prepararNovoCadastro();
        }
    }

    public void salvar() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            // Limpar associações não relevantes com base no tipo selecionado
            if (palestrante.getTipoPalestrante() == null) {
                 context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Tipo de Palestrante é obrigatório."));
                 return;
            }

            switch (palestrante.getTipoPalestrante()) {
                case INDIVIDUAL:
                    if (pessoaSelecionadaId != null) {
                        Pessoa pessoa = pessoaService.buscarPorId(pessoaSelecionadaId).orElse(null);
                        palestrante.setPessoaIndividual(pessoa);
                        palestrante.setCasal(null);
                        palestrante.setMembrosGrupo(new HashSet<>());
                    } else {
                        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Pessoa é obrigatória para palestrante individual."));
                        return;
                    }
                    break;
                case CASAL:
                    if (casalSelecionadoId != null) {
                        Casal casal = casalService.buscarPorId(casalSelecionadoId).orElse(null);
                        if (casal != null) {
                            palestrante.setCasal(casal);
                            palestrante.setPessoaIndividual(null);
                            palestrante.setMembrosGrupo(new HashSet<>());
                        } else {
                            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Casal não encontrado."));
                            return;
                        }
                    } else {
                        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Casal é obrigatório para palestrante do tipo casal."));
                        return;
                    }
                    break;
				/*
				 * case GRUPO: if (grupoSelecionadoIds != null && grupoSelecionadoIds.size() >=
				 * 2) { Set<Pessoa> membros = grupoSelecionadoIds.stream() .map(id ->
				 * pessoaService.buscarPorId(id)) .filter(Optional::isPresent)
				 * .map(Optional::get) .collect(Collectors.toSet());
				 * palestrante.setMembrosGrupo(membros); palestrante.setPessoaIndividual(null);
				 * palestrante.setCasal(null); } else { context.addMessage(null, new
				 * FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro",
				 * "Grupo deve ter pelo menos 2 pessoas selecionadas.")); return; } break;
				 */
            }

            if (palestrante.getId() == null) {
                palestranteService.salvar(palestrante);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Palestrante cadastrado com sucesso!"));
            } else {
                palestranteService.atualizar(palestrante);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Palestrante atualizado com sucesso!"));
            }
            carregarPalestrantes();
            prepararNovoCadastro(); // Limpa o formulário
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao salvar palestrante: " + e.getMessage()));
            // Logar a exceção e.printStackTrace();
        }
    }

    public void excluir(Long id) {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            palestranteService.excluir(id);
            carregarPalestrantes();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Palestrante excluído com sucesso!"));
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao excluir palestrante: " + e.getMessage()));
            // Logar a exceção e.printStackTrace();
        }
    }

    // Getters e Setters
    public Palestrante getPalestrante() {
        return palestrante;
    }

    public void setPalestrante(Palestrante palestrante) {
        this.palestrante = palestrante;
    }

    public List<Palestrante> getPalestrantes() {
        return palestrantes;
    }

    public List<Pessoa> getPessoasDisponiveis() {
        return pessoasDisponiveis;
    }

    public List<Casal> getCasaisDisponiveis() {
        return casaisDisponiveis;
    }

    public TipoPalestrante[] getTiposPalestrante() {
        return TipoPalestrante.values();
    }

    public Long getPessoaSelecionadaId() {
        return pessoaSelecionadaId;
    }

    public void setPessoaSelecionadaId(Long pessoaSelecionadaId) {
        this.pessoaSelecionadaId = pessoaSelecionadaId;
    }

    public Long getCasalSelecionadoId() {
        return casalSelecionadoId;
    }

    public void setCasalSelecionadoId(Long casalSelecionadoId) {
        this.casalSelecionadoId = casalSelecionadoId;
    }

    public List<Long> getGrupoSelecionadoIds() {
        return grupoSelecionadoIds;
    }

    public void setGrupoSelecionadoIds(List<Long> grupoSelecionadoIds) {
        this.grupoSelecionadoIds = grupoSelecionadoIds;
    }
}

