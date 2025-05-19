package br.com.segueme.repository.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.Pasta;
import br.com.segueme.repository.PastaRepository;

@Stateless
public class PastaRepositoryImpl implements PastaRepository {

	@PersistenceContext(unitName = "seguemePU")
	private EntityManager entityManager;

	@Override
	public Pasta save(Pasta pasta) {
		entityManager.persist(pasta);
		return pasta;
	}

	@Override
	public Pasta update(Pasta pasta) {
		return entityManager.merge(pasta);
	}

	@Override
	public Optional<Pasta> findById(Long id) {
		try {
			Pasta pasta = entityManager.createQuery("SELECT p FROM Pasta p " + "LEFT JOIN FETCH p.dirigentes d " + // Carrega
																													// a
																													// coleção
																													// dirigentes
					"WHERE p.id = :id", Pasta.class).setParameter("id", id).getSingleResult();
			return Optional.of(pasta);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	@Override
	public List<Pasta> findAll() {
		return entityManager.createQuery("SELECT p FROM Pasta p " + "JOIN FETCH p.equipe e " + // Carrega a associação
																								// equipe
				"JOIN FETCH e.tipoEquipe t " + // Carrega a associação tipoEquipe
				"ORDER BY p.ativo DESC", Pasta.class).getResultList();
	}

	@Override
	public List<Pasta> findByNome(String nome) {
		return entityManager.createQuery("SELECT p FROM Pasta p WHERE LOWER(p.nome) LIKE LOWER(:nome)", Pasta.class)
				.setParameter("nome", "%" + nome + "%").getResultList();
	}

	@Override
	public List<Pasta> findByEquipe(Long equipeId) {
		return entityManager.createQuery("SELECT p FROM Pasta p WHERE p.equipe.id = :equipeId", Pasta.class)
				.setParameter("equipeId", equipeId).getResultList();
	}

	@Override
	public boolean delete(Long id) {
		Pasta pasta = entityManager.find(Pasta.class, id);
		if (pasta != null) {
			entityManager.remove(pasta);
			return true;
		}
		return false;
	}

	public boolean hasDirigentes(Long id) {
		Long count = entityManager
				.createQuery("SELECT COUNT(d) FROM Dirigente d WHERE d.pasta.id = :pastaId", Long.class)
				.setParameter("pastaId", id).getSingleResult();

		return count > 0;
	}

	@Override
	public List<Pasta> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
		return entityManager
				.createQuery("SELECT p FROM Pasta p WHERE " + "p.dataInicio >= :dataInicio AND p.dataFim <= :dataFim",
						Pasta.class)
				.setParameter("dataInicio", dataInicio).setParameter("dataFim", dataFim).getResultList();
	}

	@Override
	public List<Pasta> findVigentes() {
		return entityManager.createQuery("SELECT p FROM Pasta p WHERE p.ativo = true", Pasta.class).getResultList();
	}

	@Override
	public boolean hasAssociations(Long id) {
		Long count = entityManager
				.createQuery("SELECT COUNT(d) FROM Dirigente d WHERE d.pasta.id = :pastaId", Long.class)
				.setParameter("pastaId", id).getSingleResult();

		return count > 0;
	}

}
