package br.com.segueme.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Palestra;
import br.com.segueme.entity.Palestrante;
import br.com.segueme.enums.TemaPalestra;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.PalestraService;
import br.com.segueme.service.PalestranteService;

@Named
@ViewScoped
public class PalestraController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PalestraService palestraService;

    @Inject
    private EncontroService encontroService;
    
    @Inject
    private PalestranteService palestranteService; // Para selecionar palestrantes

    private Palestra palestra;
    private List<Palestra> palestras;
    private List<Encontro> encontrosDisponiveis;
    private List<Palestrante> palestrantesDisponiveis;
    private List<Long> palestrantesSelecionadosIds; // IDs dos palestrantes selecionados

    private List<Encontro> encontrosDisponiveisParaFiltro; // Para o filtro da tabela
    
    @PostConstruct
    public void init() {
        palestra = new Palestra();
        palestra.setEncontro(new Encontro()); // Inicializa para evitar NullPointerException no formulário
        carregarPalestras();
        carregarEncontrosDisponiveisParaFiltro();
        //carregarEncontrosDisponiveis();
        //carregarPalestrantesDisponiveis();
        palestrantesSelecionadosIds = new ArrayList<>();
    }

    public void carregarPalestras() {
        palestras = palestraService.buscarTodos();
    }

    public void carregarEncontrosDisponiveis() {
        encontrosDisponiveis = encontroService.buscarTodos(); // Idealmente, buscar apenas encontros ativos
    }
    
    public void carregarEncontrosDisponiveisParaFiltro() { // Usado no filtro da tabela
        encontrosDisponiveisParaFiltro = encontroService.buscarTodos(); 
    }
    
   
    /**
     * Converte um objeto TemaPalestra para string para fins de filtragem
     */
    public Converter getTemaConverter() {
        return new Converter() {
            @Override
            public Object getAsObject(FacesContext context, UIComponent component, String value) {
                if (value == null || value.isEmpty()) {
                    return null;
                }
                return TemaPalestra.valueOf(value);
            }

            @Override
            public String getAsString(FacesContext context, UIComponent component, Object value) {
                if (value == null) {
                    return "";
                }
                return ((TemaPalestra) value).name();
            }
        };
    }
    

    public void carregarPalestrantesDisponiveis() {
        this.palestrantesDisponiveis = palestranteService.buscarTodos();
    }

    public void prepararNovoCadastro() {
    	carregarEncontrosDisponiveis();
    	carregarPalestrantesDisponiveis();
        palestra = new Palestra();
        palestra.setEncontro(new Encontro());
        palestrantesSelecionadosIds = new ArrayList<>();
    }

    public void prepararEdicao(Long id) {
    	carregarEncontrosDisponiveis();
        carregarPalestrantesDisponiveis();

        Palestra palestraParaEdicao = palestraService.buscarPorId(id); // Seu repository já faz EAGER FETCH
        
        if (palestraParaEdicao != null) {
            this.palestra = palestraParaEdicao;

            // Preenche a lista de IDs dos palestrantes já associados
            if (this.palestra.getPalestrantes() != null && !this.palestra.getPalestrantes().isEmpty()) {
                this.palestrantesSelecionadosIds = this.palestra.getPalestrantes().stream()
                                                        .map(Palestrante::getId)
                                                        .collect(Collectors.toList());
            } else {
                this.palestrantesSelecionadosIds = new ArrayList<>();
            }

            // Garante que o encontro não seja nulo para evitar NullPointerException no formulário
            // ao tentar acessar palestra.encontro.id.
            // O valor para o p:selectOneMenu de Encontro será palestra.encontro.id
            if (this.palestra.getEncontro() == null) {
                this.palestra.setEncontro(new Encontro()); // Inicializa para evitar NPE no binding do ID
            }

        } else {
            // Tratar caso não encontre a palestra
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Palestra com ID " + id + " não encontrada."));
            // Limpar o formulário ou definir um estado padrão
            this.palestra = new Palestra();
            this.palestra.setEncontro(new Encontro());
            this.palestrantesSelecionadosIds = new ArrayList<>();
        }
    }
    
    public String salvar() { // Alterado para retornar String
        FacesContext context = FacesContext.getCurrentInstance();
        boolean success = false;
        try {
            // ... (lógica de busca de encontro e palestrantes como antes) ...
            if (palestra.getEncontro() != null && palestra.getEncontro().getId() != null) {
                Encontro encontroObj = encontroService.buscarPorId(palestra.getEncontro().getId()).orElse(null);
                if (encontroObj == null) {
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Encontro não encontrado."));
                    return null; // Permanece na mesma página (diálogo aberto)
                }
                palestra.setEncontro(encontroObj);
            } else {
                 context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Encontro é obrigatório."));
                 return null; // Permanece na mesma página
            }
            
            if (palestra.getPalestrantes() == null) {
                palestra.setPalestrantes(new HashSet<>());
            } else {
                palestra.getPalestrantes().clear();
            }
            
            if (palestrantesSelecionadosIds != null && !palestrantesSelecionadosIds.isEmpty()) {
                for (Long palestranteId : palestrantesSelecionadosIds) {
                    Palestrante palestranteEntity = palestranteService.buscarPorId(palestranteId);
                    if (palestranteEntity != null) {
                        palestra.getPalestrantes().add(palestranteEntity);
                        if (palestranteEntity.getPalestras() == null) {
                            palestranteEntity.setPalestras(new HashSet<>());
                        }
                        palestranteEntity.getPalestras().add(palestra);
                    }
                }
            }

            if (palestra.getId() == null) {
                palestraService.salvar(palestra);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Palestra cadastrada com sucesso!"));
            } else {
                palestraService.atualizar(palestra);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Palestra atualizada com sucesso!"));
            }
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao salvar palestra: " + e.getMessage()));
        }

        if (success) {
            // Não precisa mais chamar carregarPalestras() ou prepararNovoCadastro() aqui
            // se o redirect for para a mesma view, o @PostConstruct fará o necessário.
            // Ou, se quiser manter o diálogo fechado e a lista atualizada,
            // você pode precisar de uma estratégia diferente se não quiser um full redirect.
            // Para PRG completo:
            return "/pages/palestra/lista.xhtml?faces-redirect=true";
        } else {
            return null; // Permanece na página atual (diálogo provavelmente ainda aberto)
        }
    }

    public void excluir(Long id) {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            palestraService.excluir(id);
            carregarPalestras();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Palestra excluída com sucesso!"));
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao excluir palestra: " + e.getMessage()));
            // Logar a exceção e.printStackTrace();
        }
    }

    // Getters e Setters
    public Palestra getPalestra() {
        return palestra;
    }

    public void setPalestra(Palestra palestra) {
        this.palestra = palestra;
    }

    public List<Palestra> getPalestras() {
        return palestras;
    }

    public List<Encontro> getEncontrosDisponiveis() {
        return encontrosDisponiveis;
    }

    public TemaPalestra[] getTemasPalestra() {
        return TemaPalestra.values();
    }
    public List<Palestrante> getPalestrantesDisponiveis() {
        return palestrantesDisponiveis;
    }

    public List<Encontro> getEncontrosDisponiveisParaFiltro() {
		return encontrosDisponiveisParaFiltro;
	}



	public List<Long> getPalestrantesSelecionadosIds() {
        return palestrantesSelecionadosIds;
    }

    public void setPalestrantesSelecionadosIds(List<Long> palestrantesSelecionadosIds) {
        this.palestrantesSelecionadosIds = palestrantesSelecionadosIds;
    }
}

