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
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Equipe;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.EquipeService;
import br.com.segueme.service.PessoaService;

@Named
@ViewScoped
public class RelatorioController implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private EncontroService encontroService;

    @Inject
    private PessoaService pessoaService;

    @Inject
    private EquipeService equipeService;

    private List<Encontro> encontros;
    private List<Pessoa> pessoas;
    private List<Equipe> equipes;
    private List<String> meses;
    
    private Encontro encontroSelecionado;
    private Encontro encontroAnteriorSelecionado;
    private Pessoa pessoaSelecionada;
    private Equipe equipeSelecionada;
    private Month mesSelecionado;

    private List<Map<String, Object>> resultadoRelatorio;
    private String tipoRelatorioSelecionado;

    @PostConstruct
    public void init() {
        carregarEncontros();
        carregarPessoas();
        carregarEquipes();
        mesSelecionado = LocalDate.now().getMonth();
        carregarMeses(); // Inicializa a lista de meses
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

        String sql = "SELECT " +
                    "e.id AS encontrista_id, " +
                    "p.nome, " +
                    "p.email, " +
                    "p.telefone, " +
                    "p.data_nascimento, " +
                    "e.valor_pago, " +
                    "e.forma_pagamento, " +
                    "e.data_inscricao " +
                    "FROM encontrista e " +
                    "JOIN pessoa p ON e.pessoa_id = p.id " +
                    "JOIN encontro enc ON e.encontro_id = enc.id " +
                    "WHERE enc.id = :encontro_id " +
                    "ORDER BY p.nome";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("encontro_id", encontroSelecionado.getId());
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        resultadoRelatorio = converterResultadoParaMapList(results,
                new String[]{"encontrista_id", "nome", "email", "telefone", "data_nascimento", "valor_pago", "forma_pagamento", "data_inscricao"});

        tipoRelatorioSelecionado = "Encontristas por Encontro";
    }

    public void gerarRelatorioTrabalhadoresPorEquipe() {
        if (equipeSelecionada == null) {
            return;
        }

        String sql = "SELECT " +
                    "t.id AS trabalhador_id, " +
                    "p.nome, " +
                    "p.email, " +
                    "p.telefone, " +
                    "CASE WHEN t.eh_coordenador = true THEN 'Sim' ELSE 'Não' END AS coordenador, " +
                    "t.data_inicio, " +
                    "CASE WHEN t.foi_encontrista = true THEN 'Sim' ELSE 'Não' END AS foi_encontrista " +
                    "FROM trabalhador t " +
                    "JOIN pessoa p ON t.pessoa_id = p.id " +
                    "JOIN equipe eq ON t.equipe_id = eq.id " +
                    "WHERE eq.id = :equipe_id " +
                    "ORDER BY eh_coordenador DESC, p.nome";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("equipe_id", equipeSelecionada.getId());
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        resultadoRelatorio = converterResultadoParaMapList(results,
                new String[]{"trabalhador_id", "nome", "email", "telefone", "coordenador", "data_inicio", "foi_encontrista"});

        tipoRelatorioSelecionado = "Trabalhadores por Equipe";
    }

    public void gerarRelatorioContribuicoesPorEncontro() {
        String sql = "SELECT " +
                    "enc.nome AS encontro, " +
                    "enc.data_inicio, " +
                    "COUNT(c.id) AS total_contribuicoes, " +
                    "SUM(c.valor) AS valor_total, " +
                    "AVG(c.valor) AS valor_medio " +
                    "FROM contribuicao c " +
                    "JOIN trabalhador t ON c.trabalhador_id = t.id " +
                    "JOIN encontro enc ON t.encontro_id = enc.id " +
                    "GROUP BY enc.id, enc.nome, enc.data_inicio " +
                    "ORDER BY enc.data_inicio DESC";

        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        resultadoRelatorio = converterResultadoParaMapList(results,
                new String[]{"encontro", "data_inicio", "total_contribuicoes", "valor_total", "valor_medio"});

        tipoRelatorioSelecionado = "Contribuições por Encontro";
    }

    public void gerarRelatorioCasaisTrabalhadores() {
        if (encontroSelecionado == null) {
            return;
        }

        String sql = "SELECT " +
                    "c.id AS casal_id, " +
                    "p_esposo.nome AS esposo, " +
                    "p_esposa.nome AS esposa, " +
                    "c.data_casamento, " +
                    "eq_esposo.nome AS equipe_esposo " +
                    "FROM casal c " +
                    "JOIN pessoa p_esposo ON c.pessoa1_id = p_esposo.id " +
                    "JOIN pessoa p_esposa ON c.pessoa2_id = p_esposa.id " +
                    "JOIN trabalhador t_esposo ON t_esposo.pessoa_id = p_esposo.id " +
                    "JOIN trabalhador t_esposa ON t_esposa.pessoa_id = p_esposa.id " +
                    "JOIN equipe eq_esposo ON t_esposo.equipe_id = eq_esposo.id " +
                    "WHERE t_esposo.encontro_id = :encontro_id " +
                    "AND t_esposa.encontro_id = :encontro_id " +
                    "ORDER BY c.data_casamento";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("encontro_id", encontroSelecionado.getId());
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        resultadoRelatorio = converterResultadoParaMapList(results,
        		new String[]{"casal_id", "esposo", "esposa", "data_casamento", "equipe_esposo"});

        tipoRelatorioSelecionado = "Casais Trabalhadores";
    }

    public void gerarRelatorioEncontristasQueSeTornaramTrabalhadores() {
        if (encontroAnteriorSelecionado == null || encontroSelecionado == null) {
            return;
        }

        String sql = "SELECT " +
                    "p.nome, " +
                    "enc_anterior.nome AS encontro_anterior, " +
                    "enc_atual.nome AS encontro_atual, " +
                    "eq.nome AS equipe_atual " +
                    "FROM encontrista e " +
                    "JOIN pessoa p ON e.pessoa_id = p.id " +
                    "JOIN encontro enc_anterior ON e.encontro_id = enc_anterior.id " +
                    "JOIN trabalhador t ON t.pessoa_id = p.id " +
                    "JOIN encontro enc_atual ON t.encontro_id = enc_atual.id " +
                    "JOIN equipe eq ON t.equipe_id = eq.id " +
                    "WHERE enc_anterior.id = :encontro_anterior_id " +
                    "AND enc_atual.id = :encontro_atual_id " +
                    "ORDER BY p.nome";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("encontro_anterior_id", encontroAnteriorSelecionado.getId());
        query.setParameter("encontro_atual_id", encontroSelecionado.getId());
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        resultadoRelatorio = converterResultadoParaMapList(results,
                new String[]{"nome", "encontro_anterior", "encontro_atual", "equipe_atual"});

        tipoRelatorioSelecionado = "Encontristas que se tornaram Trabalhadores";
    }

    public void gerarRelatorioDirigentesPorPasta() {
        String sql = "SELECT " +
                    "pasta.nome AS pasta, " +
                    "p.nome AS dirigente, " +
                    "d.data_inicio, " +
                    "d.data_fim, " +
                    "(d.data_fim - d.data_inicio) AS dias_mandato, " +
                    "CASE WHEN d.ativo = true THEN 'Sim' ELSE 'Não' END AS ativo " +
                    "FROM dirigente d " +
                    "JOIN trabalhador t ON d.trabalhador_id = t.id " +
                    "JOIN pessoa p ON t.pessoa_id = p.id " +
                    "JOIN pasta ON d.pasta_id = pasta.id " +
                    "ORDER BY pasta.nome, d.data_inicio DESC";

        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        resultadoRelatorio = converterResultadoParaMapList(results,
                new String[]{"pasta", "dirigente", "data_inicio", "data_fim", "dias_mandato", "ativo"});

        tipoRelatorioSelecionado = "Dirigentes por Pasta";
    }

    public void gerarRelatorioDistribuicaoTrabalhadoresPorEquipe() {
        if (encontroSelecionado == null) {
            return;
        }

        String sql = "SELECT " +
                    "eq.nome AS equipe, " +
                    "tipo.nome AS tipo_equipe, " +
                    "COUNT(t.id) AS total_trabalhadores, " +
                    "SUM(CASE WHEN t.eh_coordenador = true THEN 1 ELSE 0 END) AS total_coordenadores, " +
                    "SUM(CASE WHEN t.foi_encontrista = true THEN 1 ELSE 0 END) AS total_ex_encontristas " +
                    "FROM trabalhador t " +
                    "JOIN equipe eq ON t.equipe_id = eq.id " +
                    "JOIN tipo_equipe tipo ON eq.tipo_equipe_id = tipo.id " +
                    "WHERE t.encontro_id = :encontro_id " +
                    "GROUP BY eq.id, eq.nome, tipo.nome " +
                    "ORDER BY tipo.nome, eq.nome";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("encontro_id", encontroSelecionado.getId());
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        resultadoRelatorio = converterResultadoParaMapList(results,
                new String[]{"equipe", "tipo_equipe", "total_trabalhadores", "total_coordenadores", "total_ex_encontristas"});

        tipoRelatorioSelecionado = "Distribuição de Trabalhadores por Equipe";
    }

    public void gerarRelatorioFinanceiroContribuicoes() {
        if (encontroSelecionado == null) {
            return;
        }

        String sql = "SELECT " +
                    "c.forma_pagamento, " +
                    "COUNT(c.id) AS quantidade, " +
                    "SUM(c.valor) AS valor_total, " +
                    "MIN(c.valor) AS menor_valor, " +
                    "MAX(c.valor) AS maior_valor, " +
                    "AVG(c.valor) AS valor_medio " +
                    "FROM contribuicao c " +
                    "JOIN trabalhador t ON c.trabalhador_id = t.id " +
                    "WHERE t.encontro_id = :encontro_id " +
                    "GROUP BY c.forma_pagamento " +
                    "ORDER BY valor_total DESC";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("encontro_id", encontroSelecionado.getId());
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        resultadoRelatorio = converterResultadoParaMapList(results,
                new String[]{"forma_pagamento", "quantidade", "valor_total", "menor_valor", "maior_valor", "valor_medio"});

        tipoRelatorioSelecionado = "Financeiro de Contribuições";
    }

    public void gerarRelatorioAniversariantesMes() {
        if (mesSelecionado == null) {
            return;
        }

        String sql = "SELECT " +
                    "p.nome, " +
                    "p.data_nascimento, " +
                    "EXTRACT(DAY FROM p.data_nascimento) AS dia_aniversario, " +
                    "CASE " +
                    "    WHEN e.id IS NOT NULL THEN 'Encontrista' " +
                    "    WHEN t.id IS NOT NULL THEN 'Trabalhador' " +
                    "    ELSE 'Trabalhador' " +
                    "END AS tipo, " +
                    "COALESCE(eq.nome, 'N/A') AS equipe " +
                    "FROM pessoa p " +
                    "LEFT JOIN encontrista e ON e.pessoa_id = p.id AND e.encontro_id = :encontro_id " +
                    "LEFT JOIN trabalhador t ON t.pessoa_id = p.id AND t.encontro_id = :encontro_id " +
                    "LEFT JOIN equipe eq ON t.equipe_id = eq.id " +
                    "WHERE EXTRACT(MONTH FROM p.data_nascimento) = :mes " +
                    "ORDER BY EXTRACT(DAY FROM p.data_nascimento)";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("encontro_id", encontroSelecionado != null ? encontroSelecionado.getId() : null);
        query.setParameter("mes", mesSelecionado.getValue());
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        resultadoRelatorio = converterResultadoParaMapList(results,
                new String[]{"nome", "data_nascimento", "dia_aniversario", "tipo", "equipe"});

        tipoRelatorioSelecionado = "Aniversariantes do Mês";
    }

    public void gerarRelatorioHistoricoParticipacao() {
        if (pessoaSelecionada == null) {
            return;
        }

        String sql = "SELECT " +
                    "p.nome, " +
                    "enc.nome AS encontro, " +
                    "enc.data_inicio, " +
                    "CASE " +
                    "    WHEN e.id IS NOT NULL THEN 'Encontrista' " +
                    "    WHEN t.id IS NOT NULL THEN 'Trabalhador' " +
                    "    ELSE 'Não participou' " +
                    "END AS tipo_participacao, " +
                    "COALESCE(eq.nome, 'N/A') AS equipe, " +
                    "CASE WHEN t.eh_coordenador = true THEN 'Sim' ELSE 'Não' END AS coordenador " +
                    "FROM pessoa p " +
                    "CROSS JOIN encontro enc " +
                    "LEFT JOIN encontrista e ON e.pessoa_id = p.id AND e.encontro_id = enc.id " +
                    "LEFT JOIN trabalhador t ON t.pessoa_id = p.id AND t.encontro_id = enc.id " +
                    "LEFT JOIN equipe eq ON t.equipe_id = eq.id " +
                    "WHERE p.id = :pessoa_id " +
                    "ORDER BY enc.data_inicio DESC";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("pessoa_id", pessoaSelecionada.getId());
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        resultadoRelatorio = converterResultadoParaMapList(results,
                new String[]{"nome", "encontro", "data_inicio", "tipo_participacao", "equipe", "coordenador"});

        tipoRelatorioSelecionado = "Histórico de Participação";
    }

    private List<Map<String, Object>> converterResultadoParaMapList(List<Object[]> result, String[] keys) {
        List<Map<String, Object>> mapList = new ArrayList<>();
    
        for (Object[] row : result) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], row[i]);
            }
            mapList.add(map);
        }
    
        return mapList;
    }

	public List<Encontro> getEncontros() {
		return encontros;
	}

	public void setEncontros(List<Encontro> encontros) {
		this.encontros = encontros;
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