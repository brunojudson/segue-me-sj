package br.com.segueme.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Encontrista;
import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.enums.AptidaoEquipe;
import br.com.segueme.enums.Circulo;
import br.com.segueme.service.EncontristaService;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.PdfService;
import br.com.segueme.service.PessoaService;

@Named
@ViewScoped
public class EncontristaController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EncontristaService encontristaService;

    @Inject
    private PessoaService pessoaService;

    @Inject
    private EncontroService encontroService;

    @Inject
    private PdfService pdfService;

    private List<Encontrista> encontristas;
    private Encontrista encontrista;
    private Encontrista encontristaSelecionado;

    private List<Pessoa> pessoas;
    private List<Encontro> encontros;

    // Modal detalhes
    private Encontrista encontristaDetalhes;

    // Aptidões selecionadas (binding separado por causa do @ElementCollection)
    private List<AptidaoEquipe> aptidoesSelecionadas = new ArrayList<>();

    private static final String CAMINHO_FOTOS = System.getProperty("caminho_fotos", "C:\\Desenvolvimento\\fotos") + java.io.File.separator;
    
    @PostConstruct
    public void init() {
        carregarEncontristas();
        carregarPessoas();
        carregarEncontros();
        limpar();
    }

    public void carregarEncontristas() {
        encontristas = encontristaService.buscarTodos();
    }

    public void carregarPessoas() {
        pessoas = pessoaService.buscarTodos();
    }

    public void carregarEncontros() {
        encontros = encontroService.buscarAtivos();
    }

    public void limpar() {
        encontrista = new Encontrista();
        aptidoesSelecionadas = new ArrayList<>();
    }

    public String salvar() {
        return salvarInterno(true);
    }
    
    public String salvarEContinuar() {
        return salvarInterno(false);
    }

    private String salvarInterno(boolean redirecionarParaLista) {
        try {
            encontrista.setAptidoes(new ArrayList<>(aptidoesSelecionadas != null ? aptidoesSelecionadas : new ArrayList<>()));

            if (encontrista.getId() == null) {
                encontristaService.salvar(encontrista);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Seguimista cadastrado com sucesso!"));
            } else {
                encontristaService.atualizar(encontrista);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Seguimista atualizado com sucesso!"));
            }

            carregarEncontristas();
            limpar();
            return redirecionarParaLista ? "lista?faces-redirect=true" : "";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
            return null;
        }
    }

    public String visualizar(Encontrista encontrista) {
        this.encontristaSelecionado = encontrista;
        return "visualizar?faces-redirect=true&id=" + encontrista.getId();
    }

    public String editar(Encontrista encontrista) {
        this.encontrista = encontrista;
        return "cadastro?faces-redirect=true&id=" + encontrista.getId();
    }

    public void prepararExclusao(Encontrista encontrista) {
        this.encontristaSelecionado = encontrista;
    }

    public void excluir() {
        try {
            encontristaService.remover(encontristaSelecionado.getId());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Seguimista excluído com sucesso!"));
            carregarEncontristas();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
    }

    public void carregarEncontrista() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null && !idParam.isEmpty()) {
            Long id = Long.valueOf(idParam);
            encontristaService.buscarPorId(id).ifPresent(e -> {
                this.encontrista = e;
                this.aptidoesSelecionadas = e.getAptidoes() != null
                        ? new ArrayList<>(e.getAptidoes())
                        : new ArrayList<>();
            });
        }
    }

    public String getFoto() {
        if (encontrista.getPessoa() != null && encontrista.getPessoa().getFoto() != null
                && !encontrista.getPessoa().getFoto().isEmpty()) {
            // Adiciona um parâmetro único (timestamp) para evitar cache
            return "/fotos/" + encontrista.getPessoa().getFoto() + "?t=" + System.currentTimeMillis();
        }
        // Retorna uma imagem padrão caso o casal não tenha foto
        return "/resources/images/default_avatar.png?t=" + System.currentTimeMillis();
    }

    // ...existing code...
    public void gerarRelatorioEncontristasAtivos() {
        try {
            List<Encontrista> ativos = encontristaService.buscarAtivos();
            pdfService.gerarRelatorioEncontristasAtivos(ativos);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao gerar relatório", e.getMessage()));
        }
    }

    public Circulo[] getCirculos() {
        return Circulo.values();
    }

    public AptidaoEquipe[] getAptidoesDisponiveis() {
        return AptidaoEquipe.values();
    }

    public List<AptidaoEquipe> getAptidoesSelecionadas() {
        if (aptidoesSelecionadas == null) aptidoesSelecionadas = new ArrayList<>();
        return aptidoesSelecionadas;
    }

    public void setAptidoesSelecionadas(List<AptidaoEquipe> aptidoesSelecionadas) {
        this.aptidoesSelecionadas = aptidoesSelecionadas;
    }

    public void abrirDetalhes(Encontrista encontrista) {
        this.encontristaDetalhes = encontristaService.buscarPorId(encontrista.getId()).orElse(encontrista);
    }

    public void fecharDetalhes() {
        this.encontristaDetalhes = null;
    }

    public Encontrista getEncontristaDetalhes() {
        return encontristaDetalhes;
    }

    // Getters e Setters

    public List<Encontrista> getEncontristas() {
        return encontristas;
    }

    public void setEncontristas(List<Encontrista> encontristas) {
        this.encontristas = encontristas;
    }

    public Encontrista getEncontrista() {
        return encontrista;
    }

    public void setEncontrista(Encontrista encontrista) {
        this.encontrista = encontrista;
    }

    public Encontrista getEncontristaSelecionado() {
        return encontristaSelecionado;
    }

    public void setEncontristaSelecionado(Encontrista encontristaSelecionado) {
        this.encontristaSelecionado = encontristaSelecionado;
    }

    public List<Pessoa> getPessoas() {
        return pessoas;
    }

    public void setPessoas(List<Pessoa> pessoas) {
        this.pessoas = pessoas;
    }

    public List<Encontro> getEncontros() {
        return encontros;
    }

    public void setEncontros(List<Encontro> encontros) {
        this.encontros = encontros;
    }
}
