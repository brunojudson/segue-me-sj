package br.com.segueme.repository.impl;

import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.mindrot.jbcrypt.BCrypt;

import br.com.segueme.entity.Permissao;
import br.com.segueme.entity.Usuario;
import br.com.segueme.repository.UsuarioRepository;

@Stateless
public class UsuarioRepositoryImpl implements UsuarioRepository {

    @PersistenceContext(unitName = "seguemePU")
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
    public List<Usuario> findAll() {
        return entityManager.createQuery(
            "SELECT DISTINCT u FROM Usuario u JOIN FETCH u.permissoes", Usuario.class
        ).getResultList();
    }

    public Optional<Usuario> findById(Long id) {
    	Usuario usuario = entityManager.find(Usuario.class, id);
        return Optional.ofNullable(usuario);
    }

    public void save(Usuario usuario) {
    	System.out.println("Chegou aqui repository: " + usuario.getId());
        // Só gera hash se a senha não estiver vazia e não for um hash já existente
        if (usuario.getSenha() != null && !usuario.getSenha().startsWith("$2a$")) {
            String hash = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt());
            usuario.setSenha(hash);
        }
        if (usuario.getId() == null) {
            entityManager.persist(usuario);
        } else {
            entityManager.merge(usuario);
        }
    }

    public void delete(Long id) {
        Usuario u = entityManager.find(Usuario.class, id);
        if (u != null) entityManager.remove(u);
    }
    
    public List<Permissao> buscaPermissoes() {
        return entityManager.createQuery("SELECT p FROM Permissao p", Permissao.class).getResultList();
    }
}
