package br.com.segueme.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.Encontrista;
import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Equipe;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.enums.Circulo;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.EquipeService;
import br.com.segueme.service.PessoaService;
import br.com.segueme.service.RelatorioService;

@Named
@ViewScoped
public class RelatorioController implements Serializable {

    private static final long serialVersionUID = 1L;

    // Formata valores monetários para exportação (R$ 2.500,00)
    public String formatarMoedaExport(Object value) {
        if (value == null) return "";
        try {
            java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));
            if (value instanceof Number) {
                return nf.format(value);
            }
            // Tenta converter se vier como String
            return nf.format(Double.parseDouble(value.toString().replace(",", ".")));
        } catch (Exception e) {
            return value.toString();
        }
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private EncontroService encontroService;

    @Inject
    private PessoaService pessoaService;

    @Inject
    private EquipeService equipeService;

    @Inject
    private RelatorioService relatorioService;

    private List<Encontro> encontros;
    private List<Pessoa> pessoas;
    private List<Equipe> equipes;
    private List<String> meses;

    private Encontro encontroSelecionado;
    private Encontro encontroAnteriorSelecionado;
    private Pessoa pessoaSelecionada;
    private Equipe equipeSelecionada;
    private Month mesSelecionado;

    // Dados para o relatório em cards por equipe (encontro anterior)
    private List<Equipe> equipesEncontroAnterior;
    private Map<Long, List<Map<String, Object>>> trabalhadoresPorEquipe;

    private List<Map<String, Object>> resultadoRelatorio;
    private String tipoRelatorioSelecionado;
    
    private Encontro encontroSelecionadoCirculos;
    private List<Encontrista> integrantesSelecionados;
    private Circulo circuloSelecionado;

    @PostConstruct
    public void init() {
        carregarEncontros();
        carregarPessoas();
        carregarEquipes();
        mesSelecionado = LocalDate.now().getMonth();
        carregarMeses(); // Inicializa a lista de meses
    }


    
    // Retorna a soma do valor arrecadado no relatório financeiro de contribuições
    public Double getSomaValorArrecadado() {
        if (resultadoRelatorio == null || resultadoRelatorio.isEmpty()) return 0.0;
        double soma = 0.0;
        for (Map<String, Object> item : resultadoRelatorio) {
            Object valor = item.get("valor_total");
            if (valor instanceof Number) {
                soma += ((Number) valor).doubleValue();
            } else if (valor != null) {
                try {
                    soma += Double.parseDouble(valor.toString());
                } catch (NumberFormatException e) {
                    // ignora valores inválidos
                }
            }
        }
        return soma;
    }
    public void carregarMeses() {
        meses = Arrays.stream(Month.values())
                .map(Month::name)
                .map(nome -> nome.substring(0, 1).toUpperCase() + nome.substring(1).toLowerCase())
                .map(nome -> nome.replace("January", "Janeiro")
                        .replace("February", "Fevereiro")
                        .replace("March", "Março")
                        .replace("April", "Abril")
                        .replace("May", "Maio")
                        .replace("June", "Junho")
                        .replace("July", "Julho")
                        .replace("August", "Agosto")
                        .replace("September", "Setembro")
                        .replace("October", "Outubro")
                        .replace("November", "Novembro")
                        .replace("December", "Dezembro"))
                .collect(Collectors.toList());
    }

    public void carregarEncontros() {
        encontros = encontroService.buscarTodos();
    }

    public void carregarPessoas() {
        pessoas = pessoaService.buscarTodos();
    }

    public void carregarEquipes() {
        equipes = equipeService.buscarTodos();
    }

    public void gerarRelatorioEncontristasPorEncontro() {
        if (encontroSelecionado == null) {
            return;
        }
        resultadoRelatorio = relatorioService.buscarEncontristasPorEncontro(encontroSelecionado.getId());
        tipoRelatorioSelecionado = "Encontristas por Encontro";
    }

    public void gerarRelatorioTrabalhadoresPorEquipe() {
        if (equipeSelecionada == null) {
            return;
        }
        resultadoRelatorio = relatorioService.buscarTrabalhadoresPorEquipe(equipeSelecionada.getId());
        tipoRelatorioSelecionado = "Trabalhadores por Equipe";
    }

    public void gerarRelatorioContribuicoesPorEncontro() {
        resultadoRelatorio = relatorioService.buscarContribuicoesPorEncontro();
        tipoRelatorioSelecionado = "Contribuições por Encontro";
    }

    public void gerarRelatorioCasaisTrabalhadores() {
        if (encontroSelecionado == null) {
            return;
        }
        resultadoRelatorio = relatorioService.buscarCasaisTrabalhadores(encontroSelecionado.getId());
        tipoRelatorioSelecionado = "Casais Trabalhadores";
    }

    public void gerarRelatorioEncontristasQueSeTornaramTrabalhadores() {
        if (encontroAnteriorSelecionado == null || encontroSelecionado == null) {
            return;
        }
        resultadoRelatorio = relatorioService.buscarEncontristasQueSeTornaramTrabalhadores(
                encontroAnteriorSelecionado.getId(), encontroSelecionado.getId());
        tipoRelatorioSelecionado = "Encontristas que se tornaram Trabalhadores";
    }

    public void gerarRelatorioDirigentesPorPasta() {
        resultadoRelatorio = relatorioService.buscarDirigentesPorPasta();
        tipoRelatorioSelecionado = "Dirigentes por Pasta";
    }

    public void gerarRelatorioDistribuicaoTrabalhadoresPorEquipe() {
        if (encontroSelecionado == null) {
            return;
        }
        resultadoRelatorio = relatorioService.buscarDistribuicaoTrabalhadoresPorEquipe(encontroSelecionado.getId());
        tipoRelatorioSelecionado = "Distribuição de Trabalhadores por Equipe";
    }

    /**
     * Gera relatório em formato de cards organizado por equipe para o encontro anterior selecionado.
     */
    public void gerarRelatorioEncontroAnteriorCards() {
        if (encontroAnteriorSelecionado == null) {
            return;
        }
        equipesEncontroAnterior = equipeService.buscarPorEncontro(encontroAnteriorSelecionado.getId());
        trabalhadoresPorEquipe = new HashMap<>();
        for (Equipe eq : equipesEncontroAnterior) {
            List<Map<String, Object>> lista = relatorioService.buscarTrabalhadoresPorEquipe(eq.getId());
            trabalhadoresPorEquipe.put(eq.getId(), lista);
        }
        tipoRelatorioSelecionado = "Trabalhadores por Equipe (Encontro Anterior)";
    }

    public void limparEncontroAnteriorCards() {
        this.encontroAnteriorSelecionado = null;
        if (this.equipesEncontroAnterior != null) this.equipesEncontroAnterior.clear();
        if (this.trabalhadoresPorEquipe != null) this.trabalhadoresPorEquipe.clear();
        this.equipesEncontroAnterior = null;
        this.trabalhadoresPorEquipe = null;
        this.tipoRelatorioSelecionado = null;
    }

    /**
     * Wrapper que retorna String para compatibilidade com alguns usos de EL em action.
     */
    public String limparEncontroAnteriorCardsSubmit() {
        limparEncontroAnteriorCards();
        return null;
    }

    public void gerarRelatorioFinanceiroContribuicoes() {
        if (encontroSelecionado == null) {
            return;
        }
        resultadoRelatorio = relatorioService.buscarFinanceiroContribuicoes(encontroSelecionado.getId());
        tipoRelatorioSelecionado = "Financeiro de Contribuições";
    }

    public void gerarRelatorioAniversariantesMes() {
        if (mesSelecionado == null) {
            return;
        }
        Long encontroId = encontroSelecionado != null ? encontroSelecionado.getId() : null;
        resultadoRelatorio = relatorioService.buscarAniversariantesMes(encontroId, mesSelecionado);
        tipoRelatorioSelecionado = "Aniversariantes do Mês";
    }

    public void gerarRelatorioHistoricoParticipacao() {
        if (pessoaSelecionada == null) {
            return;
        }
        resultadoRelatorio = relatorioService.buscarHistoricoParticipacao(pessoaSelecionada.getId());
        tipoRelatorioSelecionado = "Histórico de Participação";
    }

    public void gerarRelatorioCoordenadoresPorEncontro() {
        if (encontroSelecionado == null) {
            return;
        }
        resultadoRelatorio = relatorioService.buscarPalestrasPorEncontro(encontroSelecionado.getId());
        tipoRelatorioSelecionado = "Coordenadores por Encontro";
    }
    
    public void gerarRelatorioPalestrasPorEncontro() {
        if (encontroSelecionado == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione um encontro."));
            return;
        }
        resultadoRelatorio = relatorioService.buscarPalestrasPorEncontro(encontroSelecionado.getId());
        tipoRelatorioSelecionado = "Palestras e Palestrantes por Encontro";
    }

    public void gerarRelatorioAptidoesPorEncontro() {
        if (encontroSelecionado == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione um encontro."));
            return;
        }
        resultadoRelatorio = relatorioService.buscarAptidoesPorEncontro(encontroSelecionado.getId());
        tipoRelatorioSelecionado = "Aptidões dos Seguimistas por Encontro";
    }

    public void gerarRelatorioAptidoesSeguimistasPorEncontro() {
        if (encontroSelecionado == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione um encontro."));
            return;
        }
        resultadoRelatorio = relatorioService.buscarAptidoesSeguimistasPorEncontro(encontroSelecionado.getId());
        tipoRelatorioSelecionado = "Aptidões dos Seguimistas/Encontristas por Encontro";
    }

    public long getContarSeguimistasComAptidoes() {
        if (resultadoRelatorio == null) return 0;
        return resultadoRelatorio.stream()
                .filter(m -> m.get("aptidoes") != null && !"Nenhuma".equals(m.get("aptidoes")))
                .count();
    }

    public long getContarAptosPalestrar() {
        if (resultadoRelatorio == null) return 0;
        return resultadoRelatorio.stream()
                .filter(m -> "Sim".equals(m.get("apto_palestrar")))
                .count();
    }

    public long getContarAptosCoordena() {
        if (resultadoRelatorio == null) return 0;
        return resultadoRelatorio.stream()
                .filter(m -> "Sim".equals(m.get("apto_coordenar")))
                .count();
    }
    //INICIO DA INFORMAÇÃO DE CIRCULOS
    // Retorna todos os círculos
    public Circulo[] getCirculos() {
        return Circulo.values();
    }

    // Busca os integrantes do círculo selecionado no encontro selecionado
    public void selecionarCirculo(Circulo circulo) {
        this.circuloSelecionado = circulo;
        if (encontroSelecionadoCirculos != null && circulo != null) {
            integrantesSelecionados = entityManager.createQuery(
                    "SELECT e FROM Encontrista e JOIN FETCH e.pessoa JOIN FETCH e.encontro WHERE e.encontro = :encontro AND e.circulo = :circulo ORDER BY e.pessoa.nome", 
                    Encontrista.class)
                    .setParameter("encontro", encontroSelecionadoCirculos)
                    .setParameter("circulo", circulo)
                    .getResultList();
        } else {
            integrantesSelecionados = new ArrayList<>();
        }
    }

    // Busca todos os encontristas do encontro selecionado
    public long contarIntegrantesPorCirculo(Circulo circulo) {
        if (encontroSelecionadoCirculos == null) return 0;
        Long count = entityManager.createQuery(
            "SELECT COUNT(e) FROM Encontrista e WHERE e.encontro = :encontro AND e.circulo = :circulo", 
            Long.class)
            .setParameter("encontro", encontroSelecionadoCirculos)
            .setParameter("circulo", circulo)
            .getSingleResult();
        return count != null ? count : 0;
    }

    // Conta o total de encontristas do encontro selecionado
    public long getTotalEncontristas() {
        if (encontroSelecionadoCirculos == null) return 0;
        Long count = entityManager.createQuery(
            "SELECT COUNT(e) FROM Encontrista e WHERE e.encontro = :encontro", 
            Long.class)
            .setParameter("encontro", encontroSelecionadoCirculos)
            .getSingleResult();
        return count != null ? count : 0;
    }

    // Conta quantos círculos têm pelo menos um integrante no encontro selecionado
    public long getCirculosAtivos() {
        if (encontroSelecionadoCirculos == null) return 0;
        Long count = entityManager.createQuery(
            "SELECT COUNT(DISTINCT e.circulo) FROM Encontrista e WHERE e.encontro = :encontro", 
            Long.class)
            .setParameter("encontro", encontroSelecionadoCirculos)
            .getSingleResult();
        return count != null ? count : 0;
    }

    // Getters e setters
    public Encontro getEncontroSelecionadoCirculos() {
        return encontroSelecionadoCirculos;
    }

    public void setEncontroSelecionadoCirculos(Encontro encontroSelecionadoCirculos) {
        this.encontroSelecionadoCirculos = encontroSelecionadoCirculos;
    }

    public List<Encontrista> getIntegrantesSelecionados() {
        return integrantesSelecionados;
    }

    public Circulo getCirculoSelecionado() {
        return circuloSelecionado;
    }
    //FIM DA INFORMAÇÃO DE CIRCULOS
    public List<Encontro> getEncontros() {
        return encontros;
    }

    public void setEncontros(List<Encontro> encontros) {
        this.encontros = encontros;
    }

    public List<Equipe> getEquipesEncontroAnterior() {
        return equipesEncontroAnterior;
    }

    public Map<Long, List<Map<String, Object>>> getTrabalhadoresPorEquipe() {
        return trabalhadoresPorEquipe;
    }

    public List<Pessoa> getPessoas() {
        return pessoas;
    }

    public void setPessoas(List<Pessoa> pessoas) {
        this.pessoas = pessoas;
    }

    public List<Equipe> getEquipes() {
        return equipes;
    }

    public void setEquipes(List<Equipe> equipes) {
        this.equipes = equipes;
    }

    public Encontro getEncontroSelecionado() {
        return encontroSelecionado;
    }

    public void setEncontroSelecionado(Encontro encontroSelecionado) {
        this.encontroSelecionado = encontroSelecionado;
    }

    public Pessoa getPessoaSelecionada() {
        return pessoaSelecionada;
    }

    public void setPessoaSelecionada(Pessoa pessoaSelecionada) {
        this.pessoaSelecionada = pessoaSelecionada;
    }

    public Equipe getEquipeSelecionada() {
        return equipeSelecionada;
    }

    public void setEquipeSelecionada(Equipe equipeSelecionada) {
        this.equipeSelecionada = equipeSelecionada;
    }

    public List<Map<String, Object>> getResultadoRelatorio() {
        return resultadoRelatorio;
    }

    public void setResultadoRelatorio(List<Map<String, Object>> resultadoRelatorio) {
        this.resultadoRelatorio = resultadoRelatorio;
    }

    public String getTipoRelatorioSelecionado() {
        return tipoRelatorioSelecionado;
    }

    public void setTipoRelatorioSelecionado(String tipoRelatorioSelecionado) {
        this.tipoRelatorioSelecionado = tipoRelatorioSelecionado;
    }

    public Encontro getEncontroAnteriorSelecionado() {
        return encontroAnteriorSelecionado;
    }

    public void setEncontroAnteriorSelecionado(Encontro encontroAnteriorSelecionado) {
        this.encontroAnteriorSelecionado = encontroAnteriorSelecionado;
    }

    public List<String> getMeses() {
        return meses;
    }

    public void setMeses(List<String> meses) {
        this.meses = meses;
    }

    public Month getMesSelecionado() {
        return mesSelecionado;
    }

    public void setMesSelecionado(Month mesSelecionado) {
        this.mesSelecionado = mesSelecionado;
    }

}