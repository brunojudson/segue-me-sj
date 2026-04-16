package br.com.segueme.controller;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Permissao;
import br.com.segueme.entity.Usuario;
import br.com.segueme.service.UsuarioService;

@Named
@ViewScoped
public class UsuarioController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private UsuarioService usuarioservice;
	
	@Inject
	private LoginController loginController;

	private List<Permissao> permissoes;
	
	private Set<Permissao> permissoesSelecionadas; // permissões do usuário

	private List<Usuario> usuarios;
	private Usuario usuario = new Usuario();
	
	private String senhaAtual;
	private String novaSenha;
	private String confirmaNovaSenha;
	
	@PostConstruct
	public void init() {
		// Se for admin, carrega todos os usuários
		if (loginController != null && loginController.hasPermission("ADMIN")) {
			usuarios = usuarioservice.findAll();
			permissoes = usuarioservice.buscaPermissoes();
		} else {
			// Se não for admin, carrega apenas o próprio usuário
			if (loginController != null && loginController.getUsuarioLogado() != null) {
				usuarios = List.of(loginController.getUsuarioLogado());
			}
		}
	}

	public void novo() {
		usuario = new Usuario();
	}

	public String salvar() {
		usuario.setPermissoes(permissoesSelecionadas);
		usuarioservice.save(usuario);
		usuarios = usuarioservice.findAll();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Usuário salvo com sucesso!"));
		usuario = new Usuario();
		
		return "lista?faces-redirect=true";
	}
	
	public void carregarUsuario() {
		String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		if (idParam != null && !idParam.isEmpty()) {
			Long id = Long.valueOf(idParam);
			usuarioservice.buscarPorId(id).ifPresent(u -> {
				this.usuario = u;
				// Copia as permissões para um novo HashSet para evitar LazyInitializationException
				this.permissoesSelecionadas = new java.util.HashSet<>(u.getPermissoes());
				this.usuario.setSenha(null); // Limpa somente o campo senha
			});
		}
	}
	
	public String editar(Usuario usuario) {
		this.usuario = usuario;
		return "cadastro?faces-redirect=true&id=" + usuario.getId();
	}

	public void excluir(Usuario u) {
		usuarioservice.delete(u.getId());
		usuarios = usuarioservice.findAll();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Usuário excluído com sucesso!"));
	}
	
	public String alterarSenha() {
		try {
			// Valida se o usuário está logado
			if (loginController == null || loginController.getUsuarioLogado() == null) {
				FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Usuário não autenticado"));
				return null;
			}
			
			// Valida se as senhas foram preenchidas
			if (senhaAtual == null || senhaAtual.isEmpty()) {
				FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Senha atual é obrigatória"));
				return null;
			}
			
			if (novaSenha == null || novaSenha.isEmpty()) {
				FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Nova senha é obrigatória"));
				return null;
			}
			
			if (confirmaNovaSenha == null || confirmaNovaSenha.isEmpty()) {
				FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Confirmação de senha é obrigatória"));
				return null;
			}
			
			// Valida se as novas senhas coincidem
			if (!novaSenha.equals(confirmaNovaSenha)) {
				FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "As senhas não coincidem"));
				return null;
			}
			
			// Chama o serviço para alterar a senha
			usuarioservice.alterarSenha(loginController.getUsuarioLogado().getId(), senhaAtual, novaSenha);
			
			// Limpa os campos
			senhaAtual = null;
			novaSenha = null;
			confirmaNovaSenha = null;
			
			FacesContext.getCurrentInstance().addMessage(null, 
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Senha alterada com sucesso!"));
			
			return null;
		} catch (SecurityException e) {
			FacesContext.getCurrentInstance().addMessage(null, 
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
			return null;
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, 
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao alterar senha: " + e.getMessage()));
			return null;
		}
	}
	
	public Set<Permissao> getPermissoesSelecionadas() {
	    return permissoesSelecionadas;
	}

	// Setter para permissoesSelecionadas
	public void setPermissoesSelecionadas(Set<Permissao> permissoesSelecionadas) {
	    this.permissoesSelecionadas = permissoesSelecionadas;
	}
	// Getters e setters
	public List<Usuario> getUsuarios() {
		return usuarios;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuarios(List<Usuario> usuarios) {
		this.usuarios = usuarios;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public List<Permissao> getPermissoes() {
		return permissoes;
	}

	public void setPermissoes(List<Permissao> permissoes) {
		this.permissoes = permissoes;
	}

	public String getSenhaAtual() {
		return senhaAtual;
	}

	public void setSenhaAtual(String senhaAtual) {
		this.senhaAtual = senhaAtual;
	}

	public String getNovaSenha() {
		return novaSenha;
	}

	public void setNovaSenha(String novaSenha) {
		this.novaSenha = novaSenha;
	}

	public String getConfirmaNovaSenha() {
		return confirmaNovaSenha;
	}

	public void setConfirmaNovaSenha(String confirmaNovaSenha) {
		this.confirmaNovaSenha = confirmaNovaSenha;
	}

}
