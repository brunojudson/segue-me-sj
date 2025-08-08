package br.com.segueme.repository.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.Dirigente;
import br.com.segueme.repository.DirigenteRepository;

@Stateless
public class DirigenteRepositoryImpl implements DirigenteRepository {

    @PersistenceContext(unitName = "seguemePU")
    private EntityManager entityManager;

    @Override
    public Dirigente save(Dirigente dirigente) {
        entityManager.persist(dirigente);
        return dirigente;
    }

    @Override
    public Dirigente update(Dirigente dirigente) {
        return entityManager.merge(dirigente);
    }

    @Override
    public Optional<Dirigente> findById(Long id) {
        try {
            Dirigente dirigente = entityManager.createQuery(
                    "SELECT d FROM Dirigente d " +
                    "LEFT JOIN FETCH d.trabalhador t " + // Carrega a associação Trabalhador
                    "LEFT JOIN FETCH t.equipe e " + // Carrega a associação Equipe
                    "LEFT JOIN FETCH e.tipoEquipe " + // Carrega TipoEquipe
                    "LEFT JOIN FETCH d.pasta p " + // Carrega a associação Pasta
                    "WHERE d.id = :id", Dirigente.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(dirigente);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Dirigente> findAll() {
        return entityManager.createQuery(
                "SELECT d FROM Dirigente d " +
                        "JOIN FETCH d.trabalhador t " + // Carrega Trabalhador
                        "JOIN FETCH t.pessoa p " + // Carrega Pessoa do Trabalhador
                        "JOIN FETCH t.equipe e " + // Carrega Equipe do Trabalhador
                        "LEFT JOIN FETCH e.tipoEquipe te " + // Carrega TipoEquipe da Equipe
                        "LEFT JOIN FETCH e.encontro en " + // Carrega Encontro da Equipe
                        "JOIN FETCH d.pasta p " + // Carrega Pasta do Dirigente
                        "LEFT JOIN FETCH p.equipe pe " + // Carrega Equipe da Pasta
                        "ORDER BY d.ativo DESC",
                Dirigente.class)
                .getResultList();
    }

    @Override
    public List<Dirigente> findByTrabalhador(Long trabalhadorId) {
        return entityManager.createQuery(
                "SELECT d FROM Dirigente d WHERE d.trabalhador.id = :trabalhadorId",
                Dirigente.class)
                .setParameter("trabalhadorId", trabalhadorId)
                .getResultList();
    }

    @Override
    public List<Dirigente> findByPasta(Long pastaId) {
        return entityManager.createQuery(
                "SELECT d FROM Dirigente d WHERE d.pasta.id = :pastaId",
                Dirigente.class)
                .setParameter("pastaId", pastaId)
                .getResultList();
    }

    public List<Dirigente> findDirigentesAtivos() {
        LocalDate hoje = LocalDate.now();
        return entityManager.createQuery(
                "SELECT d FROM Dirigente d WHERE d.dataInicio <= :hoje AND d.dataFim >= :hoje AND d.ativo = true",
                Dirigente.class)
                .setParameter("hoje", hoje)
                .getResultList();
    }

    @Override
    public boolean delete(Long id) {
        System.out.println("Tentando excluir Dirigente com ID: " + id);
        Dirigente dirigente = entityManager.find(Dirigente.class, id);
        if (dirigente != null) {
            System.out.println("Dirigente encontrado: " + dirigente);
            entityManager.remove(dirigente);
            System.out.println("Dirigente removido com sucesso.");
            return true;
        } else {
            System.out.println("Dirigente não encontrado.");
        }
        return false;
    }

    public boolean verificarMandatoAtivo(Long trabalhadorId, LocalDate dataReferencia) {
        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }

        Long count = entityManager.createQuery(
                "SELECT COUNT(d) FROM Dirigente d " +
                        "WHERE d.trabalhador.id = :trabalhadorId " +
                        "AND d.dataInicio <= :dataReferencia " +
                        "AND d.dataFim >= :dataReferencia " +
                        "AND d.ativo = true",
                Long.class)
                .setParameter("trabalhadorId", trabalhadorId)
                .setParameter("dataReferencia", dataReferencia)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public List<Dirigente> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return entityManager.createQuery(
                "SELECT d FROM Dirigente d WHERE " +
                        "d.dataInicio >= :dataInicio AND d.dataFim <= :dataFim",
                Dirigente.class)
                .setParameter("dataInicio", dataInicio)
                .setParameter("dataFim", dataFim)
                .getResultList();
    }

    @Override
    public List<Dirigente> findVigentes() {
        LocalDate hoje = LocalDate.now();
        return entityManager.createQuery(
                "SELECT d FROM Dirigente d WHERE " +
                        "d.dataInicio <= :hoje AND d.dataFim >= :hoje AND d.ativo = true",
                Dirigente.class)
                .setParameter("hoje", hoje)
                .getResultList();
    }

    @Override
    public Optional<Dirigente> findByTrabalhadorAndPasta(Long trabalhadorId, Long pastaId) {
        try {
            Dirigente dirigente = entityManager.createQuery(
                    "SELECT d FROM Dirigente d WHERE " +
                            "d.trabalhador.id = :trabalhadorId AND d.pasta.id = :pastaId",
                    Dirigente.class)
                    .setParameter("trabalhadorId", trabalhadorId)
                    .setParameter("pastaId", pastaId)
                    .getSingleResult();
            return Optional.of(dirigente);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

}
