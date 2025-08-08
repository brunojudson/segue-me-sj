package br.com.segueme.repository.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Palestra;
import br.com.segueme.repository.PalestraRepository;

@Stateless
public class PalestraRepositoryImpl implements PalestraRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
    public Palestra findById(Long id) {
        TypedQuery<Palestra> query = entityManager.createQuery(
            "SELECT DISTINCT p FROM Palestra p " + // Adicionado DISTINCT aqui também
            "LEFT JOIN FETCH p.encontro " +
            "LEFT JOIN FETCH p.palestrantes pal " +
            "LEFT JOIN FETCH pal.pessoaIndividual " +
            "LEFT JOIN FETCH pal.casal c " +
            "LEFT JOIN FETCH c.pessoa1 " +
            "LEFT JOIN FETCH c.pessoa2 " +
            "WHERE p.id = :id", Palestra.class);
        query.setParameter("id", id);
        List<Palestra> result = query.getResultList(); // Use getResultList para evitar NoResultException se a query puder retornar mais de um por causa dos FETCH em coleções
        return result.isEmpty() ? null : result.get(0); // Retorna o primeiro ou null
    }
	
	public List<Palestra> findAll() {
	    return entityManager.createQuery(
	        "SELECT DISTINCT p FROM Palestra p " +
	        "LEFT JOIN FETCH p.encontro e " +
	        "LEFT JOIN FETCH p.palestrantes pal " +
	        "LEFT JOIN FETCH pal.pessoaIndividual " +  // Se usar palestrantes individuais
	        "LEFT JOIN FETCH pal.casal c " +           // Se usar casais como palestrantes
	        "LEFT JOIN FETCH c.pessoa1 " +             // Se precisar da pessoa1 do casal
	        "LEFT JOIN FETCH c.pessoa2 " +             // Se precisar da pessoa2 do casal
	        "ORDER BY p.titulo", 
	        Palestra.class).getResultList();
	}

	@Override
	public List<Palestra> findByEncontro(Encontro encontro) {
		TypedQuery<Palestra> query = entityManager
				.createQuery("SELECT p FROM Palestra p WHERE p.encontro = :encontro ORDER BY p.titulo", Palestra.class);
		query.setParameter("encontro", encontro);
		return query.getResultList();
	}

	@Override
	@Transactional
	public void save(Palestra palestra) {
		entityManager.merge(palestra);
	}

	@Override
	@Transactional
	public void update(Palestra palestra) {
		entityManager.merge(palestra);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		Palestra palestra = findById(id);
		if (palestra != null) {
			// Remover associações ManyToMany antes de deletar
			palestra.getPalestrantes().forEach(palestrante -> palestrante.getPalestras().remove(palestra));
			palestra.getPalestrantes().clear();
			entityManager.remove(palestra);
		}
	}

	@Override
	public boolean hasAssociations(Long id) {
		// Verifica se a palestra está associada a algum palestrante
		// A lógica de verificação de associações pode ser mais complexa dependendo das
		// regras de negócio
		// Por enquanto, consideramos que a associação principal é com Palestrante
		Palestra palestra = findById(id);
		return palestra != null && !palestra.getPalestrantes().isEmpty();
	}
}
