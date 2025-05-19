package br.com.segueme.config;


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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter("/*")
public class AuthFilter implements Filter {

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

        // Verificar se o usuário está logado
        LoginController loginController = (session != null) ? 
            (LoginController) session.getAttribute("loginController") : null;

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