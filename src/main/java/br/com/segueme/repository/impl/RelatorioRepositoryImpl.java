package br.com.segueme.repository.impl;

import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import br.com.segueme.repository.RelatorioRepository;

/**
 * Implementação do repositório de relatórios.
 * Centraliza todas as queries nativas que antes estavam no RelatorioController.
 */
@Stateless
public class RelatorioRepositoryImpl implements RelatorioRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Map<String, Object>> buscarEncontristasPorEncontro(Long encontroId) {
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
        query.setParameter("encontro_id", encontroId);
        return converterResultado(query,
                new String[] { "encontrista_id", "nome", "email", "telefone", "data_nascimento", "valor_pago",
                        "forma_pagamento", "data_inscricao" });
    }

    @Override
    public List<Map<String, Object>> buscarTrabalhadoresPorEquipe(Long equipeId) {
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
        query.setParameter("equipe_id", equipeId);
        return converterResultado(query,
                new String[] { "trabalhador_id", "nome", "email", "telefone", "coordenador", "data_inicio",
                        "foi_encontrista" });
    }

    @Override
    public List<Map<String, Object>> buscarContribuicoesPorEncontro() {
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
        return converterResultado(query,
                new String[] { "encontro", "data_inicio", "total_contribuicoes", "valor_total", "valor_medio" });
    }

    @Override
    public List<Map<String, Object>> buscarCasaisTrabalhadores(Long encontroId) {
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
        query.setParameter("encontro_id", encontroId);
        return converterResultado(query,
                new String[] { "casal_id", "esposo", "esposa", "data_casamento", "equipe_esposo" });
    }

    @Override
    public List<Map<String, Object>> buscarEncontristasQueSeTornaramTrabalhadores(Long encontroAnteriorId, Long encontroAtualId) {
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
        query.setParameter("encontro_anterior_id", encontroAnteriorId);
        query.setParameter("encontro_atual_id", encontroAtualId);
        return converterResultado(query,
                new String[] { "nome", "encontro_anterior", "encontro_atual", "equipe_atual" });
    }

    @Override
    public List<Map<String, Object>> buscarDirigentesPorPasta() {
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
        return converterResultado(query,
                new String[] { "pasta", "dirigente", "data_inicio", "data_fim", "dias_mandato", "ativo" });
    }

    @Override
    public List<Map<String, Object>> buscarDistribuicaoTrabalhadoresPorEquipe(Long encontroId) {
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
        query.setParameter("encontro_id", encontroId);
        return converterResultado(query,
                new String[] { "equipe", "tipo_equipe", "total_trabalhadores", "total_coordenadores",
                        "total_ex_encontristas" });
    }

    @Override
    public List<Map<String, Object>> buscarFinanceiroContribuicoes(Long encontroId) {
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
        query.setParameter("encontro_id", encontroId);
        return converterResultado(query,
                new String[] { "forma_pagamento", "quantidade", "valor_total", "menor_valor", "maior_valor",
                        "valor_medio" });
    }

    @Override
    public List<Map<String, Object>> buscarAniversariantesMes(Long encontroId, Month mes) {
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
        query.setParameter("encontro_id", encontroId);
        query.setParameter("mes", mes.getValue());
        return converterResultado(query,
                new String[] { "nome", "data_nascimento", "dia_aniversario", "tipo", "equipe" });
    }

    @Override
    public List<Map<String, Object>> buscarHistoricoParticipacao(Long pessoaId) {
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
        query.setParameter("pessoa_id", pessoaId);
        return converterResultado(query,
                new String[] { "nome", "encontro", "data_inicio", "tipo_participacao", "equipe", "coordenador" });
    }

    @Override
    public List<Map<String, Object>> buscarPalestrasPorEncontro(Long encontroId) {
        String sql = "SELECT " +
                "pal.tema AS palestra_tema, " +
                "pal.data_hora AS palestra_data_hora, " +
                "COALESCE(p_individual.nome, CONCAT(p_casal_pessoa1.nome, ' e ', p_casal_pessoa2.nome)) AS palestrante_nome, " +
                "CASE " +
                "    WHEN pstr.pessoa_id IS NOT NULL THEN 'Individual' " +
                "    WHEN pstr.casal_id IS NOT NULL THEN 'Casal' " +
                "    ELSE 'N/A' " +
                "END AS tipo_palestrante " +
                "FROM palestra pal " +
                "JOIN palestra_palestrante pp ON pal.id = pp.palestra_id " +
                "JOIN palestrante pstr ON pp.palestrante_id = pstr.id " +
                "LEFT JOIN pessoa p_individual ON pstr.pessoa_id = p_individual.id " +
                "LEFT JOIN casal c ON pstr.casal_id = c.id " +
                "LEFT JOIN pessoa p_casal_pessoa1 ON c.pessoa1_id = p_casal_pessoa1.id " +
                "LEFT JOIN pessoa p_casal_pessoa2 ON c.pessoa2_id = p_casal_pessoa2.id " +
                "WHERE pal.encontro_id = :encontro_id " +
                "ORDER BY pal.data_hora, palestrante_nome";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("encontro_id", encontroId);
        return converterResultado(query,
                new String[] { "palestra_tema", "palestra_data_hora", "palestrante_nome", "tipo_palestrante" });
    }

    @Override
    public List<Map<String, Object>> buscarAptidoesPorEncontro(Long encontroId) {
        String sql = "SELECT " +
                "p.nome, " +
                "CASE WHEN p.sexo = 'M' THEN 'Masculino' WHEN p.sexo = 'F' THEN 'Feminino' ELSE 'N/I' END AS sexo, " +
                "eq.nome AS equipe, " +
                "tipo.nome AS tipo_equipe, " +
                "CASE WHEN t.eh_coordenador = true THEN 'Sim' ELSE 'Não' END AS coordenador, " +
                "CASE WHEN t.apto_para_palestrar = true THEN 'Sim' ELSE 'Não' END AS apto_palestrar, " +
                "CASE WHEN t.apto_para_coordenar = true THEN 'Sim' ELSE 'Não' END AS apto_coordenar, " +
                "CASE WHEN t.foi_encontrista = true THEN 'Sim' ELSE 'Não' END AS foi_encontrista " +
                "FROM trabalhador t " +
                "JOIN pessoa p ON t.pessoa_id = p.id " +
                "JOIN equipe eq ON t.equipe_id = eq.id " +
                "JOIN tipo_equipe tipo ON eq.tipo_equipe_id = tipo.id " +
                "WHERE t.encontro_id = :encontro_id " +
                "ORDER BY p.nome";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("encontro_id", encontroId);
        return converterResultado(query,
                new String[] { "nome", "sexo", "equipe", "tipo_equipe", "coordenador",
                        "apto_palestrar", "apto_coordenar", "foi_encontrista" });
    }

    @Override
    public List<Map<String, Object>> buscarAptidoesSeguimistasPorEncontro(Long encontroId) {
        String sql = "SELECT " +
                "p.nome, " +
                "CASE WHEN p.sexo = 'M' THEN 'Masculino' WHEN p.sexo = 'F' THEN 'Feminino' ELSE 'N/I' END AS sexo, " +
                "COALESCE(e.circulo, 'N/I') AS circulo, " +
                "e.valor_pago, " +
                "COALESCE(e.forma_pagamento, 'N/I') AS forma_pagamento, " +
                "CASE WHEN e.ativo = true THEN 'Sim' ELSE 'Não' END AS ativo, " +
                "COALESCE(( " +
                "    SELECT STRING_AGG( " +
                "        CASE ea.aptidao " +
                "            WHEN 'ANIMACAO' THEN 'Animação' " +
                "            WHEN 'CANTO' THEN 'Canto' " +
                "            WHEN 'COZINHA' THEN 'Cozinha' " +
                "            WHEN 'ESTACIONAMENTO' THEN 'Estacionamento' " +
                "            WHEN 'FAXINA' THEN 'Faxina' " +
                "            WHEN 'GRAFICA' THEN 'Gráfica' " +
                "            WHEN 'LANCHE' THEN 'Lanche' " +
                "            WHEN 'LITURGIA' THEN 'Liturgia' " +
                "            WHEN 'MINI_MERCADO' THEN 'Mini Mercado' " +
                "            WHEN 'SALA' THEN 'Sala' " +
                "            WHEN 'VIGILIA_E_LITURGIA' THEN 'Vigília e Liturgia' " +
                "            WHEN 'VIGILIA_PAROQUIAL' THEN 'Vigília Paroquial' " +
                "            ELSE ea.aptidao " +
                "        END, ', ' ORDER BY ea.aptidao " +
                "    ) " +
                "    FROM encontrista_aptidao ea " +
                "    WHERE ea.encontrista_id = e.id " +
                "), 'Nenhuma') AS aptidoes " +
                "FROM encontrista e " +
                "JOIN pessoa p ON e.pessoa_id = p.id " +
                "WHERE e.encontro_id = :encontro_id " +
                "ORDER BY p.nome";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("encontro_id", encontroId);
        return converterResultado(query,
                new String[] { "nome", "sexo", "circulo", "valor_pago", "forma_pagamento", "ativo", "aptidoes" });
    }

    // ==================== Método utilitário ====================

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> converterResultado(Query query, String[] keys) {
        List<Object[]> results = query.getResultList();
        List<Map<String, Object>> mapList = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], row[i]);
            }
            mapList.add(map);
        }

        return mapList;
    }
}
