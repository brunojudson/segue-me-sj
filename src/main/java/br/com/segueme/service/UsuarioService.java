package br.com.segueme.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.mindrot.jbcrypt.BCrypt;

import br.com.segueme.entity.Usuario;
import br.com.segueme.repository.UsuarioRepository;

@ApplicationScoped
public class UsuarioService {

    @Inject
    private UsuarioRepository usuarioRepository;

    public Usuario autenticar(String email, String senha) {
        // Busca o usuário pelo e-mail
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("Usuário ou senha inválidos"));

        // Verifica se o usuário está ativo
        if (!usuario.getAtivo()) {
            throw new SecurityException("Usuário inativo");
        }

        // Valida a senha usando BCrypt
        if (!BCrypt.checkpw(senha, usuario.getSenha())) {
            throw new SecurityException("Usuário ou senha inválidos");
        }

        return usuario;
    }
}