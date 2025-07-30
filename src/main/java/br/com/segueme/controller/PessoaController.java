package br.com.segueme.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import br.com.segueme.entity.Pessoa;
import br.com.segueme.enums.Escolaridade;
import br.com.segueme.enums.Sacramento;
import br.com.segueme.service.PessoaService;

@Named
@ViewScoped
public class PessoaController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private PessoaService pessoaService;

	private List<Pessoa> pessoas;
	private Pessoa pessoa;
	private Pessoa pessoaSelecionada;

	private boolean processandoAtualizacao = false;
	private boolean idadesAtualizadas = false;

	private List<Sacramento> sacramentosSelecionados;

	private Long pessoaIdParaCarregar;

	private UploadedFile uploadedFile;
	private String fotoPreview;

	private static final String CAMINHO_FOTOS = "C:\\Desenvovilmento\\fotos\\";

	@PostConstruct
	public void init() {
		carregarPessoas();
		limpar();
	}

	public String getFoto() {
		if (pessoa != null && pessoa.getFoto() != null && !pessoa.getFoto().isEmpty()) {
			// Adiciona um parâmetro único (timestamp) para evitar cache
			return "/fotos/" + pessoa.getFoto() + "?t=" + System.currentTimeMillis();
		}
		// Retorna uma imagem padrão caso o casal não tenha foto
		return "/resources/images/default_avatar.png?t=" + System.currentTimeMillis();
	}

	// PessoaController.java
	public void atualizarIdades() {
		processandoAtualizacao = true;
		idadesAtualizadas = false;
		try {
			pessoaService.atualizarIdades();
			carregarPessoas();
			idadesAtualizadas = true;
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Idades atualizadas com sucesso!", null));
			// Passa parâmetro para dialog fechar
			PrimeFaces.current().ajax().addCallbackParam("atualizado", true);
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao atualizar idades!", null));
			PrimeFaces.current().ajax().addCallbackParam("atualizado", false);
		} finally {
			processandoAtualizacao = false;
		}
	}

	public void carregarPessoas() {
		pessoas = pessoaService.buscarTodos();
	}

	public void limpar() {
		pessoa = new Pessoa();
	}

	public String salvar() {
		if (pessoa.getSacramentos() == null) { // Segurança extra, mas Pessoa deve cuidar disso
			pessoa.setSacramentos(new ArrayList<>());
		}
		pessoa.setSacramentos(new ArrayList<>(this.sacramentosSelecionados));

		try {
			// Verificar se há uma nova foto para upload
			if (uploadedFile != null) {
				String nomeArquivo = gerarNomeArquivo();
				String caminhoArquivo = CAMINHO_FOTOS + "" + nomeArquivo;

				// Apagar a foto anterior, se existir
				if (pessoa.getFoto() != null) {
					File fotoAnterior = new File(CAMINHO_FOTOS + "" + pessoa.getFoto());
					if (fotoAnterior.exists()) {
						fotoAnterior.delete();
					}
				}
				// Salvar o novo arquivo no diretório
				Files.copy(uploadedFile.getInputStream(), Paths.get(caminhoArquivo),
						StandardCopyOption.REPLACE_EXISTING);

				// Atualizar o nome do arquivo no objeto casal
				pessoa.setFoto(nomeArquivo);
			}
			if (pessoa.getId() == null) {

				pessoaService.salvar(pessoa);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pessoa cadastrada com sucesso!"));
			} else {

				pessoaService.atualizar(pessoa);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pessoa atualizada com sucesso!"));
			}

			carregarPessoas();
			limpar();
			return "lista?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
			return null;
		}
	}
	
	public String salvarEContinuar() {
		if (pessoa.getSacramentos() == null) { // Segurança extra, mas Pessoa deve cuidar disso
			pessoa.setSacramentos(new ArrayList<>());
		}
		pessoa.setSacramentos(new ArrayList<>(this.sacramentosSelecionados));

		try {
			// Verificar se há uma nova foto para upload
			if (uploadedFile != null) {
				String nomeArquivo = gerarNomeArquivo();
				String caminhoArquivo = CAMINHO_FOTOS + "" + nomeArquivo;

				// Apagar a foto anterior, se existir
				if (pessoa.getFoto() != null) {
					File fotoAnterior = new File(CAMINHO_FOTOS + "" + pessoa.getFoto());
					if (fotoAnterior.exists()) {
						fotoAnterior.delete();
					}
				}
				// Salvar o novo arquivo no diretório
				Files.copy(uploadedFile.getInputStream(), Paths.get(caminhoArquivo),
						StandardCopyOption.REPLACE_EXISTING);

				// Atualizar o nome do arquivo no objeto casal
				pessoa.setFoto(nomeArquivo);
			}
			if (pessoa.getId() == null) {

				pessoaService.salvar(pessoa);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pessoa cadastrada com sucesso!"));
			} else {

				pessoaService.atualizar(pessoa);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pessoa atualizada com sucesso!"));
			}

			carregarPessoas();
			limpar();
			return "";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
			return null;
		}
	}


	private String gerarNomeArquivo() {
		String nome = pessoa.getNome();
		long timestamp = System.currentTimeMillis(); // Gera o timestamp atual
		return nome.replaceAll("\\s+", "_") + "_" + timestamp + ".jpg";
	}

	public String visualizar(Pessoa pessoa) {
		this.pessoaSelecionada = pessoa;
		return "visualizar?faces-redirect=true&id=" + pessoa.getId();
	}

	public String editar(Pessoa pessoa) {
		this.pessoa = pessoa;
		return "cadastro?faces-redirect=true&id=" + pessoa.getId();
	}

	public void prepararExclusao(Pessoa pessoa) {
		this.pessoaSelecionada = pessoa;
	}

	public void excluir() {
		try {
			// Excluir o arquivo associado à pessoa, se existir
			if (pessoaSelecionada.getFoto() != null) {
				File arquivoFoto = new File(CAMINHO_FOTOS + "" + pessoaSelecionada.getFoto());
				if (arquivoFoto.exists()) {
					boolean deletado = arquivoFoto.delete();
					if (!deletado) {
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
								"Aviso", "Não foi possível excluir a foto da pessoa."));
					}
				}
			}
			pessoaService.remover(pessoaSelecionada.getId());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pessoa excluída com sucesso!"));
			carregarPessoas();
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public void carregarPessoa() {
		String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		if (idParam != null && !idParam.isEmpty()) {
			try {
				Long id = Long.valueOf(idParam);
				pessoaService.buscarPorId(id).ifPresent(p -> {
					this.pessoa = p;
					// Popula sacramentosSelecionados com os sacramentos da pessoa carregada
					if (this.pessoa.getSacramentos() != null) {
						this.sacramentosSelecionados = new ArrayList<>(this.pessoa.getSacramentos());
					} else {
						this.sacramentosSelecionados = new ArrayList<>();
					}
					// Se houver foto, prepara para exibição (se necessário, mas seu getFoto() já
					// lida com isso)
				});
				if (this.pessoa == null) {
					// Pessoa não encontrada, talvez adicionar uma mensagem ou redirecionar
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Pessoa não encontrada."));
					// limpar(); // Opcional: limpar campos se a pessoa não for encontrada
				}
			} catch (NumberFormatException e) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "ID inválido para carregar pessoa."));
				limpar(); // Limpa os campos se o ID for inválido
			}
		} else {
			// Nenhum ID fornecido, provavelmente é um novo cadastro
			limpar(); // Garante que tudo está limpo para um novo cadastro
		}
	}

	public void gerarFichaInscricao(Pessoa pessoa) {
		Document document = new Document();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter.getInstance(document, baos);
			document.open();

			// Título
			Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
			Paragraph titulo = new Paragraph("Ficha de Inscrição", tituloFont);
			titulo.setAlignment(Element.ALIGN_CENTER);
			document.add(titulo);

			document.add(new Paragraph(" ")); // Espaço

			// Foto
			String caminhoFoto;
			if (pessoa.getFoto() != null) {
				caminhoFoto = CAMINHO_FOTOS + pessoa.getFoto();
			} else {
				// Caminho para o avatar padrão (ajuste conforme o local do arquivo em seu
				// projeto)
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

			// Dados da Pessoa
			Font dadosFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
			document.add(new Paragraph("Nome: " + pessoa.getNome(), dadosFont));
			document.add(new Paragraph("CPF: " + pessoa.getCpf(), dadosFont));
			document.add(new Paragraph("Data de Nascimento: " + pessoa.getDataNascimento(), dadosFont));
			document.add(new Paragraph("Endereço: " + pessoa.getEndereco(), dadosFont));
			document.add(new Paragraph("Telefone: " + pessoa.getTelefone(), dadosFont));
			document.add(new Paragraph("E-mail: " + pessoa.getEmail(), dadosFont));
			document.add(new Paragraph("Sexo: " + (pessoa.getSexo() != null ? pessoa.getSexo() : "Não informado"),
					dadosFont));
			document.add(new Paragraph("Idade: " + (pessoa.getIdade() != null ? pessoa.getIdade() : "Não calculada"),
					dadosFont));

			document.close();

			// Enviar PDF para o navegador
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			response.reset();
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition",
					"attachment; filename=Ficha_Inscricao_" + pessoa.getId() + ".pdf");
			response.getOutputStream().write(baos.toByteArray());
			response.getOutputStream().flush();
			facesContext.responseComplete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void handleFileUpload(FileUploadEvent event) { // Ou apenas (UploadedFile file) se não usar FileUploadEvent
        this.uploadedFile = event.getFile(); // Se usar FileUploadEvent
        // Se não usar FileUploadEvent, o PrimeFaces pode injetar diretamente em um atributo UploadedFile
        // desde que o listener aponte para um método que receba UploadedFile.
        // Para o preview imediato, você pode converter para Data URL:
        if (this.uploadedFile != null && this.uploadedFile.getSize() > 0) {
            try {
                byte[] bytes = this.uploadedFile.getContent();
                String base64Image = Base64.getEncoder().encodeToString(bytes);
                this.fotoPreview = "data:" + this.uploadedFile.getContentType() + ";base64," + base64Image;
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", event.getFile().getFileName() + " carregado."));
            } catch (Exception e) { // Mude para IOException se getContent() lançar apenas isso
                this.fotoPreview = null;
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível processar a imagem para preview."));
            }
        } else {
            this.fotoPreview = null; // Limpa o preview se nenhum arquivo for selecionado ou for inválido
        }
        // Não é necessário chamar PrimeFaces.current().ajax().update() aqui se o p:fileUpload já tem o update.
    }
	
	// Autocomplete para nomes de pessoas (pai/mãe)
	public List<String> autocompleteNomePessoa(String query) {
		List<String> nomes = new ArrayList<>();
		for (Pessoa p : pessoaService.buscarTodos()) {
			if (p.getNome() != null && p.getNome().toLowerCase().contains(query.toLowerCase())) {
				nomes.add(p.getNome());
			}
		}
		return nomes;
	}
	public Sacramento[] getTodosSacramentos() {
		return Sacramento.values();
	}

	public Long getPessoaIdParaCarregar() {
		return pessoaIdParaCarregar;
	}

	public String getFotoPreview() {
		return fotoPreview;
	}

	public void setFotoPreview(String fotoPreview) {
		this.fotoPreview = fotoPreview;
	}

	public void setPessoaIdParaCarregar(Long pessoaIdParaCarregar) {
		this.pessoaIdParaCarregar = pessoaIdParaCarregar;
	}

	// Getters e Setters
	public boolean isProcessandoAtualizacao() {
		return processandoAtualizacao;
	}

	public boolean isIdadesAtualizadas() {
		return idadesAtualizadas;
	}

	public List<Pessoa> getPessoas() {
		return pessoas;
	}

	public void setPessoas(List<Pessoa> pessoas) {
		this.pessoas = pessoas;
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public Pessoa getPessoaSelecionada() {
		return pessoaSelecionada;
	}

	public void setPessoaSelecionada(Pessoa pessoaSelecionada) {
		this.pessoaSelecionada = pessoaSelecionada;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public List<Sacramento> getSacramentosSelecionados() {
		if (sacramentosSelecionados == null) { // Garantia extra
			sacramentosSelecionados = new ArrayList<>();
		}
		return sacramentosSelecionados;
	}

	public Escolaridade[] getTodasEscolaridades() {
		return Escolaridade.values();
	}

	public void setSacramentosSelecionados(List<Sacramento> sacramentosSelecionados) {
		this.sacramentosSelecionados = sacramentosSelecionados;
	}

}
