package br.com.segueme.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import br.com.segueme.entity.MovimentoFinanceiro;
import br.com.segueme.enums.TipoMovimento;

/**
 * Repository para operações com MovimentoFinanceiro.
 */
public interface MovimentoFinanceiroRepository {

    /**
     * Salva um novo movimento financeiro
     */
    MovimentoFinanceiro save(MovimentoFinanceiro movimento);

    /**
     * Atualiza um movimento financeiro existente
     */
    MovimentoFinanceiro update(MovimentoFinanceiro movimento);

    /**
     * Busca um movimento pelo ID
     */
    Optional<MovimentoFinanceiro> findById(Long id);

    /**
     * Busca todos os movimentos financeiros
     */
    List<MovimentoFinanceiro> findAll();

    /**
     * Busca movimentos por encontro
     */
    List<MovimentoFinanceiro> findByEncontro(Long encontroId);

    /**
     * Busca movimentos por tipo (RECEITA ou DESPESA) e encontro
     */
    List<MovimentoFinanceiro> findByTipoAndEncontro(TipoMovimento tipo, Long encontroId);

    /**
     * Remove um movimento financeiro
     */
    boolean delete(Long id);

    /**
     * Calcula o total de receitas de um encontro
     */
    BigDecimal somarReceitasPorEncontro(Long encontroId);

    /**
     * Calcula o total de despesas de um encontro
     */
    BigDecimal somarDespesasPorEncontro(Long encontroId);

    /**
     * Conta o total de movimentos
     */
    long count();
}
