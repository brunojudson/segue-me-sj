package br.com.segueme.config;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtro CORS para permitir requisições cross-origin nas APIs
 */
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inicialização do filtro, se necessário
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Headers CORS
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        httpResponse.setHeader("Access-Control-Allow-Headers", 
            "Origin, Content-Type, Accept, Authorization, X-Requested-With, Access-Control-Request-Method, Access-Control-Request-Headers");
        httpResponse.setHeader("Access-Control-Expose-Headers", 
            "Content-Type, Authorization, X-Total-Count, Location");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        
        // Para requisições OPTIONS (preflight), responder imediatamente
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        // Continuar a cadeia de filtros
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Limpeza do filtro, se necessário
    }
}


