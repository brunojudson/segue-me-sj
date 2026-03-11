package br.com.segueme.repository.impl;

import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.VendaArtigo;
import br.com.segueme.repository.VendaArtigoRepository;

@Stateless
public class VendaArtigoRepositoryImpl implements VendaArtigoRepository {

    @PersistenceContext(unitName = "seguemePU")
    private EntityManager entityManager;

    @Override
    public VendaArtigo save(VendaArtigo artigo) {
        entityManager.persist(artigo);
        return artigo;
    }

    @Override
    public VendaArtigo update(VendaArtigo artigo) {
        return entityManager.merge(artigo);
    }

    @Override
    public Optional<VendaArtigo> findById(Long id) {
        try {
            VendaArtigo artigo = entityManager.find(VendaArtigo.class, id);
            return Optional.ofNullable(artigo);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<VendaArtigo> findByCodigo(String codigo) {
        try {
            VendaArtigo artigo = entityManager.createQuery(
                    "SELECT a FROM VendaArtigo a WHERE a.codigo = :codigo", 
                    VendaArtigo.class)
                    .setParameter("codigo", codigo)
                    .getSingleResult();
            return Optional.of(artigo);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<VendaArtigo> findAll() {
        return entityManager.createQuery(
                "SELECT a FROM VendaArtigo a ORDER BY a.nome", 
                VendaArtigo.class)
                .getResultList();
    }

    @Override
    public List<VendaArtigo> findAtivos() {
        return entityManager.createQuery(
                "SELECT a FROM VendaArtigo a WHERE a.ativo = TRUE ORDER BY a.nome", 
                VendaArtigo.class)
                .getResultList();
    }

    @Override
    public List<VendaArtigo> findByCategoria(String categoria) {
        return entityManager.createQuery(
                "SELECT a FROM VendaArtigo a WHERE a.categoria = :categoria ORDER BY a.nome", 
                VendaArtigo.class)
                .setParameter("categoria", categoria)
                .getResultList();
    }

    @Override
    public List<VendaArtigo> findComEstoqueBaixo() {
        return entityManager.createQuery(
                "SELECT a FROM VendaArtigo a " +
                "WHERE a.estoqueAtual < a.estoqueMinimo " +
                "AND a.ativo = TRUE " +
                "ORDER BY a.nome", 
                VendaArtigo.class)
                .getResultList();
    }

    @Override
    public List<VendaArtigo> findByNome(String nome) {
        return entityManager.createQuery(
                "SELECT a FROM VendaArtigo a " +
                "WHERE LOWER(a.nome) LIKE LOWER(:nome) " +
                "ORDER BY a.nome", 
                VendaArtigo.class)
                .setParameter("nome", "%" + nome + "%")
                .getResultList();
    }

    @Override
    public boolean delete(Long id) {
        VendaArtigo artigo = entityManager.find(VendaArtigo.class, id);
        if (artigo != null) {
            entityManager.remove(artigo);
            return true;
        }
        return false;
    }

    @Override
    public List<String> findAllCategorias() {
        return entityManager.createQuery(
                "SELECT DISTINCT a.categoria FROM VendaArtigo a " +
                "WHERE a.categoria IS NOT NULL " +
                "ORDER BY a.categoria", 
                String.class)
                .getResultList();
    }

    @Override
    public Integer findMaxSuffixForPrefix(String prefix) {
        try {
                    // Usa substring com regex para extrair dígitos no final do código
                    // substring(text from pattern) não é set-returning, evita erro de aggregate sobre set-returning
                    String sql = "SELECT MAX(CAST(substring(codigo from '[0-9]+$') AS integer)) FROM venda_artigo WHERE codigo LIKE :likePattern";
                    Object result = entityManager.createNativeQuery(sql)
                        .setParameter("likePattern", prefix + "%")
                        .getSingleResult();
            if (result == null) return null;
            if (result instanceof Number) {
                return ((Number) result).intValue();
            }
            try {
                return Integer.valueOf(result.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
