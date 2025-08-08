package br.com.segueme.repository.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.Contribuicao;
import br.com.segueme.repository.ContribuicaoRepository;

@Stateless
public class ContribuicaoRepositoryImpl implements ContribuicaoRepository {

	@PersistenceContext(unitName = "seguemePU")
	private EntityManager entityManager;

	@Override
	public Contribuicao save(Contribuicao contribuicao) {
		entityManager.persist(contribuicao);
		return contribuicao;
	}

	@Override
	public Contribuicao update(Contribuicao contribuicao) {
		return entityManager.merge(contribuicao);
	}

	@Override
	public Optional<Contribuicao> findById(Long id) {
	    try {
	        Contribuicao contribuicao = entityManager.createQuery(
	                "SELECT c FROM Contribuicao c " +
	                "LEFT JOIN FETCH c.trabalhador t " + // Carrega Trabalhador
	                "LEFT JOIN FETCH t.equipe e " + // Carrega Equipe
	                "LEFT JOIN FETCH e.encontro en " + // Carrega Encontro
	                "WHERE c.id = :id", 
	                Contribuicao.class)
	                .setParameter("id", id)
	                .getSingleResult();
	        return Optional.of(contribuicao);
	    } catch (NoResultException e) {
	        return Optional.empty();
	    }
	}

	@Override
	public List<Contribuicao> findAll() {
	    return entityManager.createQuery(
	            "SELECT c FROM Contribuicao c " +
	                    "JOIN FETCH c.trabalhador t " + // Carrega Trabalhador
	                    "LEFT JOIN FETCH t.pessoa p " + // Carrega Pessoa do Trabalhador
	                    "LEFT JOIN FETCH t.equipe e " + // Carrega Equipe
	                    "LEFT JOIN FETCH e.encontro en " + // Carrega Encontro
	                    "ORDER BY c.dataPagamento DESC, t.pessoa.nome ",
	            Contribuicao.class)
	            .getResultList();
	}

	@Override
	public List<Contribuicao> findByTrabalhador(Long trabalhadorId) {
		return entityManager.createQuery(
				"SELECT c FROM Contribuicao c WHERE c.trabalhador.id = :trabalhadorId ORDER BY c.dataPagamento DESC",
				Contribuicao.class).setParameter("trabalhadorId", trabalhadorId).getResultList();
	}

	public List<Contribuicao> findByEncontro(Long encontroId) {
		return entityManager.createQuery(
				"SELECT c FROM Contribuicao c WHERE c.trabalhador.equipe.encontro.id = :encontroId ORDER BY c.dataPagamento DESC",
				Contribuicao.class).setParameter("encontroId", encontroId).getResultList();
	}

	@Override
	public List<Contribuicao> findByPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
		return entityManager.createQuery(
				"SELECT c FROM Contribuicao c WHERE c.dataPagamento BETWEEN :dataInicio AND :dataFim ORDER BY c.dataPagamento DESC",
				Contribuicao.class).setParameter("dataInicio", dataInicio).setParameter("dataFim", dataFim)
				.getResultList();
	}

	public BigDecimal getTotalContribuicoesByEncontro(Long encontroId) {
		return entityManager.createQuery(
				"SELECT COALESCE(SUM(c.valor), 0) FROM Contribuicao c WHERE c.trabalhador.equipe.encontro.id = :encontroId",
				BigDecimal.class).setParameter("encontroId", encontroId).getSingleResult();
	}

	@Override
	public boolean delete(Long id) {
		Contribuicao contribuicao = entityManager.find(Contribuicao.class, id);
		if (contribuicao != null) {
			entityManager.remove(contribuicao);
			return true;
		}
		return false;
	}

	@Override
	public List<Contribuicao> findByFormaPagamento(String formaPagamento) {
		return entityManager.createQuery(
				"SELECT c FROM Contribuicao c WHERE c.formaPagamento = :formaPagamento ORDER BY c.dataPagamento DESC",
				Contribuicao.class).setParameter("formaPagamento", formaPagamento).getResultList();
	}

	@Override
	public List<Contribuicao> findByValor(BigDecimal valorMinimo, BigDecimal valorMaximo) {
		return entityManager.createQuery(
				"SELECT c FROM Contribuicao c WHERE c.valor BETWEEN :valorMinimo AND :valorMaximo ORDER BY c.dataPagamento DESC",
				Contribuicao.class).setParameter("valorMinimo", valorMinimo).setParameter("valorMaximo", valorMaximo)
				.getResultList();
	}

	@Override
	public BigDecimal calcularTotalPorTrabalhador(Long trabalhadorId) {
		return entityManager.createQuery(
				"SELECT COALESCE(SUM(c.valor), 0) FROM Contribuicao c WHERE c.trabalhador.id = :trabalhadorId",
				BigDecimal.class).setParameter("trabalhadorId", trabalhadorId).getSingleResult();
	}

	@Override
	public BigDecimal calcularTotalGeral() {
		return entityManager.createQuery(
				"SELECT COALESCE(SUM(c.valor), 0) FROM Contribuicao c", BigDecimal.class)
				.getSingleResult();
	}
}
