package br.com.segueme.repository.impl;

import br.com.segueme.entity.Casal;
import br.com.segueme.repository.CasalRepository;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Stateless
public class CasalRepositoryImpl implements CasalRepository {
    
    @PersistenceContext(unitName = "seguemePU")
    private EntityManager entityManager;
    
    @Override
    public Casal save(Casal casal) {
        entityManager.persist(casal);
        return casal;
    }
    
    @Override
    public Casal update(Casal casal) {
        return entityManager.merge(casal);
    }
    
    @Override
    public Optional<Casal> findById(Long id) {
        Casal casal = entityManager.find(Casal.class, id);
        return Optional.ofNullable(casal);
    }
    
    @Override
    public List<Casal> findAll() {
        return entityManager.createQuery(
                "SELECT c FROM Casal c " +
                        "JOIN FETCH c.pessoa1 p1 " + // Carrega Pessoa1
                        "JOIN FETCH c.pessoa2 p2 " + // Carrega Pessoa2
                        "ORDER BY c.id",
                Casal.class)
                .getResultList();
    }
    
    public List<Casal> findByNome(String nome) {
        return entityManager.createQuery(
                "SELECT c FROM Casal c WHERE " +
                "LOWER(c.pessoa1.nome) LIKE LOWER(:nome) OR " +
                "LOWER(c.pessoa2.nome) LIKE LOWER(:nome)", 
                Casal.class)
                .setParameter("nome", "%" + nome + "%")
                .getResultList();
    }
    
    @Override
    public boolean delete(Long id) {
        Casal casal = entityManager.find(Casal.class, id);
        if (casal != null) {
            entityManager.remove(casal);
            return true;
        }
        return false;
    }
    
    public List<Casal> findCasaisTrabalhadores() {
        return entityManager.createQuery(
                "SELECT DISTINCT c FROM Casal c " +
                "JOIN Trabalhador t1 ON t1.pessoa.id = c.pessoa1.id " +
                "JOIN Trabalhador t2 ON t2.pessoa.id = c.pessoa2.id", 
                Casal.class)
                .getResultList();
    }

    @Override
    public List<Casal> findByPessoa(Long pessoaId) {
        return entityManager.createQuery(
                "SELECT c FROM Casal c WHERE " +
                "c.pessoa1.id = :pessoaId OR c.pessoa2.id = :pessoaId", 
                Casal.class)
                .setParameter("pessoaId", pessoaId)
                .getResultList();
    }

    @Override
    public Optional<Casal> findByPessoas(Long pessoa1Id, Long pessoa2Id) {
        try {
            Casal casal = entityManager.createQuery(
                    "SELECT c FROM Casal c WHERE " +
                    "(c.pessoa1.id = :pessoa1Id AND c.pessoa2.id = :pessoa2Id) OR " +
                    "(c.pessoa1.id = :pessoa2Id AND c.pessoa2.id = :pessoa1Id)", 
                    Casal.class)
                    .setParameter("pessoa1Id", pessoa1Id)
                    .setParameter("pessoa2Id", pessoa2Id)
                    .getSingleResult();
            return Optional.of(casal);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

}
