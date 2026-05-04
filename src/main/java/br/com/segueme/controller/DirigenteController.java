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
import br.com.segueme.entity.MotivoEncerramentoMandato;
import br.com.segueme.entity.Pasta;
import br.com.segueme.entity.StatusMandato;
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
    private Dirigente dirigenteDetalhes;

    private List<Trabalhador> trabalhadores;
    private List<Pasta> pastas;
    
    private MotivoEncerramentoMandato motivoEncerramento;
    private String observacaoEncerramento;

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
        dirigente.setStatusMandato(StatusMandato.ATIVO);
        dirigente.setDataInicio(LocalDate.now());
        // Mandato padrão de 1 ano (pode ser prorrogado por mais 1)
        dirigente.setDataFim(LocalDate.now().plusYears(1));
        this.motivoEncerramento = null;
        this.observacaoEncerramento = null;
    }

    public String salvar() {
        return salvarInterno(true);
    }
    
    public String salvarEContinuar() {
        return salvarInterno(false);
    }

    private String salvarInterno(boolean redirecionarParaLista) {
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
            return redirecionarParaLista ? "lista?faces-redirect=true" : "";
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

    /**
     * Prorroga o mandato do dirigente selecionado por mais 1 ano.
     */
    public void prorrogarMandato() {
        try {
            if (dirigenteSelecionado == null || dirigenteSelecionado.getId() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione um dirigente."));
                return;
            }
            dirigenteService.prorrogarMandato(dirigenteSelecionado.getId());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
                    "Mandato prorrogado por mais 1 ano com sucesso!"));
            carregarDirigentes();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
    }

    /**
     * Encerra o mandato do dirigente selecionado com o motivo e observação informados.
     */
    public void encerrarMandato() {
        try {
            if (dirigenteSelecionado == null || dirigenteSelecionado.getId() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione um dirigente."));
                return;
            }
            if (motivoEncerramento == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Informe o motivo do encerramento."));
                return;
            }
            dirigenteService.encerrarMandato(dirigenteSelecionado.getId(), motivoEncerramento, observacaoEncerramento);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Mandato encerrado com sucesso!"));
            carregarDirigentes();
            this.motivoEncerramento = null;
            this.observacaoEncerramento = null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
    }

    public MotivoEncerramentoMandato[] getMotivosEncerramento() {
        return MotivoEncerramentoMandato.values();
    }

    public StatusMandato[] getStatusMandatoValues() {
        return StatusMandato.values();
    }

    public void carregarDirigente() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null && !idParam.isEmpty()) {
            Long id = Long.valueOf(idParam);
            dirigenteService.buscarPorId(id).ifPresent(d -> this.dirigente = d);
        }
    }

    public void abrirDetalhes(Dirigente dirigente) {
        dirigenteService.buscarPorId(dirigente.getId()).ifPresent(d -> this.dirigenteDetalhes = d);
    }

    public void fecharDetalhes() {
        this.dirigenteDetalhes = null;
    }

    public Dirigente getDirigenteDetalhes() {
        return dirigenteDetalhes;
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

    public MotivoEncerramentoMandato getMotivoEncerramento() {
        return motivoEncerramento;
    }

    public void setMotivoEncerramento(MotivoEncerramentoMandato motivoEncerramento) {
        this.motivoEncerramento = motivoEncerramento;
    }

    public String getObservacaoEncerramento() {
        return observacaoEncerramento;
    }

    public void setObservacaoEncerramento(String observacaoEncerramento) {
        this.observacaoEncerramento = observacaoEncerramento;
    }
}
