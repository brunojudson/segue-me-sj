package br.com.segueme.controller;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Usuario;
import br.com.segueme.service.UsuarioService;

@Named
@SessionScoped
public class LoginController implements Serializable {
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String email;
    private String senha;
    private Usuario usuarioLogado;

    @Inject
    private UsuarioService usuarioService;

    public String login() {
        try {
            usuarioLogado = usuarioService.autenticar(email, senha);
            // Armazenar o LoginController na sessão
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("loginController", this);

            return "/pages/index.xhtml?faces-redirect=true";
        } catch (SecurityException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
            return null;
        }
    }

    public String getPrimeirosDoisNomes() {
        if (usuarioLogado == null || usuarioLogado.getNome() == null) {
            return "";
        }
        String[] nomes = usuarioLogado.getNome().trim().split("\\s+");
        if (nomes.length == 1) {
            return nomes[0];
        }
         return nomes[0] + " " + nomes[1] + " " + nomes[2];
    }

    public boolean hasPermission(String permission) {
        if (usuarioLogado == null) {
            return false;
        }

        if (usuarioLogado.getPermissoes() == null || usuarioLogado.getPermissoes().isEmpty()) {
            return false;
        }

     // Verificar se o nome da permissão está presente
        boolean possuiPermissao = usuarioLogado.getPermissoes().stream()
            .anyMatch(permissao -> permissao.getNome().equals(permission));

        return possuiPermissao;
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/pages/login.xhtml?faces-redirect=true";
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}