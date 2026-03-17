package br.com.segueme.repository.impl;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.Auditoria;
import br.com.segueme.repository.AuditoriaRepository;

@Stateless
public class AuditoriaRepositoryImpl implements AuditoriaRepository {

    @PersistenceContext
    private EntityManager entityManager;
    
    public void salvar(Auditoria auditoria) {
        entityManager.persist(auditoria);
    }

}
