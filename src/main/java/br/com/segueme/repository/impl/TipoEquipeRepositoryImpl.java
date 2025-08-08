package br.com.segueme.repository.impl;

import br.com.segueme.entity.TipoEquipe;
import br.com.segueme.repository.TipoEquipeRepository;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Stateless
public class TipoEquipeRepositoryImpl implements TipoEquipeRepository {
    
    @PersistenceContext(unitName = "seguemePU")
    private EntityManager entityManager;
    
    @Override
    public TipoEquipe save(TipoEquipe tipoEquipe) {
        entityManager.persist(tipoEquipe);
        return tipoEquipe;
    }
    
    @Override
    public TipoEquipe update(TipoEquipe tipoEquipe) {
        return entityManager.merge(tipoEquipe);
    }
    
    @Override
    public Optional<TipoEquipe> findById(Long id) {
        TipoEquipe tipoEquipe = entityManager.find(TipoEquipe.class, id);
        return Optional.ofNullable(tipoEquipe);
    }
    
    @Override
    public List<TipoEquipe> findAll() {
        return entityManager.createQuery("SELECT t FROM TipoEquipe t", TipoEquipe.class)
                .getResultList();
    }
    
    @Override
    public List<TipoEquipe> findByNome(String nome) {
        return entityManager.createQuery(
                "SELECT t FROM TipoEquipe t WHERE LOWER(t.nome) LIKE LOWER(:nome)", 
                TipoEquipe.class)
                .setParameter("nome", "%" + nome + "%")
                .getResultList();
    }
    
    public List<TipoEquipe> findByEhDirigente(Boolean ehDirigente) {
        return entityManager.createQuery(
                "SELECT t FROM TipoEquipe t WHERE t.ehDirigente = :ehDirigente", 
                TipoEquipe.class)
                .setParameter("ehDirigente", ehDirigente)
                .getResultList();
    }
    
    @Override
    public boolean delete(Long id) {
        TipoEquipe tipoEquipe = entityManager.find(TipoEquipe.class, id);
        if (tipoEquipe != null) {
            entityManager.remove(tipoEquipe);
            return true;
        }
        return false;
    }
    
    public boolean hasEquipes(Long id) {
        Long count = entityManager.createQuery(
                "SELECT COUNT(e) FROM Equipe e WHERE e.tipoEquipe.id = :tipoEquipeId", Long.class)
                .setParameter("tipoEquipeId", id)
                .getSingleResult();
        
        return count > 0;
    }
    @Override
    public List<TipoEquipe> findDirigentes() {
        return entityManager.createQuery(
                "SELECT t FROM TipoEquipe t WHERE t.ehDirigente = true", 
                TipoEquipe.class)
                .getResultList();
    }

    @Override
    public boolean hasAssociations(Long id) {
        Long count = entityManager.createQuery(
                "SELECT COUNT(e) FROM Equipe e WHERE e.tipoEquipe.id = :tipoEquipeId", Long.class)
                .setParameter("tipoEquipeId", id)
                .getSingleResult();
        
        return count > 0;
    }

}
