package br.com.segueme.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import br.com.segueme.controller.LoginController;
import br.com.segueme.service.UsuarioService;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Inject
    UsuarioService usuarioService;

    private static final String LOGIN_PAGE = "/pages/login.xhtml";
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/pages/login.xhtml",
            "/resources/",
            "/public/",
            "/javax.faces.resource/" // Adicionado para liberar recursos do JSF e PrimeFaces
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());
        HttpSession session = req.getSession(false);

        // Permitir acesso irrestrito a caminhos públicos
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // NOVO: Se for chamada da API, use Basic Auth
        if (path.startsWith("/api/")) {
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Basic ")) {
                String base64Credentials = authHeader.substring("Basic ".length());
                String credentials = new String(java.util.Base64.getDecoder().decode(base64Credentials));
                String[] values = credentials.split(":", 2);
                String user = values[0];
                String pass = values[1];
                // Troque pelo seu método de validação de usuário/senha:
                if (usuarioService.autenticar(user, pass) != null) {
                    chain.doFilter(request, response);
                    return;
                }
            }
            // Não autenticado: retorna 401
            res.setHeader("WWW-Authenticate", "Basic realm=\"API\"");
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Verificar se o usuário está logado
        LoginController loginController = (session != null) ? (LoginController) session.getAttribute("loginController")
                : null;

        if (isLoggedIn(loginController)) {
            chain.doFilter(request, response);
        } else {
            // Evitar redirecionamento infinito para a página de login
            if (path.equals(LOGIN_PAGE)) {
                chain.doFilter(request, response);
                return;
            }

            // Redirecionar para a página de login
            res.sendRedirect(req.getContextPath() + LOGIN_PAGE);
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isLoggedIn(LoginController loginController) {
        if (loginController == null) {
            return false;
        }
        if (loginController.getUsuarioLogado() == null) {
            return false;
        }
        return true;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Configurações iniciais, se necessário
    }

    @Override
    public void destroy() {
        // Limpeza de recursos, se necessário
    }
}