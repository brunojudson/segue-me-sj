package br.com.segueme.repository.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.Encontro;
import br.com.segueme.repository.EncontroRepository;

@Stateless
public class EncontroRepositoryImpl implements EncontroRepository {

	@PersistenceContext(unitName = "seguemePU")
	private EntityManager entityManager;

	@Override
	public Encontro save(Encontro encontro) {
		entityManager.persist(encontro);
		return encontro;
	}

	@Override
	public Encontro update(Encontro encontro) {
		return entityManager.merge(encontro);
	}

	@Override
	public Optional<Encontro> findById(Long id) {
	    try {
	        Encontro encontro = entityManager
	                .createQuery("SELECT e FROM Encontro e " +
	                        "LEFT JOIN FETCH e.encontristas en " +
	                        "LEFT JOIN FETCH e.equipes eq " +
	                        "LEFT JOIN FETCH eq.tipoEquipe te " +
	                        "LEFT JOIN FETCH e.palestras p " +
	                        "LEFT JOIN FETCH p.palestrantes pa " +
	                        "LEFT JOIN FETCH pa.pessoaIndividual " +  // Carregar pessoa individual
	                        "LEFT JOIN FETCH pa.casal c " +           // Carregar casal
	                        "LEFT JOIN FETCH c.pessoa1 " +            // Carregar pessoa1 do casal
	                        "LEFT JOIN FETCH c.pessoa2 " +            // Carregar pessoa2 do casal
	                        "WHERE e.id = :id", Encontro.class)
	                .setParameter("id", id).getSingleResult();
	        return Optional.of(encontro);
	    } catch (NoResultException e) {
	        return Optional.empty();
	    }
	}

	@Override
	public List<Encontro> findAll() {
		return entityManager.createQuery("SELECT e FROM Encontro e ORDER BY e.dataInicio DESC", Encontro.class)
				.getResultList();
	}

	@Override
	public List<Encontro> findByNome(String nome) {
		return entityManager
				.createQuery("SELECT e FROM Encontro e WHERE LOWER(e.nome) LIKE LOWER(:nome)", Encontro.class)
				.setParameter("nome", "%" + nome + "%").getResultList();
	}

	@Override
	public List<Encontro> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
		return entityManager
				.createQuery("SELECT e FROM Encontro e WHERE e.dataInicio >= :dataInicio AND e.dataFim <= :dataFim",
						Encontro.class)
				.setParameter("dataInicio", dataInicio).setParameter("dataFim", dataFim).getResultList();
	}

	@Override
	public List<Encontro> findAtivos() {

		return entityManager.createQuery("SELECT e FROM Encontro e WHERE e.ativo = true", Encontro.class)
				.getResultList();
	}
	

    public List<Encontro> findUltimosDois() {
        return entityManager.createQuery(
                "SELECT e FROM Encontro e ORDER BY e.dataInicio DESC", Encontro.class)
                .setMaxResults(2)
                .getResultList();
    }

    
	@Override
	public boolean delete(Long id) {
		Encontro encontro = entityManager.find(Encontro.class, id);
		if (encontro != null) {
			entityManager.remove(encontro);
			return true;
		}
		return false;
	}

	@Override
	public boolean deactivate(Long id) {
		Encontro encontro = entityManager.find(Encontro.class, id);
		if (encontro != null) {
			encontro.setAtivo(false);
			entityManager.merge(encontro);
			return true;
		}
		return false;
	}

	@Override
	public boolean hasAssociations(Long id) {
		// Verifica se o encontro possui encontristas
		Long encontristasCount = entityManager
				.createQuery("SELECT COUNT(e) FROM Encontrista e WHERE e.encontro.id = :encontroId", Long.class)
				.setParameter("encontroId", id).getSingleResult();

		if (encontristasCount > 0) {
			return true;
		}

		// Verifica se o encontro possui equipes
		Long equipesCount = entityManager
				.createQuery("SELECT COUNT(e) FROM Equipe e WHERE e.encontro.id = :encontroId", Long.class)
				.setParameter("encontroId", id).getSingleResult();

		return equipesCount > 0;
	}
	
	
}
