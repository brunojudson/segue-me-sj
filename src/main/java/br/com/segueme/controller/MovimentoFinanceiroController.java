package br.com.segueme.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.MovimentoFinanceiro;
import br.com.segueme.enums.CategoriaFinanceira;
import br.com.segueme.enums.TipoMovimento;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.MovimentoFinanceiroService;

/**
 * Controller para gestão financeira dos encontros.
 */
@Named
@ViewScoped
public class MovimentoFinanceiroController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MovimentoFinanceiroService movimentoService;

    @Inject
    private EncontroService encontroService;

    private List<MovimentoFinanceiro> movimentos;
    private MovimentoFinanceiro movimento;
    private MovimentoFinanceiro movimentoSelecionado;
    private MovimentoFinanceiro movimentoDetalhes;
    private List<Encontro> encontros;

    // Filtro por encontro
    private Long encontroFiltroId;

    // Resumo financeiro
    private BigDecimal totalReceitas;
    private BigDecimal totalDespesas;
    private BigDecimal saldo;

    @PostConstruct
    public void init() {
        carregarMovimentos();
        carregarEncontros();
        limpar();
    }

    public void carregarMovimentos() {
        if (encontroFiltroId != null) {
            movimentos = movimentoService.buscarPorEncontro(encontroFiltroId);
            calcularResumo(encontroFiltroId);
        } else {
            movimentos = movimentoService.buscarTodos();
            totalReceitas = BigDecimal.ZERO;
            totalDespesas = BigDecimal.ZERO;
            saldo = BigDecimal.ZERO;
        }
    }

    public void carregarEncontros() {
        encontros = encontroService.buscarTodos();
    }

    public void limpar() {
        movimento = new MovimentoFinanceiro();
    }

    public String salvar() {
        return salvarInterno(true);
    }

    public String salvarEContinuar() {
        return salvarInterno(false);
    }

    private String salvarInterno(boolean redirecionarParaLista) {
        try {
            if (movimento.getId() == null) {
                movimentoService.salvar(movimento);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
                                "Movimento financeiro cadastrado com sucesso!"));
            } else {
                movimentoService.atualizar(movimento);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
                                "Movimento financeiro atualizado com sucesso!"));
            }

            carregarMovimentos();
            limpar();
            return redirecionarParaLista ? "lista?faces-redirect=true" : "";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
            return null;
        }
    }

    public String visualizar(MovimentoFinanceiro movimento) {
        this.movimentoSelecionado = movimento;
        return "visualizar?faces-redirect=true&id=" + movimento.getId();
    }

    public String editar(MovimentoFinanceiro movimento) {
        this.movimento = movimento;
        return "cadastro?faces-redirect=true&id=" + movimento.getId();
    }

    public void prepararExclusao(MovimentoFinanceiro movimento) {
        this.movimentoSelecionado = movimento;
    }

    public void excluir() {
        try {
            movimentoService.remover(movimentoSelecionado.getId());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
                            "Movimento financeiro excluído com sucesso!"));
            carregarMovimentos();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
    }

    public void carregarMovimento() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null && !idParam.isEmpty()) {
            Long id = Long.valueOf(idParam);
            movimentoService.buscarPorId(id).ifPresent(m -> this.movimento = m);
        }
    }

    public void filtrarPorEncontro() {
        carregarMovimentos();
    }

    private void calcularResumo(Long encontroId) {
        totalReceitas = movimentoService.calcularTotalReceitas(encontroId);
        totalDespesas = movimentoService.calcularTotalDespesas(encontroId);
        saldo = movimentoService.calcularSaldo(encontroId);
    }

    public void abrirDetalhes(MovimentoFinanceiro movimento) {
        movimentoService.buscarPorId(movimento.getId()).ifPresent(m -> this.movimentoDetalhes = m);
    }

    public void fecharDetalhes() {
        this.movimentoDetalhes = null;
    }

    public MovimentoFinanceiro getMovimentoDetalhes() {
        return movimentoDetalhes;
    }

    public TipoMovimento[] getTiposMovimento() {
        return TipoMovimento.values();
    }

    public CategoriaFinanceira[] getCategorias() {
        return CategoriaFinanceira.values();
    }

    // Getters e Setters

    public List<MovimentoFinanceiro> getMovimentos() {
        return movimentos;
    }

    public void setMovimentos(List<MovimentoFinanceiro> movimentos) {
        this.movimentos = movimentos;
    }

    public MovimentoFinanceiro getMovimento() {
        return movimento;
    }

    public void setMovimento(MovimentoFinanceiro movimento) {
        this.movimento = movimento;
    }

    public MovimentoFinanceiro getMovimentoSelecionado() {
        return movimentoSelecionado;
    }

    public void setMovimentoSelecionado(MovimentoFinanceiro movimentoSelecionado) {
        this.movimentoSelecionado = movimentoSelecionado;
    }

    public List<Encontro> getEncontros() {
        return encontros;
    }

    public void setEncontros(List<Encontro> encontros) {
        this.encontros = encontros;
    }

    public Long getEncontroFiltroId() {
        return encontroFiltroId;
    }

    public void setEncontroFiltroId(Long encontroFiltroId) {
        this.encontroFiltroId = encontroFiltroId;
    }

    public BigDecimal getTotalReceitas() {
        return totalReceitas;
    }

    public BigDecimal getTotalDespesas() {
        return totalDespesas;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
