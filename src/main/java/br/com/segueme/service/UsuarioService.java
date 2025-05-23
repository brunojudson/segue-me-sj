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

    @SuppressWarnings("unused")
	public String getUsuarioLogadoNome() {
        Object loginController = javax.faces.context.FacesContext.getCurrentInstance()
            .getExternalContext().getSessionMap().get("loginController");
        if (loginController != null) {
            try {
                br.com.segueme.controller.LoginController controller = 
                    (br.com.segueme.controller.LoginController) loginController;
                if (controller.getUsuarioLogado() != null) {
                    return controller.getUsuarioLogado().getNome();
                }
            } catch (Exception e) {
                // log ou trate o erro se necessário
            }
        }
        return "Desconhecido";
    }
}