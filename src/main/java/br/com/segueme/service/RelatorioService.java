package br.com.segueme.service;

import java.time.Month;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.repository.RelatorioRepository;

/**
 * Serviço de relatórios que delega ao repositório de relatórios.
 * Centraliza a lógica de consulta de relatórios que antes estava no RelatorioController.
 */
@ApplicationScoped
public class RelatorioService {

    @Inject
    private RelatorioRepository relatorioRepository;

    public List<Map<String, Object>> buscarEncontristasPorEncontro(Long encontroId) {
        return relatorioRepository.buscarEncontristasPorEncontro(encontroId);
    }

    public List<Map<String, Object>> buscarTrabalhadoresPorEquipe(Long equipeId) {
        return relatorioRepository.buscarTrabalhadoresPorEquipe(equipeId);
    }

    public List<Map<String, Object>> buscarContribuicoesPorEncontro() {
        return relatorioRepository.buscarContribuicoesPorEncontro();
    }

    public List<Map<String, Object>> buscarCasaisTrabalhadores(Long encontroId) {
        return relatorioRepository.buscarCasaisTrabalhadores(encontroId);
    }

    public List<Map<String, Object>> buscarEncontristasQueSeTornaramTrabalhadores(Long encontroAnteriorId, Long encontroAtualId) {
        return relatorioRepository.buscarEncontristasQueSeTornaramTrabalhadores(encontroAnteriorId, encontroAtualId);
    }

    public List<Map<String, Object>> buscarDirigentesPorPasta() {
        return relatorioRepository.buscarDirigentesPorPasta();
    }

    public List<Map<String, Object>> buscarDistribuicaoTrabalhadoresPorEquipe(Long encontroId) {
        return relatorioRepository.buscarDistribuicaoTrabalhadoresPorEquipe(encontroId);
    }

    public List<Map<String, Object>> buscarFinanceiroContribuicoes(Long encontroId) {
        return relatorioRepository.buscarFinanceiroContribuicoes(encontroId);
    }

    public List<Map<String, Object>> buscarAniversariantesMes(Long encontroId, Month mes) {
        return relatorioRepository.buscarAniversariantesMes(encontroId, mes);
    }

    public List<Map<String, Object>> buscarHistoricoParticipacao(Long pessoaId) {
        return relatorioRepository.buscarHistoricoParticipacao(pessoaId);
    }

    public List<Map<String, Object>> buscarPalestrasPorEncontro(Long encontroId) {
        return relatorioRepository.buscarPalestrasPorEncontro(encontroId);
    }

    public List<Map<String, Object>> buscarAptidoesPorEncontro(Long encontroId) {
        return relatorioRepository.buscarAptidoesPorEncontro(encontroId);
    }

    public List<Map<String, Object>> buscarAptidoesSeguimistasPorEncontro(Long encontroId) {
        return relatorioRepository.buscarAptidoesSeguimistasPorEncontro(encontroId);
    }
}
