package br.com.segueme.repository;

import java.time.Month;
import java.util.List;
import java.util.Map;

/**
 * Interface de repositório para consultas de relatórios.
 * Centraliza todas as queries nativas que antes estavam no RelatorioController.
 */
public interface RelatorioRepository {

    List<Map<String, Object>> buscarEncontristasPorEncontro(Long encontroId);

    List<Map<String, Object>> buscarTrabalhadoresPorEquipe(Long equipeId);

    List<Map<String, Object>> buscarContribuicoesPorEncontro();

    List<Map<String, Object>> buscarCasaisTrabalhadores(Long encontroId);

    List<Map<String, Object>> buscarEncontristasQueSeTornaramTrabalhadores(Long encontroAnteriorId, Long encontroAtualId);

    List<Map<String, Object>> buscarDirigentesPorPasta();

    List<Map<String, Object>> buscarDistribuicaoTrabalhadoresPorEquipe(Long encontroId);

    List<Map<String, Object>> buscarFinanceiroContribuicoes(Long encontroId);

    List<Map<String, Object>> buscarAniversariantesMes(Long encontroId, Month mes);

    List<Map<String, Object>> buscarHistoricoParticipacao(Long pessoaId);

    List<Map<String, Object>> buscarPalestrasPorEncontro(Long encontroId);

    List<Map<String, Object>> buscarAptidoesPorEncontro(Long encontroId);

    List<Map<String, Object>> buscarAptidoesSeguimistasPorEncontro(Long encontroId);
}
