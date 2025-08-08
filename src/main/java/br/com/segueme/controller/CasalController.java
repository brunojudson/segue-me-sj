package br.com.segueme.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.primefaces.model.file.UploadedFile;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import br.com.segueme.entity.Casal;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.service.CasalService;
import br.com.segueme.service.PessoaService;

@Named
@ViewScoped
public class CasalController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CasalService casalService;
	private static final String CAMINHO_FOTOS = "C:\\Desenvovilmento\\fotos\\";
	@Inject
	private PessoaService pessoaService;

	private List<Casal> casais;
	private Casal casal;
	private Casal casalSelecionado;

	private List<Pessoa> pessoasMasculinas;
	private List<Pessoa> pessoasFemininas;

	private UploadedFile uploadedFile;

	@PostConstruct
	public void init() {
		carregarCasais();
		carregarPessoas();
		limpar();
	}

	public void carregarCasais() {
		casais = casalService.buscarTodos();
	}

	public String getFoto() {
		if (casal != null && casal.getFoto() != null && !casal.getFoto().isEmpty()) {
			// Adiciona um parâmetro único (timestamp) para evitar cache
			return "/fotos/" + casal.getFoto() + "?t=" + System.currentTimeMillis();
		}
		// Retorna uma imagem padrão caso o casal não tenha foto
		return "/resources/images/default_casal.png?t=" + System.currentTimeMillis();
	}

	public void carregarPessoas() {
		// Buscar todas as pessoas
		List<Pessoa> todasPessoas = pessoaService.buscarTodos();

		// Obter IDs das pessoas já associadas a casais
		/*
		 * List<Long> idsPessoasEmCasais = casais.stream() .flatMap(c ->
		 * List.of(c.getPessoa1().getId(), c.getPessoa2().getId()).stream())
		 * .collect(Collectors.toList());
		 */

		// Filtrar pessoas que não estão associadas a nenhum casal
		pessoasMasculinas = todasPessoas.stream().filter(p -> p.getSexo() != null && p.getSexo() == 'M') // Filtrar por
																											// sexo
																											// masculino
				// .filter(p -> !idsPessoasEmCasais.contains(p.getId())) // Excluir pessoas já
				// em casais
				.sorted((p1, p2) -> p1.getNome().compareToIgnoreCase(p2.getNome())) // Ordenar por nome
				.collect(Collectors.toList());

		pessoasFemininas = todasPessoas.stream().filter(p -> p.getSexo() != null && p.getSexo() == 'F') // Filtrar por
																										// sexo feminino
				// .filter(p -> !idsPessoasEmCasais.contains(p.getId())) // Excluir pessoas já
				// em casais
				.sorted((p1, p2) -> p1.getNome().compareToIgnoreCase(p2.getNome())) // Ordenar por nome
				.collect(Collectors.toList());
	}

	/*
	 * public void carregarPessoas() { List<Pessoa> todasPessoas =
	 * pessoaService.buscarTodos();
	 * 
	 * // Filtrar pessoas por sexo pessoasMasculinas =
	 * todasPessoas.stream().filter(p -> p.getSexo() != null && p.getSexo() == 'M')
	 * // Comparação // com // Character .collect(Collectors.toList());
	 * 
	 * pessoasFemininas = todasPessoas.stream().filter(p -> p.getSexo() != null &&
	 * p.getSexo() == 'F') // Comparação // com Character
	 * .collect(Collectors.toList()); }
	 */

	public void limpar() {
		casal = new Casal();
	}

	public String salvar() {
		try {
			// Verificar se há uma nova foto para upload
			if (uploadedFile != null) {
				String nomeArquivo = gerarNomeArquivo();
				String caminhoArquivo = CAMINHO_FOTOS + "" + nomeArquivo;

				// Apagar a foto anterior, se existir
				if (casal.getFoto() != null) {
					File fotoAnterior = new File(CAMINHO_FOTOS + "" + casal.getFoto());
					if (fotoAnterior.exists()) {
						fotoAnterior.delete();
					}
				}
				// Salvar o novo arquivo no diretório
				Files.copy(uploadedFile.getInputStream(), Paths.get(caminhoArquivo),
						StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Arquivo salvo em: " + caminhoArquivo);

				// Atualizar o nome do arquivo no objeto casal
				casal.setFoto(nomeArquivo);
			}

			// Salvar ou atualizar o casal no banco de dados
			if (casal.getId() == null) {
				casalService.salvar(casal);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Casal cadastrado com sucesso!"));
			} else {
				casalService.atualizar(casal);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Casal atualizado com sucesso!"));
			}

			carregarCasais();
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
			// Verificar se há uma nova foto para upload
			if (uploadedFile != null) {
				String nomeArquivo = gerarNomeArquivo();
				String caminhoArquivo = CAMINHO_FOTOS + "" + nomeArquivo;

				// Apagar a foto anterior, se existir
				if (casal.getFoto() != null) {
					File fotoAnterior = new File(CAMINHO_FOTOS + "" + casal.getFoto());
					if (fotoAnterior.exists()) {
						fotoAnterior.delete();
					}
				}
				// Salvar o novo arquivo no diretório
				Files.copy(uploadedFile.getInputStream(), Paths.get(caminhoArquivo),
						StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Arquivo salvo em: " + caminhoArquivo);

				// Atualizar o nome do arquivo no objeto casal
				casal.setFoto(nomeArquivo);
			}

			// Salvar ou atualizar o casal no banco de dados
			if (casal.getId() == null) {
				casalService.salvar(casal);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Casal cadastrado com sucesso!"));
			} else {
				casalService.atualizar(casal);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Casal atualizado com sucesso!"));
			}

			carregarCasais();
			limpar();
			return "";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
			return null;
		}
	}

	private String gerarNomeArquivo() {
		String nomeCasal = casal.getPessoa1().getNome() + "_" + casal.getPessoa2().getNome();
		long timestamp = System.currentTimeMillis(); // Gera o timestamp atual
		return nomeCasal.replaceAll("\\s+", "_") + "_" + timestamp + ".jpg";
	}

	public String visualizar(Casal casal) {
		this.casalSelecionado = casal;
		return "visualizar?faces-redirect=true&id=" + casal.getId();
	}

	public String editar(Casal casal) {
		this.casal = casal;
		return "cadastro?faces-redirect=true&id=" + casal.getId();
	}

	public void prepararExclusao(Casal casal) {
		this.casalSelecionado = casal;
	}

	public void excluir() {
		try {
			// Excluir o arquivo associado ao casal, se existir
			if (casalSelecionado.getFoto() != null) {
				File arquivoFoto = new File(CAMINHO_FOTOS + "" + casalSelecionado.getFoto());
				if (arquivoFoto.exists()) {
					boolean deletado = arquivoFoto.delete();
					if (!deletado) {
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
								"Aviso", "Não foi possível excluir a foto do casal."));
					}
				}
			}
			casalService.remover(casalSelecionado.getId());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Casal excluído com sucesso!"));
			carregarCasais();
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public void carregarCasal() {
		String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		if (idParam != null && !idParam.isEmpty()) {
			Long id = Long.valueOf(idParam);
			casalService.buscarPorId(id).ifPresent(c -> this.casal = c);
		}
	}

	public void gerarFichaInscricao(Casal casal) {
		Document document = new Document();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter.getInstance(document, baos);
			document.open();

			// Título
			Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
			Paragraph titulo = new Paragraph("Ficha de Cadastro do Casal", tituloFont);
			titulo.setAlignment(Element.ALIGN_CENTER);
			document.add(titulo);

			document.add(new Paragraph(" ")); // Espaço

			// Foto do casal
			String caminhoFoto;
			if (casal.getFoto() != null) {
				caminhoFoto = CAMINHO_FOTOS + casal.getFoto();
			} else {
				// Caminho para a imagem padrão
				caminhoFoto = FacesContext.getCurrentInstance().getExternalContext()
					.getRealPath("/resources/images/default_casal.png");
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

			// Dados Pessoa 1
			Font dadosFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
			Pessoa p1 = casal.getPessoa1();
			document.add(new Paragraph("Dados do Marido:", dadosFont));
			document.add(new Paragraph("Nome: " + p1.getNome(), dadosFont));
			document.add(new Paragraph("CPF: " + p1.getCpf(), dadosFont));
			document.add(new Paragraph("Data de Nascimento: " + p1.getDataNascimento(), dadosFont));
			document.add(new Paragraph("Endereço: " + p1.getEndereco(), dadosFont));
			document.add(new Paragraph("Telefone: " + p1.getTelefone(), dadosFont));
			document.add(new Paragraph("E-mail: " + p1.getEmail(), dadosFont));
			document.add(new Paragraph("Sexo: " + (p1.getSexo() != null ? p1.getSexo() : "Não informado"), dadosFont));
			document.add(
					new Paragraph("Idade: " + (p1.getIdade() != null ? p1.getIdade() : "Não calculada"), dadosFont));

			document.add(new Paragraph(" ")); // Espaço

			// Dados Pessoa 2
			Pessoa p2 = casal.getPessoa2();
			document.add(new Paragraph("Dados da Esposa:", dadosFont));
			document.add(new Paragraph("Nome: " + p2.getNome(), dadosFont));
			document.add(new Paragraph("CPF: " + p2.getCpf(), dadosFont));
			document.add(new Paragraph("Data de Nascimento: " + p2.getDataNascimento(), dadosFont));
			document.add(new Paragraph("Endereço: " + p2.getEndereco(), dadosFont));
			document.add(new Paragraph("Telefone: " + p2.getTelefone(), dadosFont));
			document.add(new Paragraph("E-mail: " + p2.getEmail(), dadosFont));
			document.add(new Paragraph("Sexo: " + (p2.getSexo() != null ? p2.getSexo() : "Não informado"), dadosFont));
			document.add(
					new Paragraph("Idade: " + (p2.getIdade() != null ? p2.getIdade() : "Não calculada"), dadosFont));

			document.close();

			// Enviar PDF para o navegador
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			response.reset();
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition",
					"attachment; filename=Ficha_Inscricao_Casal_" + casal.getId() + ".pdf");
			response.getOutputStream().write(baos.toByteArray());
			response.getOutputStream().flush();
			facesContext.responseComplete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// Getters e Setters

	public List<Casal> getCasais() {
		return casais;
	}

	public void setCasais(List<Casal> casais) {
		this.casais = casais;
	}

	public Casal getCasal() {
		return casal;
	}

	public void setCasal(Casal casal) {
		this.casal = casal;
	}

	public Casal getCasalSelecionado() {
		return casalSelecionado;
	}

	public void setCasalSelecionado(Casal casalSelecionado) {
		this.casalSelecionado = casalSelecionado;
	}

	public List<Pessoa> getPessoasMasculinas() {
		return pessoasMasculinas;
	}

	public void setPessoasMasculinas(List<Pessoa> pessoasMasculinas) {
		this.pessoasMasculinas = pessoasMasculinas;
	}

	public List<Pessoa> getPessoasFemininas() {
		return pessoasFemininas;
	}

	public void setPessoasFemininas(List<Pessoa> pessoasFemininas) {
		this.pessoasFemininas = pessoasFemininas;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}
}
