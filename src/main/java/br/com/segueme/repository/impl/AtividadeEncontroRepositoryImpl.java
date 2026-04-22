package br.com.segueme.repository.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.AtividadeEncontro;
import br.com.segueme.entity.Encontro;
import br.com.segueme.repository.AtividadeEncontroRepository;

@Stateless
public class AtividadeEncontroRepositoryImpl implements AtividadeEncontroRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public AtividadeEncontro findById(Long id) {
        return entityManager.createQuery(
                "SELECT a FROM AtividadeEncontro a " +
                "LEFT JOIN FETCH a.encontro " +
                "LEFT JOIN FETCH a.palestra p " +
                "LEFT JOIN FETCH p.palestrantes pal " +
                "LEFT JOIN FETCH pal.pessoaIndividual " +
                "LEFT JOIN FETCH pal.casal c " +
                "LEFT JOIN FETCH c.pessoa1 " +
                "LEFT JOIN FETCH c.pessoa2 " +
                "WHERE a.id = :id",
                AtividadeEncontro.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<AtividadeEncontro> findByEncontro(Encontro encontro) {
        return entityManager.createQuery(
                "SELECT DISTINCT a FROM AtividadeEncontro a " +
                "LEFT JOIN FETCH a.encontro " +
                "LEFT JOIN FETCH a.palestra p " +
                "LEFT JOIN FETCH p.palestrantes pal " +
                "LEFT JOIN FETCH pal.pessoaIndividual " +
                "LEFT JOIN FETCH pal.casal c " +
                "LEFT JOIN FETCH c.pessoa1 " +
                "LEFT JOIN FETCH c.pessoa2 " +
                "WHERE a.encontro = :encontro " +
                "ORDER BY a.dataHoraInicio ASC, a.ordem ASC",
                AtividadeEncontro.class)
                .setParameter("encontro", encontro)
                .getResultList();
    }

    @Override
    public void save(AtividadeEncontro atividade) {
        entityManager.persist(atividade);
    }

    @Override
    public void update(AtividadeEncontro atividade) {
        entityManager.merge(atividade);
    }

    @Override
    public void delete(Long id) {
        AtividadeEncontro atividade = entityManager.find(AtividadeEncontro.class, id);
        if (atividade != null) {
            entityManager.remove(atividade);
        }
    }
}
