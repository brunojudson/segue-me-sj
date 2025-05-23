package br.com.segueme.repository.impl;

import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.Pessoa;
import br.com.segueme.repository.PessoaRepository;

@Stateless
public class PessoaRepositoryImpl implements PessoaRepository {

    @PersistenceContext(unitName = "seguemePU")
    private EntityManager entityManager;

    @Override
    public Pessoa save(Pessoa pessoa) {
        entityManager.persist(pessoa);
        return pessoa;
    }

    @Override
    public Pessoa update(Pessoa pessoa) {
        return entityManager.merge(pessoa);
    }

    @Override
    public Optional<Pessoa> findById(Long id) {
        Pessoa pessoa = entityManager.find(Pessoa.class, id);
        return Optional.ofNullable(pessoa);
    }

    @Override
    public List<Pessoa> findAll() {
        return entityManager.createQuery("SELECT p FROM Pessoa p ORDER BY p.nome", Pessoa.class)
                .getResultList();
    }

    @Override
    public List<Pessoa> findAllExcludingActiveEncontristas() {
        return entityManager.createQuery(
                "SELECT p FROM Pessoa p WHERE p.id NOT IN " +
                "(SELECT e.pessoa.id FROM Encontrista e WHERE e.ativo = true) " +
                "ORDER BY p.nome", Pessoa.class)
                .getResultList();
    }
    @Override
    public List<Pessoa> findAllExcludingEncontristas() {
        return entityManager.createQuery(
                "SELECT p FROM Pessoa p WHERE p.id NOT IN " +
                "(SELECT e.pessoa.id FROM Encontrista e) " +
                "ORDER BY p.nome", Pessoa.class)
                .getResultList();
    }

    @Override
    public List<Pessoa> findByNome(String nome) {
        return entityManager.createQuery("SELECT p FROM Pessoa p WHERE LOWER(p.nome) LIKE LOWER(:nome)", Pessoa.class)
                .setParameter("nome", "%" + nome + "%")
                .getResultList();
    }

    @Override
    public Optional<Pessoa> findByCpf(String cpf) {
        try {
            Pessoa pessoa = entityManager.createQuery("SELECT p FROM Pessoa p WHERE p.cpf = :cpf", Pessoa.class)
                    .setParameter("cpf", cpf)
                    .getSingleResult();
            return Optional.of(pessoa);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(Long id) {
        Pessoa pessoa = entityManager.find(Pessoa.class, id);
        if (pessoa != null) {
            entityManager.remove(pessoa);
            return true;
        }
        return false;
    }

    @Override
    public boolean deactivate(Long id) {
        Pessoa pessoa = entityManager.find(Pessoa.class, id);
        if (pessoa != null) {
            pessoa.setAtivo(false);
            entityManager.merge(pessoa);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasAssociations(Long id) {
        // Verifica se a pessoa está associada a algum encontrista
        Long encontristaCount = entityManager.createQuery(
                "SELECT COUNT(e) FROM Encontrista e WHERE e.pessoa.id = :pessoaId", Long.class)
                .setParameter("pessoaId", id)
                .getSingleResult();

        if (encontristaCount > 0) {
            return true;
        }

        // Verifica se a pessoa está associada a algum trabalhador
        Long trabalhadorCount = entityManager.createQuery(
                "SELECT COUNT(t) FROM Trabalhador t WHERE t.pessoa.id = :pessoaId", Long.class)
                .setParameter("pessoaId", id)
                .getSingleResult();

        return trabalhadorCount > 0;
    }
}
