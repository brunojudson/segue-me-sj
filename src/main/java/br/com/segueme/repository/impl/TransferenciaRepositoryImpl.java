package br.com.segueme.repository.impl;

import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.Transferencia;
import br.com.segueme.enums.StatusTransferencia;
import br.com.segueme.repository.TransferenciaRepository;

@Stateless
public class TransferenciaRepositoryImpl implements TransferenciaRepository {

    @PersistenceContext(unitName = "seguemePU")
    private EntityManager entityManager;

    @Override
    public Transferencia save(Transferencia transferencia) {
        entityManager.persist(transferencia);
        return transferencia;
    }

    @Override
    public Transferencia update(Transferencia transferencia) {
        return entityManager.merge(transferencia);
    }

    @Override
    public Optional<Transferencia> findById(Long id) {
        try {
            Transferencia t = entityManager.createQuery(
                    "SELECT t FROM Transferencia t LEFT JOIN FETCH t.pessoa WHERE t.id = :id",
                    Transferencia.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(t);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Transferencia> findAll() {
        return entityManager.createQuery(
                "SELECT t FROM Transferencia t LEFT JOIN FETCH t.pessoa p ORDER BY t.dataSolicitacao DESC",
                Transferencia.class)
                .getResultList();
    }

    @Override
    public List<Transferencia> findByPessoa(Long pessoaId) {
        return entityManager.createQuery(
                "SELECT t FROM Transferencia t LEFT JOIN FETCH t.pessoa p " +
                "WHERE p.id = :pessoaId ORDER BY t.dataSolicitacao DESC",
                Transferencia.class)
                .setParameter("pessoaId", pessoaId)
                .getResultList();
    }

    @Override
    public List<Transferencia> findByTipo(StatusTransferencia tipo) {
        return entityManager.createQuery(
                "SELECT t FROM Transferencia t LEFT JOIN FETCH t.pessoa p " +
                "WHERE t.tipoTransferencia = :tipo ORDER BY t.dataSolicitacao DESC",
                Transferencia.class)
                .setParameter("tipo", tipo)
                .getResultList();
    }
}
