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

	private List<Permissao> permissoes;
	
	private Set<Permissao> permissoesSelecionadas; // permissões do usuário

	private List<Usuario> usuarios;
	private Usuario usuario = new Usuario();
	
	@PostConstruct
	public void init() {
		usuarios = usuarioservice.findAll();
		permissoes = usuarioservice.buscaPermissoes();

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

}
