package br.com.segueme.repository.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.MovimentoFinanceiro;
import br.com.segueme.enums.TipoMovimento;
import br.com.segueme.repository.MovimentoFinanceiroRepository;

@Stateless
public class MovimentoFinanceiroRepositoryImpl implements MovimentoFinanceiroRepository {

    @PersistenceContext(unitName = "seguemePU")
    private EntityManager entityManager;

    @Override
    public MovimentoFinanceiro save(MovimentoFinanceiro movimento) {
        entityManager.persist(movimento);
        return movimento;
    }

    @Override
    public MovimentoFinanceiro update(MovimentoFinanceiro movimento) {
        return entityManager.merge(movimento);
    }

    @Override
    public Optional<MovimentoFinanceiro> findById(Long id) {
        try {
            MovimentoFinanceiro movimento = entityManager.createQuery(
                    "SELECT m FROM MovimentoFinanceiro m " +
                    "JOIN FETCH m.encontro " +
                    "WHERE m.id = :id", MovimentoFinanceiro.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(movimento);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<MovimentoFinanceiro> findAll() {
        return entityManager.createQuery(
                "SELECT m FROM MovimentoFinanceiro m " +
                "JOIN FETCH m.encontro " +
                "ORDER BY m.dataMovimento DESC",
                MovimentoFinanceiro.class)
                .getResultList();
    }

    @Override
    public List<MovimentoFinanceiro> findByEncontro(Long encontroId) {
        return entityManager.createQuery(
                "SELECT m FROM MovimentoFinanceiro m " +
                "JOIN FETCH m.encontro " +
                "WHERE m.encontro.id = :encontroId " +
                "ORDER BY m.dataMovimento DESC",
                MovimentoFinanceiro.class)
                .setParameter("encontroId", encontroId)
                .getResultList();
    }

    @Override
    public List<MovimentoFinanceiro> findByTipoAndEncontro(TipoMovimento tipo, Long encontroId) {
        return entityManager.createQuery(
                "SELECT m FROM MovimentoFinanceiro m " +
                "JOIN FETCH m.encontro " +
                "WHERE m.tipo = :tipo AND m.encontro.id = :encontroId " +
                "ORDER BY m.dataMovimento DESC",
                MovimentoFinanceiro.class)
                .setParameter("tipo", tipo)
                .setParameter("encontroId", encontroId)
                .getResultList();
    }

    @Override
    public boolean delete(Long id) {
        try {
            entityManager.createQuery("DELETE FROM MovimentoFinanceiro m WHERE m.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            entityManager.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public BigDecimal somarReceitasPorEncontro(Long encontroId) {
        BigDecimal resultado = entityManager.createQuery(
                "SELECT COALESCE(SUM(m.valor), 0) FROM MovimentoFinanceiro m " +
                "WHERE m.tipo = :tipo AND m.encontro.id = :encontroId",
                BigDecimal.class)
                .setParameter("tipo", TipoMovimento.RECEITA)
                .setParameter("encontroId", encontroId)
                .getSingleResult();
        return resultado != null ? resultado : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal somarDespesasPorEncontro(Long encontroId) {
        BigDecimal resultado = entityManager.createQuery(
                "SELECT COALESCE(SUM(m.valor), 0) FROM MovimentoFinanceiro m " +
                "WHERE m.tipo = :tipo AND m.encontro.id = :encontroId",
                BigDecimal.class)
                .setParameter("tipo", TipoMovimento.DESPESA)
                .setParameter("encontroId", encontroId)
                .getSingleResult();
        return resultado != null ? resultado : BigDecimal.ZERO;
    }

    @Override
    public long count() {
        return entityManager.createQuery("SELECT COUNT(m) FROM MovimentoFinanceiro m", Long.class)
                .getSingleResult();
    }
}
