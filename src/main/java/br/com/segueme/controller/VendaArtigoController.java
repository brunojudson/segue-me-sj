package br.com.segueme.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.VendaArtigo;
import br.com.segueme.entity.CategoriaArtigo;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.stream.Collectors;
import br.com.segueme.service.VendaArtigoService;

/**
 * Managed Bean para gerenciar artigos de venda
 */
@Named
@ViewScoped
public class VendaArtigoController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private VendaArtigoService artigoService;
    
    private List<VendaArtigo> artigos;
    private VendaArtigo artigo;
    private VendaArtigo artigoSelecionado;
    private List<String> categorias;
    
    private String filtroNome;
    private String filtroCategoria;
    private Boolean filtroAtivo;
    
    @PostConstruct
    public void init() {
        carregarArtigos();
        carregarCategorias();
        limpar();
    }
    
    public void carregarArtigos() {
        try {
            artigos = artigoService.buscarTodos();
        } catch (Exception e) {
            addErrorMessage("Erro ao carregar artigos: " + e.getMessage());
        }
    }
    
    public void carregarCategorias() {
        try {
            // Carrega categorias a partir do enum CategoriaArtigo
            categorias = Arrays.stream(CategoriaArtigo.values())
                    .map(CategoriaArtigo::getDescricao)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            addErrorMessage("Erro ao carregar categorias: " + e.getMessage());
        }
    }
    
    public void limpar() {
        artigo = new VendaArtigo();
        artigo.setAtivo(true);
        artigo.setEstoqueAtual(0);
        artigo.setEstoqueMinimo(0);
    }
    
    public String salvar() {
        try {
            if (artigo.getId() == null) {
                artigoService.salvar(artigo);
                addInfoMessage("Artigo cadastrado com sucesso!");
            } else {
                artigoService.atualizar(artigo);
                addInfoMessage("Artigo atualizado com sucesso!");
            }
            carregarArtigos();
            limpar();
            return "lista?faces-redirect=true";
        } catch (Exception e) {
            addErrorMessage("Erro ao salvar artigo: " + e.getMessage());
            return null;
        }
    }
    
    public String editar(VendaArtigo artigo) {
        this.artigo = artigo;
        return "cadastro?faces-redirect=true&id=" + artigo.getId();
    }
    
    public void prepararExclusao(VendaArtigo artigo) {
        this.artigoSelecionado = artigo;
    }
    
    public void excluir() {
        try {
            artigoService.remover(artigoSelecionado.getId());
            addInfoMessage("Artigo excluído com sucesso!");
            carregarArtigos();
        } catch (Exception e) {
            addErrorMessage("Erro ao excluir artigo: " + e.getMessage());
        }
    }
    
    public void ativar(VendaArtigo artigo) {
        try {
            artigoService.ativar(artigo.getId());
            addInfoMessage("Artigo ativado com sucesso!");
            carregarArtigos();
        } catch (Exception e) {
            addErrorMessage("Erro ao ativar artigo: " + e.getMessage());
        }
    }
    
    public void inativar(VendaArtigo artigo) {
        try {
            artigoService.inativar(artigo.getId());
            addInfoMessage("Artigo inativado com sucesso!");
            carregarArtigos();
        } catch (Exception e) {
            addErrorMessage("Erro ao inativar artigo: " + e.getMessage());
        }
    }

    /**
     * Alterna o status ativo/inativo do artigo (usado pela UI para evitar expressões ternárias em EL)
     */
    public void toggleAtivo(VendaArtigo artigo) {
        if (artigo == null) return;
        if (artigo.getAtivo() != null && artigo.getAtivo()) {
            inativar(artigo);
        } else {
            ativar(artigo);
        }
    }

    public void prepararNovo() {
        limpar();
    }

    /**
     * Atualiza o código do artigo a partir da categoria selecionada.
     * Será invocado via AJAX quando o usuário escolher a categoria.
     */
    public void atualizarCodigo() {
        if (artigo == null || artigo.getCategoria() == null) return;
        String prefix = gerarPrefixo(artigo.getCategoria());
        String novoCodigo = artigoService.gerarCodigoComPrefixo(prefix);
        artigo.setCodigo(novoCodigo);
    }

    private String gerarPrefixo(String categoria) {
        if (categoria == null) return "ART";
        // Remove acentos e caracteres não alfabéticos
        String normalized = Normalizer.normalize(categoria, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replaceAll("[^A-Za-z]", "");
        if (normalized.length() >= 3) {
            return normalized.substring(0, 3).toUpperCase();
        } else {
            return (normalized + "ART").substring(0, 3).toUpperCase();
        }
    }

    public void prepararEdicao(VendaArtigo artigo) {
        this.artigo = artigo;
    }

    public void excluir(VendaArtigo artigo) {
        this.artigoSelecionado = artigo;
        excluir();
    }
    
    public void filtrarArtigos() {
        try {
            if (filtroNome != null && !filtroNome.trim().isEmpty()) {
                artigos = artigoService.buscarPorNome(filtroNome);
            } else if (filtroCategoria != null && !filtroCategoria.trim().isEmpty()) {
                artigos = artigoService.buscarPorCategoria(filtroCategoria);
            } else if (filtroAtivo != null && filtroAtivo) {
                artigos = artigoService.buscarAtivos();
            } else {
                carregarArtigos();
            }
        } catch (Exception e) {
            addErrorMessage("Erro ao filtrar artigos: " + e.getMessage());
        }
    }
    
    public void limparFiltros() {
        filtroNome = null;
        filtroCategoria = null;
        filtroAtivo = null;
        carregarArtigos();
    }
    
    public void carregarArtigo() {
        String idParam = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("id");
        if (idParam != null && !idParam.isEmpty()) {
            Long id = Long.valueOf(idParam);
            artigoService.buscarPorId(id).ifPresent(a -> this.artigo = a);
        }
    }
    
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", message));
    }
    
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", message));
    }
    
    // Getters e Setters
    public List<VendaArtigo> getArtigos() {
        return artigos;
    }
    
    public void setArtigos(List<VendaArtigo> artigos) {
        this.artigos = artigos;
    }
    
    public VendaArtigo getArtigo() {
        return artigo;
    }
    
    public void setArtigo(VendaArtigo artigo) {
        this.artigo = artigo;
    }
    
    public VendaArtigo getArtigoSelecionado() {
        return artigoSelecionado;
    }
    
    public void setArtigoSelecionado(VendaArtigo artigoSelecionado) {
        this.artigoSelecionado = artigoSelecionado;
    }
    
    public List<String> getCategorias() {
        return categorias;
    }
    
    public void setCategorias(List<String> categorias) {
        this.categorias = categorias;
    }
    
    public String getFiltroNome() {
        return filtroNome;
    }
    
    public void setFiltroNome(String filtroNome) {
        this.filtroNome = filtroNome;
    }
    
    public String getFiltroCategoria() {
        return filtroCategoria;
    }
    
    public void setFiltroCategoria(String filtroCategoria) {
        this.filtroCategoria = filtroCategoria;
    }
    
    public Boolean getFiltroAtivo() {
        return filtroAtivo;
    }
    
    public void setFiltroAtivo(Boolean filtroAtivo) {
        this.filtroAtivo = filtroAtivo;
    }
}
