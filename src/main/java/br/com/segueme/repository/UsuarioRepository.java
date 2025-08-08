package br.com.segueme.repository;

import java.util.List;
import java.util.Optional;

import br.com.segueme.entity.Usuario;

public interface UsuarioRepository {
    Optional<Usuario> findByEmail(String email);
    
    List<Usuario> findAll();
    
    Optional<Usuario> findById(Long id);
    
    void save(Usuario usuario);
    
    void delete(Long id);
    
    List<br.com.segueme.entity.Permissao> buscaPermissoes();
}
