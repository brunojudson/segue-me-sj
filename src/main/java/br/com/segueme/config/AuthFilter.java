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
    // Páginas acessíveis a qualquer usuário logado (independente do perfil)
    private static final List<String> LOGGED_IN_PATHS = Arrays.asList(
            "/pages/usuario/alterar-senha.xhtml"
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
        // Páginas acessíveis a qualquer usuário logado
        if (isLoggedInPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Detectar permissões do usuário
        boolean isAdmin = loginController.hasPermission("ADMIN");
        boolean isUser = loginController.hasPermission("USER");
        boolean isVenda = loginController.hasPermission("VENDA");
        boolean isProver = loginController.hasPermission("PROVER");

        // Usuário sem nenhuma permissão: só alterar senha (já tratado acima), resto redireciona para login
        if (!isAdmin && !isUser && !isVenda && !isProver) {
            res.sendRedirect(req.getContextPath() + LOGIN_PAGE);
            return;
        }

        // Página inicial é acessível a qualquer usuário com pelo menos um perfil
        if (path.equals("/pages/index.xhtml")) {
            chain.doFilter(request, response);
            return;
        }

        // ADMIN tem acesso a tudo
        if (isAdmin) {
            chain.doFilter(request, response);
            return;
        }

        // Bloquear acesso à área de usuários (cadastro/lista) para não-ADMINs
        if (path.startsWith("/pages/usuario")) {
            res.sendRedirect(req.getContextPath() + "/pages/index.xhtml");
            return;
        }

        // Perfil USER: quase tudo, exceto encontros (criar/manipular), vendas, financeiro, usuários
        if (isUser) {
            if (path.startsWith("/pages/venda")
                || path.startsWith("/pages/encontro")
                || path.startsWith("/pages/financeiro")) {
                res.sendRedirect(req.getContextPath() + "/pages/index.xhtml");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        // Perfil PROVER: vendas, contribuições e alterar senha
        if (isProver) {
            if (path.startsWith("/pages/venda")
                || path.startsWith("/pages/contribuicao")
                || isPublicPath(path)) {
                chain.doFilter(request, response);
                return;
            }
            res.sendRedirect(req.getContextPath() + "/pages/index.xhtml");
            return;
        }

        // Perfil VENDA: só páginas de venda e alterar senha
        if (isVenda) {
            if (path.startsWith("/pages/venda") || isPublicPath(path)) {
                chain.doFilter(request, response);
                return;
            }
            res.sendRedirect(req.getContextPath() + "/pages/index.xhtml");
            return;
        }

        // Usuário sem nenhuma permissão conhecida: redireciona para login
        res.sendRedirect(req.getContextPath() + LOGIN_PAGE);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isLoggedInPath(String path) {
        return LOGGED_IN_PATHS.stream().anyMatch(path::equals);
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