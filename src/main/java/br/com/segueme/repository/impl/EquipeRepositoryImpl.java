package br.com.segueme.repository.impl;

import br.com.segueme.entity.Equipe;
import br.com.segueme.repository.EquipeRepository;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Stateless
public class EquipeRepositoryImpl implements EquipeRepository {

    @PersistenceContext(unitName = "seguemePU")
    private EntityManager entityManager;

    @Override
    public Equipe save(Equipe equipe) {
        entityManager.persist(equipe);
        return equipe;
    }

    @Override
    public Equipe update(Equipe equipe) {
        return entityManager.merge(equipe);
    }

    @Override
    public Optional<Equipe> findById(Long id) {
        try {
            Equipe equipe = entityManager.createQuery(
                    "SELECT e FROM Equipe e " +
                            "LEFT JOIN FETCH e.trabalhadores t " + // Carrega a coleção trabalhadores
                            "LEFT JOIN FETCH e.tipoEquipe " + // Carrega a associação TipoEquipe
                            "LEFT JOIN FETCH e.encontro " + // Carrega a associação Encontro
                            "WHERE e.id = :id",
                    Equipe.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(equipe);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Equipe> findAll() {
        return entityManager.createQuery(
                "SELECT e FROM Equipe e " +
                        "JOIN FETCH e.tipoEquipe t " + // Carrega a associação TipoEquipe
                        "LEFT JOIN FETCH e.encontro en " + // Carrega a associação Encontro
                        "ORDER BY e.ativo DESC, e.nome ASC",
                Equipe.class)
                .getResultList();
    }

    @Override
    public List<Equipe> findByNome(String nome) {
        return entityManager.createQuery(
                "SELECT e FROM Equipe e WHERE LOWER(e.nome) LIKE LOWER(:nome)",
                Equipe.class)
                .setParameter("nome", "%" + nome + "%")
                .getResultList();
    }

    @Override
    public List<Equipe> findByTipoEquipe(Long tipoEquipeId) {
        return entityManager.createQuery(
                "SELECT e FROM Equipe e WHERE e.tipoEquipe.id = :tipoEquipeId",
                Equipe.class)
                .setParameter("tipoEquipeId", tipoEquipeId)
                .getResultList();
    }

    @Override
    public List<Equipe> findByEncontro(Long encontroId) {
        return entityManager.createQuery(
            "SELECT e FROM Equipe e LEFT JOIN FETCH e.tipoEquipe t LEFT JOIN FETCH e.encontro en WHERE e.encontro.id = :encontroId ORDER BY e.nome",
            Equipe.class)
            .setParameter("encontroId", encontroId)
            .getResultList();
    }

    @Override
    public boolean delete(Long id) {
        Equipe equipe = entityManager.find(Equipe.class, id);
        if (equipe != null) {
            entityManager.remove(equipe);

            return true;
        }
        return false;
    }

    public boolean hasTrabalhadores(Long id) {
        Long count = entityManager.createQuery(
                "SELECT COUNT(t) FROM Trabalhador t WHERE t.equipe.id = :equipeId", Long.class)
                .setParameter("equipeId", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public List<Equipe> findDirigentes() {
        return entityManager.createQuery(
                "SELECT e FROM Equipe e WHERE e.dirigente = true", Equipe.class)
                .getResultList();
    }

    @Override
    public List<Equipe> findAtivas() {
        return entityManager.createQuery(
                "SELECT e FROM Equipe e " +
                        "LEFT JOIN FETCH e.encontro " + // Carrega a associação Encontro
                        "LEFT JOIN FETCH e.tipoEquipe " + // Carrega a associação TipoEquipe
                        "WHERE e.ativo = true " +
                        "ORDER BY e.nome ASC", // Ordena pelo nome da equipe
                Equipe.class)
                .getResultList();
    }

    @Override
    public boolean deactivate(Long id) {
        Equipe equipe = entityManager.find(Equipe.class, id);
        if (equipe != null) {
            equipe.setAtivo(false);
            entityManager.merge(equipe);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasAssociations(Long id) {
        Long trabalhadoresCount = entityManager.createQuery(
            "SELECT COUNT(t) FROM Trabalhador t WHERE t.equipe.id = :equipeId", Long.class)
            .setParameter("equipeId", id)
            .getSingleResult();

        Long pastasCount = entityManager.createQuery(
            "SELECT COUNT(p) FROM Pasta p WHERE p.equipe.id = :equipeId", Long.class)
            .setParameter("equipeId", id)
            .getSingleResult();

        return (trabalhadoresCount != null && trabalhadoresCount > 0) || (pastasCount != null && pastasCount > 0);
    }

    @Override
    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM Equipe e", Long.class)
                .getSingleResult();
    }
}
