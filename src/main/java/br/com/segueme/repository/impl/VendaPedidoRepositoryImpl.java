package br.com.segueme.repository.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.VendaPedido;
import br.com.segueme.entity.enums.StatusPedido;
import br.com.segueme.repository.VendaPedidoRepository;

@Stateless
public class VendaPedidoRepositoryImpl implements VendaPedidoRepository {

    @PersistenceContext(unitName = "seguemePU")
    private EntityManager entityManager;

    @Override
    public VendaPedido save(VendaPedido pedido) {
        entityManager.persist(pedido);
        entityManager.flush(); // Força a geração do ID e do numeroPedido pelo trigger
        return pedido;
    }

    @Override
    public VendaPedido update(VendaPedido pedido) {
        return entityManager.merge(pedido);
    }

    @Override
    public Optional<VendaPedido> findById(Long id) {
        try {
                VendaPedido pedido = entityManager.createQuery(
                    "SELECT DISTINCT p FROM VendaPedido p " +
                    "LEFT JOIN FETCH p.encontro e " +
                    "LEFT JOIN FETCH p.trabalhadorResponsavel tr " +
                    "LEFT JOIN FETCH tr.pessoa " +
                    "LEFT JOIN FETCH p.fechadoPorTrabalhador ft " +
                    "LEFT JOIN FETCH ft.pessoa " +
                    "LEFT JOIN FETCH p.itens i " +
                    "LEFT JOIN FETCH i.artigo " +
                    "WHERE p.id = :id", 
                    VendaPedido.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(pedido);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<VendaPedido> findByNumeroPedido(String numeroPedido) {
        try {
            VendaPedido pedido = entityManager.createQuery(
                    "SELECT p FROM VendaPedido p " +
                    "LEFT JOIN FETCH p.encontro " +
                    "LEFT JOIN FETCH p.trabalhadorResponsavel tr " +
                    "LEFT JOIN FETCH tr.pessoa " +
                    "LEFT JOIN FETCH p.fechadoPorTrabalhador ft " +
                    "LEFT JOIN FETCH ft.pessoa " +
                    "WHERE p.numeroPedido = :numeroPedido", 
                    VendaPedido.class)
                    .setParameter("numeroPedido", numeroPedido)
                    .getSingleResult();
            return Optional.of(pedido);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<VendaPedido> findAll() {
        return entityManager.createQuery(
                "SELECT DISTINCT p FROM VendaPedido p " +
                "LEFT JOIN FETCH p.encontro " +
                "LEFT JOIN FETCH p.trabalhadorResponsavel tr " +
                "LEFT JOIN FETCH tr.pessoa " +
                "LEFT JOIN FETCH p.fechadoPorTrabalhador ft " +
                "LEFT JOIN FETCH ft.pessoa " +
                "LEFT JOIN FETCH p.itens i " +
                "LEFT JOIN FETCH i.artigo " +
                "ORDER BY p.dataAbertura DESC", 
                VendaPedido.class)
                .getResultList();
    }

    @Override
    public List<VendaPedido> findByEncontro(Long encontroId) {
        return entityManager.createQuery(
                "SELECT DISTINCT p FROM VendaPedido p " +
                "LEFT JOIN FETCH p.encontro " +
                "LEFT JOIN FETCH p.trabalhadorResponsavel tr " +
                "LEFT JOIN FETCH tr.pessoa " +
                "LEFT JOIN FETCH p.fechadoPorTrabalhador ft " +
                "LEFT JOIN FETCH ft.pessoa " +
                "LEFT JOIN FETCH p.itens i " +
                "LEFT JOIN FETCH i.artigo " +
                "WHERE p.encontro.id = :encontroId " +
                "ORDER BY p.dataAbertura DESC", 
                VendaPedido.class)
                .setParameter("encontroId", encontroId)
                .getResultList();
    }

    @Override
    public List<VendaPedido> findByTrabalhador(Long trabalhadorId) {
        return entityManager.createQuery(
                "SELECT DISTINCT p FROM VendaPedido p " +
                "LEFT JOIN FETCH p.encontro " +
                "LEFT JOIN FETCH p.trabalhadorResponsavel tr " +
                "LEFT JOIN FETCH tr.pessoa " +
                "LEFT JOIN FETCH p.itens i " +
                "LEFT JOIN FETCH i.artigo " +
                "WHERE p.trabalhadorResponsavel.id = :trabalhadorId " +
                "ORDER BY p.dataAbertura DESC", 
                VendaPedido.class)
                .setParameter("trabalhadorId", trabalhadorId)
                .getResultList();
    }

    @Override
    public List<VendaPedido> findByStatus(StatusPedido status) {
        return entityManager.createQuery(
                "SELECT DISTINCT p FROM VendaPedido p " +
                "LEFT JOIN FETCH p.encontro " +
                "LEFT JOIN FETCH p.trabalhadorResponsavel tr " +
                "LEFT JOIN FETCH tr.pessoa " +
                "LEFT JOIN FETCH p.fechadoPorTrabalhador ft " +
                "LEFT JOIN FETCH ft.pessoa " +
                "LEFT JOIN FETCH p.itens i " +
                "LEFT JOIN FETCH i.artigo " +
                "WHERE p.status = :status " +
                "ORDER BY p.dataAbertura DESC", 
                VendaPedido.class)
                .setParameter("status", status)
                .getResultList();
    }

    public List<VendaPedido> findAbertosByEncontro(Long encontroId) {
        return entityManager.createQuery(
            "SELECT DISTINCT p FROM VendaPedido p " +
            "LEFT JOIN FETCH p.encontro " +
            "LEFT JOIN FETCH p.trabalhadorResponsavel tr " +
            "LEFT JOIN FETCH tr.pessoa " +
            "LEFT JOIN FETCH p.fechadoPorTrabalhador ft " +
            "LEFT JOIN FETCH ft.pessoa " +
            "LEFT JOIN FETCH p.itens i " +
            "LEFT JOIN FETCH i.artigo " +
            "WHERE p.encontro.id = :encontroId " +
            "AND p.status = :status " +
            "ORDER BY p.dataAbertura DESC", 
            VendaPedido.class)
            .setParameter("encontroId", encontroId)
            .setParameter("status", StatusPedido.ABERTO)
            .getResultList();
    }

    @Override
    public List<VendaPedido> findAguardoPagamentoByEncontro(Long encontroId) {
        return entityManager.createQuery(
            "SELECT DISTINCT p FROM VendaPedido p " +
            "LEFT JOIN FETCH p.encontro " +
            "LEFT JOIN FETCH p.trabalhadorResponsavel tr " +
            "LEFT JOIN FETCH tr.pessoa " +
            "LEFT JOIN FETCH p.fechadoPorTrabalhador ft " +
            "LEFT JOIN FETCH ft.pessoa " +
            "LEFT JOIN FETCH p.itens i " +
            "LEFT JOIN FETCH i.artigo " +
            "WHERE p.encontro.id = :encontroId " +
            "AND p.status = :status " +
            "ORDER BY p.dataFechamento DESC", 
            VendaPedido.class)
            .setParameter("encontroId", encontroId)
            .setParameter("status", StatusPedido.AGUARDO_PAGAMENTO)
            .getResultList();
    }

    @Override
    public List<VendaPedido> findByEncontroAndStatus(Long encontroId, StatusPedido status) {
        return entityManager.createQuery(
                "SELECT DISTINCT p FROM VendaPedido p " +
                "LEFT JOIN FETCH p.encontro " +
                "LEFT JOIN FETCH p.trabalhadorResponsavel tr " +
                "LEFT JOIN FETCH tr.pessoa " +
                "LEFT JOIN FETCH p.fechadoPorTrabalhador ft " +
                "LEFT JOIN FETCH ft.pessoa " +
                "LEFT JOIN FETCH p.itens i " +
                "LEFT JOIN FETCH i.artigo " +
                "WHERE p.encontro.id = :encontroId " +
                "AND p.status = :status " +
                "ORDER BY p.dataAbertura DESC", 
                VendaPedido.class)
                .setParameter("encontroId", encontroId)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public List<VendaPedido> findByPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return entityManager.createQuery(
                "SELECT DISTINCT p FROM VendaPedido p " +
                "LEFT JOIN FETCH p.encontro " +
                "LEFT JOIN FETCH p.trabalhadorResponsavel tr " +
                "LEFT JOIN FETCH tr.pessoa " +
                "LEFT JOIN FETCH p.fechadoPorTrabalhador ft " +
                "LEFT JOIN FETCH ft.pessoa " +
                "LEFT JOIN FETCH p.itens i " +
                "LEFT JOIN FETCH i.artigo " +
                "WHERE p.dataAbertura BETWEEN :dataInicio AND :dataFim " +
                "ORDER BY p.dataAbertura DESC", 
                VendaPedido.class)
                .setParameter("dataInicio", dataInicio)
                .setParameter("dataFim", dataFim)
                .getResultList();
    }

    @Override
    public List<VendaPedido> findByPessoa(Long pessoaId) {
        return entityManager.createQuery(
                "SELECT DISTINCT p FROM VendaPedido p " +
                "LEFT JOIN FETCH p.encontro " +
                "LEFT JOIN FETCH p.trabalhadorResponsavel tr " +
                "LEFT JOIN FETCH tr.pessoa " +
                "LEFT JOIN FETCH p.fechadoPorTrabalhador ft " +
                "LEFT JOIN FETCH ft.pessoa " +
                "LEFT JOIN FETCH p.itens i " +
                "LEFT JOIN FETCH i.artigo " +
                "LEFT JOIN p.pessoasAssociadas pp " +
                "WHERE pp.pessoa.id = :pessoaId " +
                "ORDER BY p.dataAbertura DESC", 
                VendaPedido.class)
                .setParameter("pessoaId", pessoaId)
                .getResultList();
    }

    @Override
    public boolean delete(Long id) {
        VendaPedido pedido = entityManager.find(VendaPedido.class, id);
        if (pedido != null) {
            entityManager.remove(pedido);
            return true;
        }
        return false;
    }
}
