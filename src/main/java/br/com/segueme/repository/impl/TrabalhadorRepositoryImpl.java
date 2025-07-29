package br.com.segueme.repository.impl;

import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.Trabalhador;
import br.com.segueme.repository.TrabalhadorRepository;

@Stateless
public class TrabalhadorRepositoryImpl implements TrabalhadorRepository {

	@PersistenceContext(unitName = "seguemePU")
	private EntityManager entityManager;

	@Override
	public Trabalhador save(Trabalhador trabalhador) {
		entityManager.persist(trabalhador);
		return trabalhador;
	}

	@Override
	public Trabalhador update(Trabalhador trabalhador) {
		return entityManager.merge(trabalhador);
	}

	@Override
	public Optional<Trabalhador> findById(Long id) {
		try {
			Trabalhador trabalhador = entityManager.createQuery(
					"SELECT t FROM Trabalhador t " +
							"LEFT JOIN FETCH t.encontro e " +
							"LEFT JOIN FETCH t.equipe eq " +
							"LEFT JOIN FETCH eq.tipoEquipe " +
							"LEFT JOIN FETCH t.contribuicoes c " +
							"LEFT JOIN FETCH t.cargos cg " + // Adicione esta linha para carregar cargos
							"WHERE t.id = :id",
					Trabalhador.class)
					.setParameter("id", id)
					.getSingleResult();
			return Optional.of(trabalhador);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	@Override
	public List<Trabalhador> findAll() {
		return entityManager.createQuery(
				"SELECT t FROM Trabalhador t " +
						"LEFT JOIN FETCH t.pessoa " +
						"LEFT JOIN FETCH t.equipe e " +
						"LEFT JOIN FETCH e.tipoEquipe " +
						"LEFT JOIN FETCH t.encontro " +
						"LEFT JOIN FETCH t.encontrista en " +
						"LEFT JOIN FETCH en.pessoa " +
						"LEFT JOIN FETCH en.encontro " +
						"LEFT JOIN FETCH t.contribuicoes " +
						"ORDER BY t.ativo DESC, t.pessoa.nome",
				Trabalhador.class) // Inclua outras associações
				.getResultList();
	}

	@Override
	public List<Trabalhador> findAllAtivos() {
		return entityManager.createQuery(
				"SELECT DISTINCT t FROM Trabalhador t " +
						"LEFT JOIN FETCH t.pessoa " +
						"LEFT JOIN FETCH t.equipe e " +
						"LEFT JOIN FETCH e.tipoEquipe " +
						"LEFT JOIN FETCH t.encontro " +
						"LEFT JOIN FETCH t.encontrista en " +
						"LEFT JOIN FETCH en.pessoa " +
						"LEFT JOIN FETCH en.encontro " +
						"LEFT JOIN FETCH t.contribuicoes " +
						"LEFT JOIN FETCH t.cargos " + // Adicione esta linha
						"WHERE t.ativo = true",
				Trabalhador.class)
				.getResultList();
	}

	@Override
	public List<Trabalhador> findAllDistinct() {
	    return entityManager.createQuery(
	            "SELECT DISTINCT t FROM Trabalhador t " +
	                    "LEFT JOIN FETCH t.pessoa p " +
	                    "LEFT JOIN FETCH t.equipe e " +
	                    "LEFT JOIN FETCH e.tipoEquipe " +
	                    "LEFT JOIN FETCH t.encontro " +
	                    "LEFT JOIN FETCH t.encontrista en " +
	                    "LEFT JOIN FETCH en.pessoa " +
	                    "LEFT JOIN FETCH t.contribuicoes " + // Carrega Contribuições
	                    "LEFT JOIN FETCH t.cargos " +        // Carrega Cargos
	                    "WHERE t.id IN (" +
	                    "    SELECT MIN(t2.id) FROM Trabalhador t2 GROUP BY t2.pessoa.id" +
	                    ") " +
	                    "ORDER BY t.pessoa.nome, t.id",
	            Trabalhador.class).getResultList();
	}

	@Override
	public List<Trabalhador> findByPessoa(Long pessoaId) {
		return entityManager.createQuery("SELECT t FROM Trabalhador t WHERE t.pessoa.id = :pessoaId", Trabalhador.class)
				.setParameter("pessoaId", pessoaId).getResultList();
	}

	@Override
	public List<Trabalhador> findByEquipe(Long equipeId) {
		return entityManager.createQuery("SELECT t FROM Trabalhador t WHERE t.equipe.id = :equipeId", Trabalhador.class)
				.setParameter("equipeId", equipeId).getResultList();
	}

	@Override
	public List<Trabalhador> findByEncontro(Long encontroId) {
		return entityManager
				.createQuery("SELECT t FROM Trabalhador t WHERE t.equipe.encontro.id = :encontroId", Trabalhador.class)
				.setParameter("encontroId", encontroId).getResultList();
	}

	@Override
	public List<Trabalhador> findCoordenadores() {
		return entityManager.createQuery("SELECT t FROM Trabalhador t WHERE t.coordenador = true", Trabalhador.class)
				.getResultList();
	}

	@Override
	public boolean delete(Long id) {
		Trabalhador trabalhador = entityManager.find(Trabalhador.class, id);
		if (trabalhador != null) {
			entityManager.remove(trabalhador);
			return true;
		}
		return false;
	}

	public boolean hasContribuicoes(Long id) {
		Long count = entityManager
				.createQuery("SELECT COUNT(c) FROM Contribuicao c WHERE c.trabalhador.id = :trabalhadorId", Long.class)
				.setParameter("trabalhadorId", id).getSingleResult();

		return count > 0;
	}

	@Override
	public List<Trabalhador> findExEncontristas() {
		return entityManager.createQuery("SELECT t FROM Trabalhador t WHERE t.exEncontrista = true", Trabalhador.class)
				.getResultList();
	}

	@Override
	public Optional<Trabalhador> findByPessoaEquipeEncontro(Long pessoaId, Long equipeId, Long encontroId) {
		try {
			Trabalhador trabalhador = entityManager.createQuery(
					"SELECT t FROM Trabalhador t WHERE t.pessoa.id = :pessoaId AND t.equipe.id = :equipeId AND t.equipe.encontro.id = :encontroId",
					Trabalhador.class).setParameter("pessoaId", pessoaId).setParameter("equipeId", equipeId)
					.setParameter("encontroId", encontroId).getSingleResult();
			return Optional.of(trabalhador);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Trabalhador> findByPessoaEncontro(Long pessoaId, Long encontroId) {
		try {
			Trabalhador trabalhador = entityManager.createQuery(
					"SELECT t FROM Trabalhador t WHERE t.pessoa.id = :pessoaId AND t.encontro.id = :encontroId",
					Trabalhador.class).setParameter("pessoaId", pessoaId).setParameter("encontroId", encontroId).getSingleResult();
			return Optional.of(trabalhador);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}


	@Override
	public boolean hasAssociations(Long id) {
		Long count = entityManager
				.createQuery("SELECT COUNT(d) FROM Dirigente d WHERE d.trabalhador.id = :trabalhadorId", Long.class)
				.setParameter("trabalhadorId", id).getSingleResult();

		return count > 0;
	}
}
