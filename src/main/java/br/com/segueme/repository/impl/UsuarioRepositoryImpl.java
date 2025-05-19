package br.com.segueme.repository.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.segueme.entity.Usuario;
import br.com.segueme.repository.UsuarioRepository;

@ApplicationScoped
public class UsuarioRepositoryImpl implements UsuarioRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Usuario> findByEmail(String email) {
        try {
            Usuario usuario = entityManager.createQuery(
            		"SELECT u FROM Usuario u LEFT JOIN FETCH u.permissoes WHERE u.email = :email", Usuario.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(usuario);
        } catch (javax.persistence.NoResultException e) {
            return Optional.empty();
        }
    }
}
