package br.com.segueme.service;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.mindrot.jbcrypt.BCrypt;

import br.com.segueme.entity.Permissao;
import br.com.segueme.entity.Usuario;
import br.com.segueme.repository.UsuarioRepository;

@ApplicationScoped
public class UsuarioService {

    @Inject
    private UsuarioRepository usuarioRepository;

    @Inject
    private AuditoriaService auditoriaService;

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
    
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("ID da pessoa não pode ser nulo");
		}

		return usuarioRepository.findById(id);
	}

    public void save(Usuario usuario) {
        boolean isNovo = usuario.getId() == null;
        usuarioRepository.save(usuario);
        String acao = isNovo ? "INCLUÍDO" : "ALTERADO";
        auditoriaService.registrar("Usuario", usuario.getId(), acao, getUsuarioLogadoNome(), usuario);
    }

    public void delete(Long id) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);
        usuarioRepository.delete(id);
        usuarioExistente.ifPresent(u ->
            auditoriaService.registrar("Usuario", id, "EXCLUÍDO", getUsuarioLogadoNome(), u)
        );
    }

    public List<Permissao> buscaPermissoes() {
        return usuarioRepository.buscaPermissoes();
    }
    
    public void alterarSenha(Long usuarioId, String senhaAtual, String novaSenha) {
        // Busca o usuário
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new SecurityException("Usuário não encontrado"));
        
        // Verifica se a senha atual está correta
        if (!BCrypt.checkpw(senhaAtual, usuario.getSenha())) {
            throw new SecurityException("Senha atual incorreta");
        }
        
        // Valida a nova senha
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new SecurityException("Nova senha deve ter no mínimo 6 caracteres");
        }
        
        // Criptografa e atualiza apenas a senha via JPQL (evita validação da entidade inteira)
        String senhaHash = BCrypt.hashpw(novaSenha, BCrypt.gensalt());
        usuarioRepository.atualizarSenha(usuarioId, senhaHash);
    }
}