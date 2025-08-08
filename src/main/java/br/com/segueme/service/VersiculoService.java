package br.com.segueme.service;

import java.util.List;
import java.util.Random;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.Versiculo;

@ApplicationScoped
public class VersiculoService {

	@PersistenceContext
	private EntityManager em;

	public Versiculo buscarAleatorio() {
		List<Versiculo> lista = em.createQuery("SELECT v FROM Versiculo v", Versiculo.class).getResultList();
		if (lista.isEmpty())
			return null;
		int idx = new Random().nextInt(lista.size());
		return lista.get(idx);
	}
}
