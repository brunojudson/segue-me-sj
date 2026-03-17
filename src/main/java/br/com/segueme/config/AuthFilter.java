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
            "/pages/index.xhtml",
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

        // Se não estiver logado, permitir apenas caminhos públicos e a página de login
        if (!isLoggedIn(loginController)) {
            if (isPublicPath(path) || path.equals(LOGIN_PAGE)) {
                chain.doFilter(request, response);
                return;
            }
            res.sendRedirect(req.getContextPath() + LOGIN_PAGE);
            return;
        }

        // A partir daqui: usuário logado
        // ADMIN tem acesso a tudo
        try {
            if (loginController.hasPermission("ADMIN")) {
                chain.doFilter(request, response);
                return;
            }
        } catch (Exception e) {
            // Se por algum motivo o método não existir ou falhar, negar acesso como fallback
        }

        // Bloquear acesso à área de usuários para não-ADMINs
        if (path.startsWith("/pages/usuario")) {
            res.sendRedirect(req.getContextPath() + "/pages/index.xhtml");
            return;
        }

        // Perfil VENDA só pode acessar páginas dentro de /pages/venda
        try {
            if (loginController.hasPermission("VENDA")) {
                if (path.startsWith("/pages/venda") || isPublicPath(path)) {
                    chain.doFilter(request, response);
                    return;
                } else {
                    res.sendRedirect(req.getContextPath() + "/pages/index.xhtml");
                    return;
                }
            }
        } catch (Exception e) {
            // Ignorar e tratar como usuário comum
        }

        // Usuário comum (USER) — permitir acesso geral, exceto áreas administrativas já bloqueadas acima
        chain.doFilter(request, response);
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