package br.com.segueme.repository.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import br.com.segueme.entity.Casal;
import br.com.segueme.entity.Palestrante;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.enums.TipoPalestrante;
import br.com.segueme.repository.PalestranteRepository;

@Stateless
public class PalestranteRepositoryImpl implements PalestranteRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Palestrante findById(Long id) {
        TypedQuery<Palestrante> query = entityManager.createQuery(
            "SELECT DISTINCT p FROM Palestrante p " +
            "LEFT JOIN FETCH p.pessoaIndividual " +
            "LEFT JOIN FETCH p.casal " +
            "LEFT JOIN FETCH p.membrosGrupo " +
            "LEFT JOIN FETCH p.palestras palestras " +
            "LEFT JOIN FETCH palestras.palestrantes " +
            "WHERE p.id = :id", Palestrante.class);
        query.setParameter("id", id);
        List<Palestrante> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<Palestrante> findAll() {
        // Utiliza JOIN FETCH para evitar LazyInitializationException e N+1
        // Usa DISTINCT para evitar duplicatas devido aos JOINs
        TypedQuery<Palestrante> query = entityManager.createQuery(
            "SELECT DISTINCT p FROM Palestrante p " +
            "LEFT JOIN FETCH p.pessoaIndividual " +
            "LEFT JOIN FETCH p.casal " +
            "LEFT JOIN FETCH p.membrosGrupo " +
            "LEFT JOIN FETCH p.palestras palestras " +
            "LEFT JOIN FETCH palestras.palestrantes palestrante2 " +
            "LEFT JOIN FETCH palestrante2.pessoaIndividual " +
            "LEFT JOIN FETCH palestrante2.casal " +
            "LEFT JOIN FETCH palestrante2.membrosGrupo " +
            "ORDER BY p.id", Palestrante.class);
        return query.getResultList();
    }

    @Override
    public List<Palestrante> findByPessoa(Pessoa pessoa) {
        TypedQuery<Palestrante> query = entityManager.createQuery(
            "SELECT p FROM Palestrante p WHERE p.tipoPalestrante = :tipoIndividual AND p.pessoaIndividual = :pessoa " +
            "OR :pessoa MEMBER OF p.membrosGrupo", Palestrante.class);
        query.setParameter("tipoIndividual", TipoPalestrante.INDIVIDUAL);
        query.setParameter("pessoa", pessoa);
        return query.getResultList();
    }

    @Override
    public List<Palestrante> findByCasal(Casal casal) {
        TypedQuery<Palestrante> query = entityManager.createQuery(
            "SELECT p FROM Palestrante p WHERE p.tipoPalestrante = :tipoCasal AND p.casal = :casal", Palestrante.class);
        query.setParameter("tipoCasal", TipoPalestrante.CASAL);
        query.setParameter("casal", casal);
        return query.getResultList();
    }

    @Override
    @Transactional
    public void save(Palestrante palestrante) {
        entityManager.persist(palestrante);
    }

    @Override
    @Transactional
    public void update(Palestrante palestrante) {
        entityManager.merge(palestrante);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Palestrante palestrante = findById(id);
        if (palestrante != null) {
            // Remover associações ManyToMany com Palestra
            palestrante.getPalestras().forEach(palestra -> palestra.getPalestrantes().remove(palestrante));
            palestrante.getPalestras().clear();
            
			/*
			 * // Remover associações ManyToMany com Pessoa (para tipo GRUPO) if
			 * (palestrante.getTipoPalestrante() == TipoPalestrante.GRUPO) {
			 * palestrante.getMembrosGrupo().clear(); }
			 */
            entityManager.remove(palestrante);
        }
    }
    
    @Override
    public boolean hasAssociations(Long id) {
        // Verifica se o palestrante está associado a alguma palestra
        Palestrante palestrante = findById(id);
        return palestrante != null && !palestrante.getPalestras().isEmpty();
    }
    
    @Override
    public boolean existsByCasalId(Long casalId) {
        if (casalId == null) {
            return false;
        }
        Long count = entityManager.createQuery(
                "SELECT COUNT(p) FROM Palestrante p WHERE p.casal.id = :casalId", Long.class)
            .setParameter("casalId", casalId)
            .getSingleResult();
        return count > 0;
    }
    
    @Override
    public boolean existsByPessoaIndividualId(Long pessoaIndividualId) {
        if (pessoaIndividualId == null) {
            return false;
        }
        Long count = entityManager.createQuery(
                "SELECT COUNT(p) FROM Palestrante p WHERE p.pessoaIndividual.id = :pessoaIndividualId", Long.class)
            .setParameter("pessoaIndividualId", pessoaIndividualId)
            .getSingleResult();
        return count > 0;
    }
}

