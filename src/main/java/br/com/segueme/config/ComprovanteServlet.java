package br.com.segueme.config;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Servlet para servir arquivos de comprovante financeiro armazenados no filesystem.
 * Mapeado para /comprovantes/* e serve arquivos do diretório configurado via
 * system property "caminho_comprovantes".
 */
@WebServlet("/comprovantes/*")
public class ComprovanteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String CAMINHO_COMPROVANTES =
            System.getProperty("caminho_comprovantes", "C:\\Desenvolvimento\\comprovantes")
            + File.separator;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.contains("..")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Remove a barra inicial
        String nomeArquivo = pathInfo.substring(1);

        // Valida que é somente nome de arquivo (sem subdiretórios)
        if (nomeArquivo.contains("/") || nomeArquivo.contains("\\")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        File arquivo = new File(CAMINHO_COMPROVANTES + nomeArquivo);

        if (!arquivo.exists() || !arquivo.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = determinarContentType(nomeArquivo);
        response.setContentType(contentType);
        response.setContentLengthLong(arquivo.length());
        response.setHeader("Cache-Control", "private, max-age=86400");

        try (FileInputStream fis = new FileInputStream(arquivo);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private String determinarContentType(String nomeArquivo) {
        String lower = nomeArquivo.toLowerCase();
        if (lower.endsWith(".pdf"))  return "application/pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}
