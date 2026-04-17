package br.com.segueme.controller;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

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

    private static final String CAMINHO_COMPROVANTES =
            System.getProperty("caminho_comprovantes", "C:\\Desenvolvimento\\comprovantes")
            + java.io.File.separator;

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

    // Upload de comprovante
    private byte[] comprovanteBytesUpload;
    private String comprovanteContentType;
    private String comprovantePreview; // base64 para preview imediato
    private boolean comprovanteIsPdf;

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
        comprovanteBytesUpload = null;
        comprovanteContentType = null;
        comprovantePreview = null;
        comprovanteIsPdf = false;
    }

    public String salvar() {
        return salvarInterno(true);
    }

    public String salvarEContinuar() {
        return salvarInterno(false);
    }

    private String salvarInterno(boolean redirecionarParaLista) {
        try {
            // Processa upload de comprovante antes de salvar
            if (comprovanteBytesUpload != null && comprovanteBytesUpload.length > 0) {
                File dir = new File(CAMINHO_COMPROVANTES);
                if (!dir.exists()) dir.mkdirs();

                // Remove comprovante antigo se existir
                if (movimento.getComprovanteUrl() != null && !movimento.getComprovanteUrl().startsWith("http")) {
                    new File(CAMINHO_COMPROVANTES + movimento.getComprovanteUrl()).delete();
                }

                String ext = comprovanteContentType != null && comprovanteContentType.contains("pdf") ? ".pdf"
                        : comprovanteContentType != null && comprovanteContentType.contains("png") ? ".png"
                        : ".jpg";
                String nomeArquivo = "comprovante_" + UUID.randomUUID().toString().replace("-", "") + ext;
                Files.write(Paths.get(CAMINHO_COMPROVANTES + nomeArquivo), comprovanteBytesUpload);
                movimento.setComprovanteUrl(nomeArquivo);
                comprovanteBytesUpload = null;
            }

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

    /**
     * Recebe o arquivo de comprovante via upload e gera preview base64.
     */
    public void handleComprovanteUpload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        if (file != null && file.getSize() > 0) {
            try {
                this.comprovanteBytesUpload = file.getContent();
                this.comprovanteContentType = file.getContentType();
                this.comprovanteIsPdf = comprovanteContentType != null && comprovanteContentType.contains("pdf");
                if (!comprovanteIsPdf) {
                    String base64 = Base64.getEncoder().encodeToString(this.comprovanteBytesUpload);
                    this.comprovantePreview = "data:" + comprovanteContentType + ";base64," + base64;
                } else {
                    this.comprovantePreview = null; // PDF não tem preview inline, só ícone
                }
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Comprovante carregado",
                                file.getFileName() + " pronto para salvar."));
            } catch (Exception e) {
                this.comprovanteBytesUpload = null;
                this.comprovantePreview = null;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                                "Não foi possível processar o comprovante."));
            }
        }
    }

    /** Verifica se o comprovanteUrl salvo é um arquivo local (não uma URL externa). */
    public boolean isComprovanteLocal(String comprovanteUrl) {
        return comprovanteUrl != null && !comprovanteUrl.startsWith("http");
    }

    /** Verifica se comprovante salvo é PDF. */
    public boolean isComprovantePdf(String comprovanteUrl) {
        return comprovanteUrl != null && comprovanteUrl.toLowerCase().endsWith(".pdf");
    }

    public String getComprovantePath(String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.startsWith("http")) return nomeArquivo;
        return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
                + "/comprovantes/" + nomeArquivo;
    }

    public byte[] getComprovanteBytesUpload() { return comprovanteBytesUpload; }
    public String getComprovantePreview() { return comprovantePreview; }
    public boolean isComprovanteIsPdf() { return comprovanteIsPdf; }
    public boolean isComprovanteUploadPendente() { return comprovanteBytesUpload != null && comprovanteBytesUpload.length > 0; }

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
