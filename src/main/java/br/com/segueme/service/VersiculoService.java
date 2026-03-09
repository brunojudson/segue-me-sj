package br.com.segueme.service;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.Versiculo;

@ApplicationScoped
public class VersiculoService {

	@PersistenceContext
	private EntityManager em;

	public Versiculo buscarAleatorio() {
		try {
			return em.createQuery("SELECT v FROM Versiculo v ORDER BY FUNCTION('RANDOM')", Versiculo.class)
					.setMaxResults(1)
					.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;
		}
	}
}
