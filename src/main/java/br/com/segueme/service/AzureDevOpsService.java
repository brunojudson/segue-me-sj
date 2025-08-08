package br.com.segueme.service;

import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Serviço para integração com Azure DevOps da Caixa
 */
@ApplicationScoped
public class AzureDevOpsService {
    
    private static final Logger LOGGER = Logger.getLogger(AzureDevOpsService.class.getName());
    
    // Configurações do Azure DevOps da Caixa
    private static final String AZURE_DEVOPS_BASE_URL = "https://devops.caixa";
    private static final String COLLECTION = "projetos";
    private static final String PROJECTS_COLLECTION = "Caixa";
    private static final String TOKEN = "";
    private static final String HTTPS_PREFIX = "https://";
    private static final String PROJECTS_API = "/_apis/projects?api-version=7.1";
    
    // Flag para controlar se SSL foi configurado
    private static boolean sslConfigurado = false;
    
    /**
     * Configura SSL para aceitar certificados auto-assinados
     * ATENÇÃO: Isso deve ser usado apenas em ambientes de desenvolvimento/teste
     */
    private static void configurarSSL() {
        if (sslConfigurado) {
            return;
        }
        
        try {
            // Criar um trust manager que aceita todos os certificados
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // Não fazer validação
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // Não fazer validação
                    }
                }
            };
            
            // Instalar o trust manager
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            // Criar um hostname verifier que aceita todos os hostnames
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            
            sslConfigurado = true;
            LOGGER.info("SSL configurado para aceitar certificados auto-assinados");
            
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LOGGER.warning("Erro ao configurar SSL: " + e.getMessage());
        }
    }
    
    /**
     * Busca todos os repositórios da organização Caixa
     * @return JSON com a lista de repositórios
     * @throws IOException se houver erro na requisição
     */
    public String buscarRepositoriosCaixa() throws IOException {
        // Configurar SSL antes de fazer a requisição
        configurarSSL();
        
        // Tentar diferentes formatos de URL
        String[] endpoints = {
            // Formato padrão Azure DevOps
            String.format("%s/%s/%s/_apis/git/repositories?api-version=7.1", 
                         AZURE_DEVOPS_BASE_URL, COLLECTION, PROJECTS_COLLECTION),
            
            // Formato alternativo sem collection
            String.format("%s/%s/_apis/git/repositories?api-version=7.1", 
                         AZURE_DEVOPS_BASE_URL, PROJECTS_COLLECTION),
            
            // Formato TFS on-premise
            String.format("%s/tfs/%s/%s/_apis/git/repositories?api-version=7.1", 
                         AZURE_DEVOPS_BASE_URL, COLLECTION, PROJECTS_COLLECTION),
            
            // Formato com DefaultCollection
            String.format("%s/DefaultCollection/%s/_apis/git/repositories?api-version=7.1", 
                         AZURE_DEVOPS_BASE_URL, PROJECTS_COLLECTION)
        };
        
        IOException lastException = null;
        
        for (String endpoint : endpoints) {
            try {
                LOGGER.info("Tentando endpoint: " + endpoint);
                return executarRequisicaoGet(endpoint);
            } catch (IOException e) {
                LOGGER.warning("Falha no endpoint " + endpoint + ": " + e.getMessage());
                lastException = e;
                
                // Se for redirecionamento, não tentar HTTP
                if (e.getMessage().contains("Redirecionamento detectado")) {
                    continue;
                }
                
                // Tentar versão HTTP se for HTTPS
                if (endpoint.startsWith(HTTPS_PREFIX)) {
                    try {
                        String httpEndpoint = endpoint.replace(HTTPS_PREFIX, "http://");
                        LOGGER.info("Tentando versão HTTP: " + httpEndpoint);
                        return executarRequisicaoGet(httpEndpoint);
                    } catch (IOException httpException) {
                        LOGGER.warning("Falha também na versão HTTP: " + httpException.getMessage());
                    }
                }
            }
        }
        
        throw new IOException("Todos os endpoints falharam. Último erro: " + 
                            (lastException != null ? lastException.getMessage() : "Desconhecido"));
    }
    
    /**
     * Executa uma requisição GET para o Azure DevOps
     * @param endpoint URL completa do endpoint
     * @return resposta da API em JSON
     * @throws IOException se houver erro na requisição
     */
    private String executarRequisicaoGet(String endpoint) throws IOException {
        // Configurar SSL se necessário
        if (endpoint.startsWith(HTTPS_PREFIX)) {
            configurarSSL();
        }
        
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            // Configurar a conexão
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Java-SegueMe-App/1.0");
            
            // Adicionar autenticação básica com token
            String auth = ":" + TOKEN;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            
            // Configurar para não seguir redirecionamentos automaticamente
            connection.setInstanceFollowRedirects(false);
            
            // Configurar timeouts
            connection.setConnectTimeout(30000); // 30 segundos
            connection.setReadTimeout(60000);    // 60 segundos
            
            // Verificar código de resposta
            int responseCode = connection.getResponseCode();
            LOGGER.info("Response Code: " + responseCode + " para URL: " + endpoint);
            
            // Lidar com redirecionamentos
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || 
                responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                
                String location = connection.getHeaderField("Location");
                LOGGER.warning("Redirecionamento detectado para: " + location);
                throw new IOException("Redirecionamento detectado (código " + responseCode + ") para: " + location + 
                                    ". Isso pode indicar problema de autenticação ou URL incorreta.");
            }
            
            if (responseCode >= 200 && responseCode < 300) {
                // Ler resposta de sucesso
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            } else {
                // Ler resposta de erro
                String errorResponse = "";
                try {
                    // Tentar ler do error stream primeiro
                    if (connection.getErrorStream() != null) {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                            StringBuilder error = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                error.append(line);
                            }
                            errorResponse = error.toString();
                        }
                    } else {
                        // Se não houver error stream, tentar ler do input stream
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                            StringBuilder error = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                error.append(line);
                            }
                            errorResponse = error.toString();
                        }
                    }
                } catch (Exception e) {
                    errorResponse = "Não foi possível ler a resposta de erro: " + e.getMessage();
                }
                
                // Log dos headers para debug
                LOGGER.warning("Headers da resposta:");
                connection.getHeaderFields().forEach((key, value) -> 
                    LOGGER.warning("  " + key + ": " + value));
                
                throw new IOException("Erro na requisição. Código: " + responseCode + 
                                    ", URL: " + endpoint + 
                                    ", Resposta: " + errorResponse);
            }
            
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Testa a conectividade básica com o Azure DevOps
     * @return informações sobre o teste de conectividade
     */
    public String testarConectividade() throws IOException {
        // Tentar primeiro um endpoint simples para verificar conectividade
        String[] testEndpoints = {
            AZURE_DEVOPS_BASE_URL + PROJECTS_API,
            AZURE_DEVOPS_BASE_URL + "/" + COLLECTION + PROJECTS_API,
            AZURE_DEVOPS_BASE_URL + "/tfs/" + COLLECTION + PROJECTS_API
        };
        
        StringBuilder resultado = new StringBuilder();
        resultado.append("Teste de conectividade Azure DevOps:\n");
        
        for (String endpoint : testEndpoints) {
            try {
                resultado.append("Testando: ").append(endpoint).append("\n");
                String response = executarRequisicaoGet(endpoint);
                resultado.append("✓ Sucesso - Resposta: ").append(response.substring(0, Math.min(200, response.length()))).append("...\n");
                return resultado.toString();
            } catch (IOException e) {
                resultado.append("✗ Falha: ").append(e.getMessage()).append("\n");
            }
        }
        
        throw new IOException("Teste de conectividade falhou:\n" + resultado.toString());
    }
    
    /**
     * Método público para executar requisições GET (usado para testes)
     * @param endpoint URL completa do endpoint
     * @return resposta da API em JSON
     * @throws IOException se houver erro na requisição
     */
    public String executarRequisicaoGetPublic(String endpoint) throws IOException {
        return executarRequisicaoGet(endpoint);
    }
}
