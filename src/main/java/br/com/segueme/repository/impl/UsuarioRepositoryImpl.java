package br.com.segueme.repository.impl;

import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.segueme.entity.Permissao;
import br.com.segueme.entity.Usuario;
import br.com.segueme.repository.UsuarioRepository;

@Stateless
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioRepositoryImpl.class);

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
            "SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.permissoes", Usuario.class
        ).getResultList();
    }

    public Optional<Usuario> findById(Long id) {
        try {
            Usuario usuario = entityManager.createQuery(
                "SELECT u FROM Usuario u LEFT JOIN FETCH u.permissoes WHERE u.id = :id", Usuario.class)
                .setParameter("id", id)
                .getSingleResult();
            return Optional.of(usuario);
        } catch (javax.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    public void save(Usuario usuario) {
    	logger.debug("Salvando usuário: {}", usuario.getId());
        if (usuario.getId() == null) {
            // Novo usuário: senha obrigatória, gera hash
            if (usuario.getSenha() != null && !usuario.getSenha().startsWith("$2a$")) {
                String hash = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt());
                usuario.setSenha(hash);
            }
            entityManager.persist(usuario);
        } else {
            // Edição: busca o usuário existente com permissões carregadas
            Usuario existente = entityManager.createQuery(
                "SELECT u FROM Usuario u LEFT JOIN FETCH u.permissoes WHERE u.id = :id", Usuario.class)
                .setParameter("id", usuario.getId())
                .getSingleResult();
            
            // Atualiza campos do existente (evita LazyInitializationException no merge)
            existente.setNome(usuario.getNome());
            existente.setEmail(usuario.getEmail());
            existente.setAtivo(usuario.getAtivo());
            
            // Atualiza senha se foi fornecida
            if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
                if (!usuario.getSenha().startsWith("$2a$")) {
                    String hash = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt());
                    existente.setSenha(hash);
                } else {
                    existente.setSenha(usuario.getSenha());
                }
            }
            // Mantém a senha atual se não foi fornecida (não faz nada)
            
            // Atualiza permissões
            existente.getPermissoes().clear();
            if (usuario.getPermissoes() != null) {
                existente.getPermissoes().addAll(usuario.getPermissoes());
            }
            
            // JPA fará dirty checking e update automático (não precisa merge explícito)
        }
    }

    public void delete(Long id) {
        Usuario u = entityManager.find(Usuario.class, id);
        if (u != null) entityManager.remove(u);
    }
    
    @Override
    public void atualizarSenha(Long id, String senhaHash) {
        entityManager.createQuery("UPDATE Usuario u SET u.senha = :senha WHERE u.id = :id")
            .setParameter("senha", senhaHash)
            .setParameter("id", id)
            .executeUpdate();
    }
    
    public List<Permissao> buscaPermissoes() {
        return entityManager.createQuery("SELECT p FROM Permissao p", Permissao.class).getResultList();
    }
}
