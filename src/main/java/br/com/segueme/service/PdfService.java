package br.com.segueme.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import br.com.segueme.entity.Casal;
import br.com.segueme.entity.Encontrista;
import br.com.segueme.entity.Pessoa;

/**
 * Serviço centralizado para geração de documentos PDF.
 * Substitui a lógica duplicada de geração de PDF espalhada pelos controllers.
 */
@ApplicationScoped
public class PdfService {

    private static final Logger logger = LoggerFactory.getLogger(PdfService.class);

    private static final String CAMINHO_FOTOS = System.getProperty("caminho_fotos", "C:\\Desenvolvimento\\fotos")
            + java.io.File.separator;

    private static final Font TITULO_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font DADOS_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    private static final Font CABECALHO_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font DADOS_TABELA_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);

    /**
     * Gera a ficha de inscrição de uma Pessoa e envia como download PDF.
     */
    public void gerarFichaInscricaoPessoa(Pessoa pessoa) {
        Document document = new Document();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            adicionarTitulo(document, "Ficha de Inscrição");
            adicionarFoto(document, pessoa.getFoto(), "/resources/images/default_avatar.png");

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Nome: " + pessoa.getNome(), DADOS_FONT));
            document.add(new Paragraph("CPF: " + pessoa.getCpf(), DADOS_FONT));
            document.add(new Paragraph("Data de Nascimento: " + pessoa.getDataNascimento(), DADOS_FONT));
            document.add(new Paragraph("Endereço: " + pessoa.getEndereco(), DADOS_FONT));
            document.add(new Paragraph("Telefone: " + pessoa.getTelefone(), DADOS_FONT));
            document.add(new Paragraph("E-mail: " + pessoa.getEmail(), DADOS_FONT));
            document.add(new Paragraph(
                    "Sexo: " + (pessoa.getSexo() != null ? pessoa.getSexo() : "Não informado"), DADOS_FONT));
            document.add(new Paragraph(
                    "Idade: " + (pessoa.getIdade() != null ? pessoa.getIdade() : "Não calculada"), DADOS_FONT));

            document.close();
            enviarPdfParaNavegador(baos, "Ficha_Inscricao_" + pessoa.getId() + ".pdf");
        } catch (Exception e) {
            logger.error("Erro ao gerar ficha de inscrição da pessoa {}", pessoa.getId(), e);
        }
    }

    /**
     * Gera a ficha de cadastro de um Casal e envia como download PDF.
     */
    public void gerarFichaInscricaoCasal(Casal casal) {
        Document document = new Document();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            adicionarTitulo(document, "Ficha de Cadastro do Casal");
            adicionarFoto(document, casal.getFoto(), "/resources/images/default_casal.png");

            document.add(new Paragraph(" "));

            // Dados Pessoa 1 (Marido)
            Pessoa p1 = casal.getPessoa1();
            document.add(new Paragraph("Dados do Marido:", DADOS_FONT));
            adicionarDadosPessoa(document, p1);

            document.add(new Paragraph(" "));

            // Dados Pessoa 2 (Esposa)
            Pessoa p2 = casal.getPessoa2();
            document.add(new Paragraph("Dados da Esposa:", DADOS_FONT));
            adicionarDadosPessoa(document, p2);

            document.close();
            enviarPdfParaNavegador(baos, "Ficha_Inscricao_Casal_" + casal.getId() + ".pdf");
        } catch (Exception e) {
            logger.error("Erro ao gerar ficha de inscrição do casal {}", casal.getId(), e);
        }
    }

    /**
     * Gera a ficha de inscrição de um Encontrista e envia como download PDF.
     */
    public void gerarFichaInscricaoEncontrista(Encontrista encontrista) {
        Document document = new Document();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            adicionarTitulo(document, "Ficha de Inscrição do Seguimista");

            // Foto do encontrista (via pessoa)
            String fotoNome = (encontrista.getPessoa() != null) ? encontrista.getPessoa().getFoto() : null;
            adicionarFoto(document, fotoNome, "/resources/images/default_avatar.png");

            document.add(new Paragraph(" "));

            // Dados do Encontrista
            if (encontrista.getPessoa() != null) {
                Pessoa p = encontrista.getPessoa();
                document.add(new Paragraph("Nome: " + p.getNome(), DADOS_FONT));
                document.add(new Paragraph("CPF: " + p.getCpf(), DADOS_FONT));
                String dataNasc = "";
                if (p.getDataNascimento() != null) {
                    dataNasc = new SimpleDateFormat("dd/MM/yyyy")
                            .format(java.sql.Date.valueOf(p.getDataNascimento()));
                }
                document.add(new Paragraph("Data de Nascimento: " + dataNasc, DADOS_FONT));
                document.add(new Paragraph("Endereço: " + p.getEndereco(), DADOS_FONT));
                document.add(new Paragraph("Telefone: " + p.getTelefone(), DADOS_FONT));
                document.add(new Paragraph("E-mail: " + p.getEmail(), DADOS_FONT));
                String sexo = "Não informado";
                if (p.getSexo() != null) {
                    sexo = String.valueOf(p.getSexo()).equalsIgnoreCase("M") ? "Masculino" : "Feminino";
                }
                document.add(new Paragraph("Sexo: " + sexo, DADOS_FONT));
                document.add(new Paragraph(
                        "Idade: " + (p.getIdade() != null ? p.getIdade() : "Não calculada"), DADOS_FONT));
            }

            document.add(new Paragraph(" "));

            // Dados do Encontro
            if (encontrista.getEncontro() != null) {
                document.add(new Paragraph("Encontro: " + encontrista.getEncontro().getNome(), DADOS_FONT));
                String dataEncontro = "";
                if (encontrista.getEncontro().getDataInicio() != null) {
                    dataEncontro = new SimpleDateFormat("dd/MM/yyyy")
                            .format(java.sql.Date.valueOf(encontrista.getEncontro().getDataInicio()));
                }
                document.add(new Paragraph("Data do Encontro: " + dataEncontro, DADOS_FONT));
            }

            // Data da inscrição
            if (encontrista.getDataInscricao() != null) {
                String dataInscricao = encontrista.getDataInscricao()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                document.add(new Paragraph("Data da Inscrição: " + dataInscricao, DADOS_FONT));
            }

            // Observações
            if (encontrista.getObservacoes() != null && !encontrista.getObservacoes().trim().isEmpty()) {
                document.add(new Paragraph("Observações: " + encontrista.getObservacoes(), DADOS_FONT));
            }

            document.close();
            enviarPdfParaNavegador(baos, "Ficha_Inscricao_Encontrista_" + encontrista.getId() + ".pdf");
        } catch (Exception e) {
            logger.error("Erro ao gerar ficha de inscrição do encontrista {}", encontrista.getId(), e);
        }
    }

    /**
     * Gera relatório PDF com lista de encontristas ativos e envia como download.
     */
    public void gerarRelatorioEncontristasAtivos(List<Encontrista> encontristasAtivos) {
        Document document = new Document();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Título
            Font tituloFont16 = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph titulo = new Paragraph("Relatório de Seguimistas", tituloFont16);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new Paragraph(" "));

            // Tabela com 6 colunas
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            // Cabeçalho
            table.addCell(new PdfPCell(new Paragraph("Nome", CABECALHO_FONT)));
            table.addCell(new PdfPCell(new Paragraph("CPF", CABECALHO_FONT)));
            table.addCell(new PdfPCell(new Paragraph("Encontro", CABECALHO_FONT)));
            PdfPCell dataInscricaoHeader = new PdfPCell(new Paragraph("Data de Inscrição", CABECALHO_FONT));
            dataInscricaoHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(dataInscricaoHeader);
            PdfPCell idadeHeader = new PdfPCell(new Paragraph("Idade", CABECALHO_FONT));
            idadeHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(idadeHeader);
            PdfPCell circuloHeader = new PdfPCell(new Paragraph("Círculo", CABECALHO_FONT));
            circuloHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(circuloHeader);

            // Ordena por nome
            encontristasAtivos.sort((a, b) -> {
                String nomeA = a.getPessoa() != null && a.getPessoa().getNome() != null ? a.getPessoa().getNome() : "";
                String nomeB = b.getPessoa() != null && b.getPessoa().getNome() != null ? b.getPessoa().getNome() : "";
                return nomeA.compareToIgnoreCase(nomeB);
            });

            // Dados
            for (Encontrista e : encontristasAtivos) {
                table.addCell(new PdfPCell(
                        new Paragraph(e.getPessoa() != null ? e.getPessoa().getNome() : "", DADOS_TABELA_FONT)));
                table.addCell(new PdfPCell(
                        new Paragraph(e.getPessoa() != null ? e.getPessoa().getCpf() : "", DADOS_TABELA_FONT)));
                table.addCell(new PdfPCell(
                        new Paragraph(e.getEncontro() != null ? e.getEncontro().getNome() : "", DADOS_TABELA_FONT)));

                String dataInscricao = e.getDataInscricao() != null
                        ? e.getDataInscricao().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "";
                PdfPCell dataInscricaoCell = new PdfPCell(new Paragraph(dataInscricao, DADOS_TABELA_FONT));
                dataInscricaoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(dataInscricaoCell);

                PdfPCell idadeCell = new PdfPCell(
                        new Paragraph(e.getIdade() != null ? e.getIdade().toString() : "", DADOS_TABELA_FONT));
                idadeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(idadeCell);

                PdfPCell circuloCell = new PdfPCell(
                        new Paragraph(e.getCirculo() != null ? e.getCirculo().name() : "", DADOS_TABELA_FONT));
                circuloCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(circuloCell);
            }

            document.add(table);
            document.close();
            enviarPdfParaNavegador(baos, "Relatorio_Seguimistas_Ativos.pdf");
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de encontristas ativos", e);
        }
    }

    // ==================== Métodos utilitários ====================

    /**
     * Adiciona um título centralizado ao documento PDF.
     */
    private void adicionarTitulo(Document document, String textoTitulo) throws Exception {
        Paragraph titulo = new Paragraph(textoTitulo, TITULO_FONT);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph(" "));
    }

    /**
     * Adiciona uma foto ao documento PDF. Se não existir, exibe mensagem.
     */
    private void adicionarFoto(Document document, String fotoNome, String imagemPadrao) throws Exception {
        String caminhoFoto;
        if (fotoNome != null) {
            caminhoFoto = CAMINHO_FOTOS + fotoNome;
        } else {
            caminhoFoto = FacesContext.getCurrentInstance().getExternalContext().getRealPath(imagemPadrao);
        }
        File fotoFile = new File(caminhoFoto);
        if (fotoFile.exists()) {
            Image foto = Image.getInstance(caminhoFoto);
            foto.scaleToFit(150, 150);
            foto.setAlignment(Element.ALIGN_CENTER);
            document.add(foto);
        } else {
            document.add(new Paragraph("Foto não encontrada."));
        }
        document.add(new Paragraph(" "));
    }

    /**
     * Adiciona dados básicos de uma pessoa ao documento PDF.
     */
    private void adicionarDadosPessoa(Document document, Pessoa pessoa) throws Exception {
        document.add(new Paragraph("Nome: " + pessoa.getNome(), DADOS_FONT));
        document.add(new Paragraph("CPF: " + pessoa.getCpf(), DADOS_FONT));
        document.add(new Paragraph("Data de Nascimento: " + pessoa.getDataNascimento(), DADOS_FONT));
        document.add(new Paragraph("Endereço: " + pessoa.getEndereco(), DADOS_FONT));
        document.add(new Paragraph("Telefone: " + pessoa.getTelefone(), DADOS_FONT));
        document.add(new Paragraph("E-mail: " + pessoa.getEmail(), DADOS_FONT));
        document.add(new Paragraph(
                "Sexo: " + (pessoa.getSexo() != null ? pessoa.getSexo() : "Não informado"), DADOS_FONT));
        document.add(new Paragraph(
                "Idade: " + (pessoa.getIdade() != null ? pessoa.getIdade() : "Não calculada"), DADOS_FONT));
    }

    /**
     * Envia o PDF gerado como download para o navegador.
     */
    private void enviarPdfParaNavegador(ByteArrayOutputStream baos, String nomeArquivo) throws Exception {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.reset();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + nomeArquivo);
        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().flush();
        facesContext.responseComplete();
    }
}
