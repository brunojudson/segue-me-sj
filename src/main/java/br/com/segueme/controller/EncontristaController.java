package br.com.segueme.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import br.com.segueme.entity.Encontrista;
import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.enums.Circulo;
import br.com.segueme.service.EncontristaService;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.PessoaService;

@Named
@ViewScoped
public class EncontristaController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EncontristaService encontristaService;

    @Inject
    private PessoaService pessoaService;

    @Inject
    private EncontroService encontroService;

    private List<Encontrista> encontristas;
    private Encontrista encontrista;
    private Encontrista encontristaSelecionado;

    private List<Pessoa> pessoas;
    private List<Encontro> encontros;

    private static final String CAMINHO_FOTOS = "C:\\Desenvovilmento\\fotos\\";

    @PostConstruct
    public void init() {
        carregarEncontristas();
        carregarPessoas();
        carregarEncontros();
        limpar();
    }

    public void carregarEncontristas() {
        encontristas = encontristaService.buscarTodos();
    }

    public void carregarPessoas() {
        pessoas = pessoaService.buscarTodos();
    }

    public void carregarEncontros() {
        encontros = encontroService.buscarAtivos();
    }

    public void limpar() {
        encontrista = new Encontrista();
    }

    public String salvar() {
        try {
            if (encontrista.getId() == null) {
                encontristaService.salvar(encontrista);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Seguimista cadastrado com sucesso!"));
            } else {
                encontristaService.atualizar(encontrista);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Seguimista atualizado com sucesso!"));
            }

            carregarEncontristas();
            limpar();
            return "lista?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
            return null;
        }
    }
    
    public String salvarEContinuar() {
        try {
            if (encontrista.getId() == null) {
                encontristaService.salvar(encontrista);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Seguimista cadastrado com sucesso!"));
            } else {
                encontristaService.atualizar(encontrista);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Seguimista atualizado com sucesso!"));
            }

            carregarEncontristas();
            limpar();
            return "";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
            return null;
        }
    }

    public String visualizar(Encontrista encontrista) {
        this.encontristaSelecionado = encontrista;
        return "visualizar?faces-redirect=true&id=" + encontrista.getId();
    }

    public String editar(Encontrista encontrista) {
        this.encontrista = encontrista;
        return "cadastro?faces-redirect=true&id=" + encontrista.getId();
    }

    public void prepararExclusao(Encontrista encontrista) {
        this.encontristaSelecionado = encontrista;
    }

    public void excluir() {
        try {
            encontristaService.remover(encontristaSelecionado.getId());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Seguimista excluído com sucesso!"));
            carregarEncontristas();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
    }

    public void carregarEncontrista() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null && !idParam.isEmpty()) {
            Long id = Long.valueOf(idParam);
            encontristaService.buscarPorId(id).ifPresent(e -> this.encontrista = e);
        }
    }

    public String getFoto() {
        if (encontrista.getPessoa() != null && encontrista.getPessoa().getFoto() != null
                && !encontrista.getPessoa().getFoto().isEmpty()) {
            // Adiciona um parâmetro único (timestamp) para evitar cache
            return "/fotos/" + encontrista.getPessoa().getFoto() + "?t=" + System.currentTimeMillis();
        }
        // Retorna uma imagem padrão caso o casal não tenha foto
        return "/resources/images/default_avatar.png?t=" + System.currentTimeMillis();
    }

    // ...existing code...
    public void gerarFichaInscricao(Encontrista encontrista) {

        Document document = new Document();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Título
            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph titulo = new Paragraph("Ficha de Inscrição do Seguimista", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(new Paragraph(" ")); // Espaço

            // Foto
            String caminhoFoto;
            if (encontrista.getPessoa() != null && encontrista.getPessoa().getFoto() != null) {
                caminhoFoto = CAMINHO_FOTOS + encontrista.getPessoa().getFoto();
            } else {
                caminhoFoto = FacesContext.getCurrentInstance().getExternalContext()
                        .getRealPath("/resources/images/default_avatar.png");
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

            document.add(new Paragraph(" ")); // Espaço

            // Dados do Encontrista
            Font dadosFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            if (encontrista.getPessoa() != null) {
                document.add(new Paragraph("Nome: " + encontrista.getPessoa().getNome(), dadosFont));
                document.add(new Paragraph("CPF: " + encontrista.getPessoa().getCpf(), dadosFont));
                // Data de nascimento formatada
                String dataNasc = "";
                if (encontrista.getPessoa().getDataNascimento() != null) {
                    dataNasc = new SimpleDateFormat("dd/MM/yyyy")
                            .format(java.sql.Date.valueOf(encontrista.getPessoa().getDataNascimento()));
                }
                document.add(new Paragraph("Data de Nascimento: " + dataNasc, dadosFont));
                document.add(new Paragraph("Endereço: " + encontrista.getPessoa().getEndereco(), dadosFont));
                document.add(new Paragraph("Telefone: " + encontrista.getPessoa().getTelefone(), dadosFont));
                document.add(new Paragraph("E-mail: " + encontrista.getPessoa().getEmail(), dadosFont));
                String sexo = "Não informado";
                if (encontrista.getPessoa().getSexo() != null) {
                    sexo = String.valueOf(encontrista.getPessoa().getSexo()).equalsIgnoreCase("M") ? "Masculino"
                            : "Feminino";
                }
                document.add(new Paragraph("Sexo: " + sexo, dadosFont));
                document.add(new Paragraph(
                        "Idade: " + (encontrista.getPessoa().getIdade() != null ? encontrista.getPessoa().getIdade()
                                : "Não calculada"),
                        dadosFont));
            }

            document.add(new Paragraph(" ")); // Espaço

            // Dados do Encontro
            if (encontrista.getEncontro() != null) {
                document.add(new Paragraph("Encontro: " + encontrista.getEncontro().getNome(), dadosFont));
                // Data do encontro formatada
                String dataEncontro = "";
                if (encontrista.getEncontro().getDataInicio() != null) {
                    dataEncontro = new SimpleDateFormat("dd/MM/yyyy")
                            .format(java.sql.Date.valueOf(encontrista.getEncontro().getDataInicio()));
                }
                document.add(new Paragraph("Data do Encontro: " + dataEncontro, dadosFont));
            }

            // Data da inscrição formatada
            if (encontrista.getDataInscricao() != null) {
                String dataInscricao = encontrista.getDataInscricao()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                document.add(new Paragraph("Data da Inscrição: " + dataInscricao, dadosFont));
            }

            // Observações
            if (encontrista.getObservacoes() != null && !encontrista.getObservacoes().trim().isEmpty()) {
                document.add(new Paragraph("Observações: " + encontrista.getObservacoes(), dadosFont));
            }

            document.close();

            // Enviar PDF para o navegador
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            response.reset();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=Ficha_Inscricao_Encontrista_" + encontrista.getId() + ".pdf");
            response.getOutputStream().write(baos.toByteArray());
            response.getOutputStream().flush();
            facesContext.responseComplete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void gerarRelatorioEncontristasAtivos() {
        Document document = new Document();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Título
            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph titulo = new Paragraph("Relatório de Seguimistas", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new Paragraph(" ")); // Espaço

            // Tabela (agora com 6 colunas)
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            // Cabeçalho
            Font cabecalhoFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            table.addCell(new PdfPCell(new Paragraph("Nome", cabecalhoFont)));
            table.addCell(new PdfPCell(new Paragraph("CPF", cabecalhoFont)));
            table.addCell(new PdfPCell(new Paragraph("Encontro", cabecalhoFont)));
            PdfPCell dataInscricaoHeader = new PdfPCell(new Paragraph("Data de Inscrição", cabecalhoFont));
            dataInscricaoHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(dataInscricaoHeader);
            PdfPCell idadeHeader = new PdfPCell(new Paragraph("Idade", cabecalhoFont));
            idadeHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(idadeHeader);
            PdfPCell circuloHeader = new PdfPCell(new Paragraph("Círculo", cabecalhoFont));
            circuloHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(circuloHeader);

            // Dados
            List<Encontrista> ativos = encontristaService.buscarAtivos();
            // Ordena por nome
            ativos.sort((a, b) -> {
                String nomeA = a.getPessoa() != null && a.getPessoa().getNome() != null ? a.getPessoa().getNome() : "";
                String nomeB = b.getPessoa() != null && b.getPessoa().getNome() != null ? b.getPessoa().getNome() : "";
                return nomeA.compareToIgnoreCase(nomeB);
            });

            Font dadosFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);

            for (Encontrista e : ativos) {
                table.addCell(
                        new PdfPCell(new Paragraph(e.getPessoa() != null ? e.getPessoa().getNome() : "", dadosFont)));
                table.addCell(
                        new PdfPCell(new Paragraph(e.getPessoa() != null ? e.getPessoa().getCpf() : "", dadosFont)));
                table.addCell(new PdfPCell(
                        new Paragraph(e.getEncontro() != null ? e.getEncontro().getNome() : "", dadosFont)));
                String dataInscricao = e.getDataInscricao() != null
                        ? e.getDataInscricao().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "";
                PdfPCell dataInscricaoCell = new PdfPCell(new Paragraph(dataInscricao, dadosFont));
                dataInscricaoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(dataInscricaoCell);

                PdfPCell idadeCell = new PdfPCell(
                        new Paragraph(e.getIdade() != null ? e.getIdade().toString() : "", dadosFont));
                idadeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(idadeCell);

                // Adiciona o círculo (cor)
                PdfPCell circuloCell = new PdfPCell(
                        new Paragraph(e.getCirculo() != null ? e.getCirculo().name() : "", dadosFont));
                circuloCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(circuloCell);
            }

            document.add(table);
            document.close();

            // Enviar PDF para o navegador
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            response.reset();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Relatorio_Seguimistas_Ativos.pdf");
            response.getOutputStream().write(baos.toByteArray());
            response.getOutputStream().flush();
            facesContext.responseComplete();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao gerar relatório", e.getMessage()));
        }
    }

    public Circulo[] getCirculos() {
        return Circulo.values();
    }
    // ...existing code...
    // Getters e Setters

    public List<Encontrista> getEncontristas() {
        return encontristas;
    }

    public void setEncontristas(List<Encontrista> encontristas) {
        this.encontristas = encontristas;
    }

    public Encontrista getEncontrista() {
        return encontrista;
    }

    public void setEncontrista(Encontrista encontrista) {
        this.encontrista = encontrista;
    }

    public Encontrista getEncontristaSelecionado() {
        return encontristaSelecionado;
    }

    public void setEncontristaSelecionado(Encontrista encontristaSelecionado) {
        this.encontristaSelecionado = encontristaSelecionado;
    }

    public List<Pessoa> getPessoas() {
        return pessoas;
    }

    public void setPessoas(List<Pessoa> pessoas) {
        this.pessoas = pessoas;
    }

    public List<Encontro> getEncontros() {
        return encontros;
    }

    public void setEncontros(List<Encontro> encontros) {
        this.encontros = encontros;
    }
}
