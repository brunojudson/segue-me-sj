package br.com.segueme.repository;

import java.util.Optional;

import br.com.segueme.entity.Usuario;

public interface UsuarioRepository {
    Optional<Usuario> findByEmail(String email);
}
