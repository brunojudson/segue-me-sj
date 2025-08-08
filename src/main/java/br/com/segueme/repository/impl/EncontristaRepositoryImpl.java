package br.com.segueme.repository.impl;

import br.com.segueme.entity.Encontrista;
import br.com.segueme.repository.EncontristaRepository;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Stateless
public class EncontristaRepositoryImpl implements EncontristaRepository {

    @PersistenceContext(unitName = "seguemePU")
    private EntityManager entityManager;

    @Override
    public Encontrista save(Encontrista encontrista) {
        entityManager.persist(encontrista);
        return encontrista;
    }

    @Override
    public Encontrista update(Encontrista encontrista) {
        return entityManager.merge(encontrista);
    }

    @Override
    public Optional<Encontrista> findById(Long id) {
        try {
            Encontrista encontrista = entityManager.createQuery(
                    "SELECT e FROM Encontrista e " +
                    "JOIN FETCH e.pessoa p " +
                    "LEFT JOIN FETCH p.sacramentos " +
                    "JOIN FETCH e.encontro " +
                    "LEFT JOIN FETCH e.trabalhador " +
                    "WHERE e.id = :id", Encontrista.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(encontrista);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

        @Override
    public List<Encontrista> findAll() {
        return entityManager.createQuery(
                "SELECT e FROM Encontrista e " +
                "JOIN FETCH e.pessoa " +
                "JOIN FETCH e.encontro " +
                "LEFT JOIN FETCH e.trabalhador order by e.ativo desc", // Adicione outras associações necessárias
                Encontrista.class)
                .getResultList();
    }

    @Override
    public List<Encontrista> findByEncontro(Long encontroId) {
        return entityManager.createQuery(
                "SELECT e FROM Encontrista e WHERE e.encontro.id = :encontroId",
                Encontrista.class)
                .setParameter("encontroId", encontroId)
                .getResultList();
    }

    @Override
    public List<Encontrista> findByPessoa(Long pessoaId) {
        return entityManager.createQuery(
                "SELECT e FROM Encontrista e WHERE e.pessoa.id = :pessoaId",
                Encontrista.class)
                .setParameter("pessoaId", pessoaId)
                .getResultList();
    }

    @Override
    public Optional<Encontrista> findByPessoaAndEncontro(Long pessoaId, Long encontroId) {
        try {
            Encontrista encontrista = entityManager.createQuery(
                    "SELECT e FROM Encontrista e WHERE e.pessoa.id = :pessoaId AND e.encontro.id = :encontroId",
                    Encontrista.class)
                    .setParameter("pessoaId", pessoaId)
                    .setParameter("encontroId", encontroId)
                    .getSingleResult();
            return Optional.of(encontrista);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Encontrista> findSemTrabalhador() {
        return entityManager.createQuery(
                "SELECT e FROM Encontrista e " +
                        "WHERE NOT EXISTS (SELECT t FROM Trabalhador t WHERE t.pessoa.id = e.pessoa.id)",
                Encontrista.class)
                .getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public boolean delete(Long id) {
        Encontrista encontrista = entityManager.find(Encontrista.class, id);
        if (encontrista != null) {
            // Remover associações com Trabalhador
            if (encontrista.getTrabalhador() != null) {
                encontrista.setTrabalhador(null);
                entityManager.merge(encontrista);
            }
            entityManager.remove(encontrista);
            entityManager.flush();
            return true;
        }

        return false;
    }

    public boolean deleteDirect(Long id) {
        try {
            entityManager.createQuery("DELETE FROM Encontrista e WHERE e.id = :id")
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
    public boolean hasAssociations(Long id) {
        // Verifica se o encontrista está associado a algum trabalhador
        Long count = entityManager.createQuery(
                "SELECT COUNT(t) FROM Trabalhador t JOIN Encontrista e ON t.pessoa.id = e.pessoa.id WHERE e.id = :encontristaId",
                Long.class)
                .setParameter("encontristaId", id)
                .getSingleResult();

        return count > 0;
    }

    // Este método não está na interface, mas pode ser útil para relatórios
    public List<Encontrista> findEncontristasQueSeTonaramTrabalhadores(Long encontroAnteriorId) {
        return entityManager.createQuery(
                "SELECT e FROM Encontrista e " +
                        "WHERE e.encontro.id = :encontroId " +
                        "AND EXISTS (SELECT t FROM Trabalhador t WHERE t.pessoa.id = e.pessoa.id)",
                Encontrista.class)
                .setParameter("encontroId", encontroAnteriorId)
                .getResultList();
    }
}
