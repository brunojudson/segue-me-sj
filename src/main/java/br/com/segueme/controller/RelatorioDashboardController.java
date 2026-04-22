package br.com.segueme.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Contribuicao;
import br.com.segueme.entity.Encontrista;
import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Equipe;
import br.com.segueme.entity.Palestra;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.entity.Trabalhador;
import br.com.segueme.enums.Sacramento;
import br.com.segueme.service.ContribuicaoService;
import br.com.segueme.service.EncontristaService;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.EquipeService;
import br.com.segueme.service.PalestraService;
import br.com.segueme.service.PessoaService;
import br.com.segueme.service.TrabalhadorService;

/**
 * Controller responsável exclusivamente pelo dashboard de relatórios executivos.
 * apenas agrega dados para visualização.
 * Utiliza os mesmos services já existentes no sistema.
 */
@Named
@ViewScoped
public class RelatorioDashboardController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EncontroService encontroService;
    @Inject
    private EncontristaService encontristaService;
    @Inject
    private TrabalhadorService trabalhadorService;
    @Inject
    private EquipeService equipeService;
    @Inject
    private ContribuicaoService contribuicaoService;
    @Inject
    private PessoaService pessoaService;
    @Inject
    private PalestraService palestraService;

    // Dados brutos carregados dos services
    private List<Encontro> encontros;
    private List<Encontrista> encontristas;
    private List<Trabalhador> trabalhadores;
    private List<Equipe> equipes;
    private List<Contribuicao> contribuicoes;
    private List<Pessoa> pessoas;
    private List<Palestra> palestras;

    // Dados computados
    private List<ResumoEncontro> resumoEncontros;
    private boolean carregado;

    @PostConstruct
    public void init() {
        carregarDados();
    }

    private void carregarDados() {
        encontros = encontroService.buscarTodos();
        encontristas = encontristaService.buscarTodos();
        trabalhadores = trabalhadorService.buscarTodos();
        equipes = equipeService.buscarTodos();
        contribuicoes = contribuicaoService.buscarTodos();
        pessoas = pessoaService.buscarTodosComSacramentos();
        palestras = palestraService.buscarTodos();

        calcularResumos();
        carregado = true;
    }

    private void calcularResumos() {
        resumoEncontros = new ArrayList<>();
        List<Encontro> sorted = new ArrayList<>(encontros);
        sorted.sort(Comparator.comparing(
                (Encontro e) -> e.getDataInicio() != null ? e.getDataInicio() : LocalDate.MIN).reversed());

        for (Encontro enc : sorted) {
            long totalEnc = encontristas.stream()
                    .filter(e -> e.getEncontro() != null && e.getEncontro().getId().equals(enc.getId()))
                    .count();
            long totalTrab = trabalhadores.stream()
                    .filter(t -> t.getEncontro() != null && t.getEncontro().getId().equals(enc.getId()))
                    .count();
            long totalEq = equipes.stream()
                    .filter(eq -> eq.getEncontro() != null && eq.getEncontro().getId().equals(enc.getId()))
                    .count();
            BigDecimal totalContrib = contribuicoes.stream()
                    .filter(c -> c.getTrabalhador() != null && c.getTrabalhador().getEncontro() != null
                            && c.getTrabalhador().getEncontro().getId().equals(enc.getId()))
                    .map(c -> c.getValor() != null ? c.getValor() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String status = calcularStatus(enc);

            ResumoEncontro resumo = new ResumoEncontro();
            resumo.setEncontro(enc);
            resumo.setTotalEncontristas((int) totalEnc);
            resumo.setTotalTrabalhadores((int) totalTrab);
            resumo.setTotalEquipes((int) totalEq);
            resumo.setTotalContribuicoes(totalContrib);
            resumo.setStatus(status);
            resumoEncontros.add(resumo);
        }
    }

    private String calcularStatus(Encontro e) {
        LocalDate agora = LocalDate.now();
        if (e.getAtivo() == null || !e.getAtivo()) return "Realizado";
        if (e.getDataInicio() != null && e.getDataInicio().isAfter(agora)) return "Futuro";
        if (e.getDataFim() != null && e.getDataFim().isBefore(agora)) return "Em andamento";
        return "Inativo";
    }

    // ===================== VISÃO GERAL =====================

    public int getTotalEncontros() {
        return encontros != null ? encontros.size() : 0;
    }

    public int getTotalEncontristas() {
        return encontristas != null ? encontristas.size() : 0;
    }

    public int getTotalTrabalhadores() {
        return trabalhadores != null ? trabalhadores.size() : 0;
    }

    public int getTotalPessoas() {
        return pessoas != null ? pessoas.size() : 0;
    }

    public int getTotalPessoasAtivas() {
        if (pessoas == null) return 0;
        return (int) pessoas.stream().filter(p -> p.getAtivo() != null && p.getAtivo()).count();
    }

    public int getTotalEquipes() {
        return equipes != null ? equipes.size() : 0;
    }

    public int getTotalPalestras() {
        return palestras != null ? palestras.size() : 0;
    }

    public BigDecimal getTotalContribuicoesValor() {
        if (contribuicoes == null) return BigDecimal.ZERO;
        return contribuicoes.stream()
                .map(c -> c.getValor() != null ? c.getValor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getMediaEncontristasPorEncontro() {
        if (encontros == null || encontros.isEmpty()) return 0;
        return Math.round((float) getTotalEncontristas() / encontros.size());
    }

    public int getMediaTrabPorEncontro() {
        if (encontros == null || encontros.isEmpty()) return 0;
        return Math.round((float) getTotalTrabalhadores() / encontros.size());
    }

    // ===================== PESSOAS =====================

    public int getPessoasMasculino() {
        if (pessoas == null) return 0;
        return (int) pessoas.stream()
                .filter(p -> p.getSexo() != null && (p.getSexo() == 'M' || p.getSexo() == 'm'))
                .count();
    }

    public int getPessoasFeminino() {
        if (pessoas == null) return 0;
        return (int) pessoas.stream()
                .filter(p -> p.getSexo() != null && (p.getSexo() == 'F' || p.getSexo() == 'f'))
                .count();
    }

    public int getPessoasSexoNaoInformado() {
        return getTotalPessoas() - getPessoasMasculino() - getPessoasFeminino();
    }

    public int getPercentMasculino() {
        int total = getTotalPessoas();
        if (total == 0) return 0;
        return Math.round((float) getPessoasMasculino() / total * 100);
    }

    public int getPercentFeminino() {
        int total = getTotalPessoas();
        if (total == 0) return 0;
        return Math.round((float) getPessoasFeminino() / total * 100);
    }

    public int getPercentSexoNaoInformado() {
        int total = getTotalPessoas();
        if (total == 0) return 0;
        return Math.round((float) getPessoasSexoNaoInformado() / total * 100);
    }

    /**
     * Distribuição de sacramentos entre as pessoas cadastradas.
     * Retorna mapa ordenado por contagem decrescente.
     */
    public List<Map<String, Object>> getSacramentosDistribuicao() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        if (pessoas == null) return resultado;

        Map<String, Long> contagem = new LinkedHashMap<>();
        for (Pessoa p : pessoas) {
            if (p.getSacramentos() != null) {
                for (Sacramento s : p.getSacramentos()) {
                    contagem.merge(s.getDescricao(), 1L, Long::sum);
                }
            }
        }

        contagem.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("label", entry.getKey());
                    item.put("count", entry.getValue());
                    int total = getTotalPessoas();
                    item.put("percent", total > 0 ? Math.round((float) entry.getValue() / total * 100) : 0);
                    resultado.add(item);
                });

        return resultado;
    }

    // ===================== TRABALHADORES EX-ENCONTRISTAS =====================

    public int getTrabExEncontristas() {
        if (trabalhadores == null) return 0;
        return (int) trabalhadores.stream()
                .filter(t -> t.getFoiEncontrista() != null && t.getFoiEncontrista())
                .count();
    }

    public int getPercentualExEncontristas() {
        if (trabalhadores == null || trabalhadores.isEmpty()) return 0;
        return Math.round((float) getTrabExEncontristas() / trabalhadores.size() * 100);
    }

    // ===================== FINANCEIRO =====================

    public int getTotalContribuicoesQuantidade() {
        return contribuicoes != null ? contribuicoes.size() : 0;
    }

    public BigDecimal getTicketMedio() {
        if (contribuicoes == null || contribuicoes.isEmpty()) return BigDecimal.ZERO;
        return getTotalContribuicoesValor().divide(
                BigDecimal.valueOf(contribuicoes.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Contribuições agrupadas por forma de pagamento.
     */
    public List<Map<String, Object>> getContribuicoesPorForma() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        if (contribuicoes == null) return resultado;

        Map<String, BigDecimal[]> agrupado = new LinkedHashMap<>();
        for (Contribuicao c : contribuicoes) {
            String forma = c.getFormaPagamento() != null ? c.getFormaPagamento() : "Não informado";
            agrupado.computeIfAbsent(forma, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal[] valores = agrupado.get(forma);
            valores[0] = valores[0].add(BigDecimal.ONE); // count
            valores[1] = valores[1].add(c.getValor() != null ? c.getValor() : BigDecimal.ZERO); // total
        }

        BigDecimal totalGeral = getTotalContribuicoesValor();

        agrupado.entrySet().stream()
                .sorted((a, b) -> b.getValue()[1].compareTo(a.getValue()[1]))
                .forEach(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("label", entry.getKey());
                    item.put("count", entry.getValue()[0].intValue());
                    item.put("total", entry.getValue()[1]);
                    item.put("percent", totalGeral.compareTo(BigDecimal.ZERO) > 0
                            ? entry.getValue()[1].multiply(BigDecimal.valueOf(100))
                                    .divide(totalGeral, 0, RoundingMode.HALF_UP).intValue()
                            : 0);
                    resultado.add(item);
                });

        return resultado;
    }

    // ===================== EQUIPES =====================

    public int getEquipesAtivas() {
        if (equipes == null) return 0;
        return (int) equipes.stream()
                .filter(eq -> eq.getAtivo() != null && eq.getAtivo())
                .count();
    }

    /**
     * Equipes agrupadas por tipo de equipe.
     */
    public List<Map<String, Object>> getEquipesPorTipo() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        if (equipes == null) return resultado;

        Map<String, Long> contagem = new LinkedHashMap<>();
        for (Equipe eq : equipes) {
            String tipo = eq.getTipoEquipe() != null ? eq.getTipoEquipe().getNome() : "Sem tipo";
            contagem.merge(tipo, 1L, Long::sum);
        }

        int total = getTotalEquipes();
        contagem.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("label", entry.getKey());
                    item.put("count", entry.getValue());
                    item.put("percent", total > 0 ? Math.round((float) entry.getValue() / total * 100) : 0);
                    resultado.add(item);
                });

        return resultado;
    }

    // ===================== ANÁLISE AVANÇADA =====================

    /** Faixa etária permitida para participação como encontrista. */
    private static final int IDADE_MIN = 16;
    private static final int IDADE_MAX = 23;

    /**
     * Distribuição etária dos encontristas dentro da faixa permitida (16–23 anos),
     * mais bucket de fora da faixa para identificar irregularidades.
     */
    public List<Map<String, Object>> getDistribuicaoEtariaEncontristas() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        if (encontristas == null || encontristas.isEmpty()) return resultado;

        // Faixas dentro do permitido: 16-17, 18-20, 21-23; fora: <16, >23
        int abaixo = 0, f16a17 = 0, f18a20 = 0, f21a23 = 0, acima = 0, semIdade = 0;

        for (Encontrista e : encontristas) {
            Integer idade = e.getIdade();
            if (idade == null) { semIdade++; continue; }
            if (idade < 16)       abaixo++;
            else if (idade <= 17) f16a17++;
            else if (idade <= 20) f18a20++;
            else if (idade <= 23) f21a23++;
            else                  acima++;
        }

        int totalComIdade = abaixo + f16a17 + f18a20 + f21a23 + acima;

        // Faixas dentro da faixa permitida
        int[][] dentro = {{f16a17}, {f18a20}, {f21a23}};
        String[] rotulosDentro = {"16–17 anos", "18–20 anos", "21–23 anos"};
        for (int i = 0; i < rotulosDentro.length; i++) {
            if (dentro[i][0] == 0) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", rotulosDentro[i]);
            item.put("count", dentro[i][0]);
            item.put("percent", totalComIdade > 0 ? Math.round((float) dentro[i][0] / totalComIdade * 100) : 0);
            item.put("foraFaixa", false);
            resultado.add(item);
        }
        // Fora da faixa
        if (abaixo > 0) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", "Abaixo de 16 anos ⚠");
            item.put("count", abaixo);
            item.put("percent", totalComIdade > 0 ? Math.round((float) abaixo / totalComIdade * 100) : 0);
            item.put("foraFaixa", true);
            resultado.add(item);
        }
        if (acima > 0) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", "Acima de 23 anos ⚠");
            item.put("count", acima);
            item.put("percent", totalComIdade > 0 ? Math.round((float) acima / totalComIdade * 100) : 0);
            item.put("foraFaixa", true);
            resultado.add(item);
        }
        if (semIdade > 0) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", "Idade não informada");
            item.put("count", semIdade);
            item.put("percent", 0);
            item.put("foraFaixa", false);
            resultado.add(item);
        }
        return resultado;
    }

    /** Total de encontristas fora da faixa etária permitida (16–23). */
    public int getEncontristasForaDaFaixa() {
        if (encontristas == null) return 0;
        return (int) encontristas.stream()
                .filter(e -> e.getIdade() != null)
                .filter(e -> e.getIdade() < IDADE_MIN || e.getIdade() > IDADE_MAX)
                .count();
    }

    /** Percentual de encontristas dentro da faixa permitida (16–23). */
    public int getPercentDentroFaixa() {
        if (encontristas == null || encontristas.isEmpty()) return 0;
        long comIdade = encontristas.stream().filter(e -> e.getIdade() != null).count();
        if (comIdade == 0) return 0;
        long dentroFaixa = encontristas.stream()
                .filter(e -> e.getIdade() != null)
                .filter(e -> e.getIdade() >= IDADE_MIN && e.getIdade() <= IDADE_MAX)
                .count();
        return Math.round((float) dentroFaixa / comIdade * 100);
    }

    /**
     * Idadde média dos encontristas (apenas os que têm idade informada).
     */
    public String getIdadeMediaEncontristas() {
        if (encontristas == null || encontristas.isEmpty()) return "—";
        double media = encontristas.stream()
                .filter(e -> e.getIdade() != null)
                .mapToInt(Encontrista::getIdade)
                .average()
                .orElse(0);
        return media == 0 ? "—" : String.format("%.1f", media).replace(".", ",") + " anos";
    }

    /**
     * Conversão Encontrista → Trabalhador agrupada por encontro.
     * Para cada encontro, calcula quantos encontristas desse encontro tornaram-se trabalhadores em qualquer encontro posterior.
     */
    public List<Map<String, Object>> getConversaoPorEncontro() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        if (encontros == null || encontristas == null || trabalhadores == null) return resultado;

        List<Encontro> sorted = new ArrayList<>(encontros);
        sorted.sort(Comparator.comparing(
                (Encontro e) -> e.getDataInicio() != null ? e.getDataInicio() : java.time.LocalDate.MIN));

        for (Encontro enc : sorted) {
            List<Encontrista> doEncontro = encontristas.stream()
                    .filter(e -> e.getEncontro() != null && e.getEncontro().getId().equals(enc.getId()))
                    .collect(Collectors.toList());
            if (doEncontro.isEmpty()) continue;

            // Contar quantos se tornaram trabalhadores (em qualquer encontro)
            long convertidos = doEncontro.stream()
                    .filter(e -> e.getPessoa() != null)
                    .filter(e -> trabalhadores.stream()
                            .anyMatch(t -> t.getPessoa() != null
                                    && t.getPessoa().getId().equals(e.getPessoa().getId())))
                    .count();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", enc.getNome());
            item.put("total", doEncontro.size());
            item.put("convertidos", (int) convertidos);
            item.put("percent", doEncontro.size() > 0
                    ? Math.round((float) convertidos / doEncontro.size() * 100) : 0);
            resultado.add(item);
        }
        return resultado;
    }

    /**
     * Quantidade de encontristas com dados de saúde/emergência preenchidos.
     */
    public int getEncontristasComDadosSaude() {
        if (encontristas == null) return 0;
        return (int) encontristas.stream()
                .filter(e -> isNaoVazioStr(e.getAlergias())
                        || isNaoVazioStr(e.getMedicamentos())
                        || isNaoVazioStr(e.getCondicaoMedica())
                        || isNaoVazioStr(e.getContatoEmergenciaNome()))
                .count();
    }

    /**
     * Quantidade de encontristas menores de idade (< 18 anos) sem dados de responsável.
     */
    public int getMenoresComDadosResponsavel() {
        if (encontristas == null) return 0;
        return (int) encontristas.stream()
                .filter(e -> e.getIdade() != null && e.getIdade() < 18)
                .filter(e -> isNaoVazioStr(e.getResponsavelNome()))
                .count();
    }

    public int getMenoresTotal() {
        if (encontristas == null) return 0;
        return (int) encontristas.stream()
                .filter(e -> e.getIdade() != null && e.getIdade() < 18)
                .count();
    }

    private boolean isNaoVazioStr(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /**
     * Taxa de crescimento de encontristas entre o penúltimo e o último encontro.
     * Retorna string descritiva.
     */
    public String getCrescimentoUltimoEncontro() {
        if (resumoEncontros == null || resumoEncontros.size() < 2) return "—";
        // resumoEncontros é ordenado DESC (mais recente primeiro)
        ResumoEncontro ultimo = resumoEncontros.get(0);
        ResumoEncontro penultimo = resumoEncontros.get(1);
        int diff = ultimo.getTotalEncontristas() - penultimo.getTotalEncontristas();
        if (penultimo.getTotalEncontristas() == 0) return "—";
        int pct = Math.round((float) diff / penultimo.getTotalEncontristas() * 100);
        return (pct >= 0 ? "+" : "") + pct + "%";
    }

    public boolean isCrescimentoPositivo() {
        if (resumoEncontros == null || resumoEncontros.size() < 2) return false;
        return resumoEncontros.get(0).getTotalEncontristas() >= resumoEncontros.get(1).getTotalEncontristas();
    }

    // ===================== STATUS HELPERS =====================

    public String getStatusCssClass(String status) {
        if (status == null) return "tag-muted";
        switch (status) {
            case "Futuro": return "tag-blue";
            case "Em andamento": return "tag-green";
            case "Realizado": return "tag-yellow";
            case "Inativo": return "tag-red";
            default: return "tag-muted";
        }
    }

    /**
     * Retorna o percentual de encontristas em relação ao encontro com mais encontristas.
     * Usado para barras horizontais proporcionais.
     */
    public int getBarPercent(int valor) {
        if (resumoEncontros == null || resumoEncontros.isEmpty()) return 0;
        int max = resumoEncontros.stream()
                .mapToInt(ResumoEncontro::getTotalEncontristas)
                .max().orElse(1);
        if (max == 0) return 0;
        return Math.round((float) valor / max * 100);
    }

    /**
     * Retorna percentual de contribuições de um encontro em relação ao encontro com mais contribuições.
     */
    public int getBarPercentContrib(BigDecimal valor) {
        if (resumoEncontros == null || resumoEncontros.isEmpty() || valor == null) return 0;
        BigDecimal max = resumoEncontros.stream()
                .map(ResumoEncontro::getTotalContribuicoes)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ONE);
        if (max.compareTo(BigDecimal.ZERO) == 0) return 0;
        return valor.multiply(BigDecimal.valueOf(100)).divide(max, 0, RoundingMode.HALF_UP).intValue();
    }

    // ===================== GETTERS =====================

    public List<ResumoEncontro> getResumoEncontros() {
        return resumoEncontros;
    }

    public boolean isCarregado() {
        return carregado;
    }

    // ===================== INNER CLASS =====================

    public static class ResumoEncontro implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Encontro encontro;
        private int totalEncontristas;
        private int totalTrabalhadores;
        private int totalEquipes;
        private BigDecimal totalContribuicoes = BigDecimal.ZERO;
        private String status;

        public Encontro getEncontro() { return encontro; }
        public void setEncontro(Encontro encontro) { this.encontro = encontro; }
        public int getTotalEncontristas() { return totalEncontristas; }
        public void setTotalEncontristas(int totalEncontristas) { this.totalEncontristas = totalEncontristas; }
        public int getTotalTrabalhadores() { return totalTrabalhadores; }
        public void setTotalTrabalhadores(int totalTrabalhadores) { this.totalTrabalhadores = totalTrabalhadores; }
        public int getTotalEquipes() { return totalEquipes; }
        public void setTotalEquipes(int totalEquipes) { this.totalEquipes = totalEquipes; }
        public BigDecimal getTotalContribuicoes() { return totalContribuicoes; }
        public void setTotalContribuicoes(BigDecimal totalContribuicoes) { this.totalContribuicoes = totalContribuicoes; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
