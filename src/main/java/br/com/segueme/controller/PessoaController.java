package br.com.segueme.controller;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.segueme.entity.Casal;
import br.com.segueme.entity.Encontrista;
import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Palestra;
import br.com.segueme.entity.Palestrante;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.entity.Trabalhador;
import br.com.segueme.enums.Escolaridade;
import br.com.segueme.enums.Sacramento;
import br.com.segueme.service.CasalService;
import br.com.segueme.service.EncontristaService;
import br.com.segueme.service.PalestranteService;
import br.com.segueme.service.PdfService;
import br.com.segueme.service.PessoaService;
import br.com.segueme.service.TrabalhadorService;

@Named
@ViewScoped
public class PessoaController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(PessoaController.class);

	@Inject
	private PessoaService pessoaService;

	@Inject
	private PdfService pdfService;

	@Inject
	private EncontristaService encontristaService;

	@Inject
	private TrabalhadorService trabalhadorService;

	@Inject
	private PalestranteService palestranteService;

	@Inject
	private CasalService casalService;

	private List<Pessoa> pessoas;
	private Pessoa pessoa;
	private Pessoa pessoaSelecionada;

	private boolean processandoAtualizacao = false;
	private boolean idadesAtualizadas = false;

	// Modal detalhes
	private Pessoa pessoaDetalhes;
	private List<Encontro> encontrosParticipados;
	private List<Trabalhador> equipesTrabalhou;
	private List<Palestra> palestrasRealizadas;
	private boolean detalhesModalVisible = false;

	private List<Sacramento> sacramentosSelecionados;

	private Long pessoaIdParaCarregar;

	private UploadedFile uploadedFile;
	private String fotoPreview;

	private static final String CAMINHO_FOTOS = System.getProperty("caminho_fotos", "C:\\Desenvolvimento\\fotos") + java.io.File.separator;

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
		return salvarInterno(true);
	}
	
	public String salvarEContinuar() {
		return salvarInterno(false);
	}

	private String salvarInterno(boolean redirecionarParaLista) {
		System.out.println(CAMINHO_FOTOS);
		if (pessoa.getSacramentos() == null) {
			pessoa.setSacramentos(new ArrayList<>());
		}
		pessoa.setSacramentos(new ArrayList<>(this.sacramentosSelecionados));

		try {
			// Verificar se há uma nova foto para upload
			if (uploadedFile != null) {
				String nomeArquivo = gerarNomeArquivo();
				String caminhoArquivo = CAMINHO_FOTOS + nomeArquivo;

				// Apagar a foto anterior, se existir
				if (pessoa.getFoto() != null) {
					File fotoAnterior = new File(CAMINHO_FOTOS + pessoa.getFoto());
					if (fotoAnterior.exists()) {
						fotoAnterior.delete();
					}
				}
				// Salvar o novo arquivo no diretório
				Files.copy(uploadedFile.getInputStream(), Paths.get(caminhoArquivo),
						StandardCopyOption.REPLACE_EXISTING);

				// Atualizar o nome do arquivo no objeto pessoa
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
			return redirecionarParaLista ? "lista?faces-redirect=true" : "";
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
		pdfService.gerarFichaInscricaoPessoa(pessoa);
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

	// Modal de detalhes da pessoa
	public void abrirDetalhes(Pessoa pessoa) {
		
		this.pessoaDetalhes = pessoaService.buscarPorId(pessoa.getId()).orElse(pessoa);

		// Equipes
		try {
			this.equipesTrabalhou = trabalhadorService.buscarPorPessoa(pessoa.getId());
		} catch (Exception e) {
			log.error("Erro ao carregar equipes para pessoa {}", pessoa.getId(), e);
			this.equipesTrabalhou = new ArrayList<>();
		}

		// Encontros (agrega de encontrista + trabalhador)
		try {
			LinkedHashSet<Encontro> encontrosSet = new LinkedHashSet<>();
			List<Encontrista> encontristas = encontristaService.buscarPorPessoa(pessoa.getId());
			for (Encontrista enc : encontristas) {
				if (enc.getEncontro() != null) {
					encontrosSet.add(enc.getEncontro());
				}
			}
			if (this.equipesTrabalhou != null) {
				for (Trabalhador trab : this.equipesTrabalhou) {
					if (trab.getEncontro() != null) {
						encontrosSet.add(trab.getEncontro());
					}
				}
			}
			this.encontrosParticipados = new ArrayList<>(encontrosSet);
		} catch (Exception e) {
			log.error("Erro ao carregar encontros para pessoa {}", pessoa.getId(), e);
			this.encontrosParticipados = new ArrayList<>();
		}

		// Palestras
		List<Palestra> todasPalestras = new ArrayList<>();
		try {
			List<Palestrante> palestrantesIndividual = palestranteService.buscarPorPessoa(this.pessoaDetalhes);
			for (Palestrante p : palestrantesIndividual) {
				todasPalestras.addAll(p.getPalestras());
			}

			List<Casal> casais = casalService.buscarPorPessoa(pessoa.getId());
			for (Casal casal : casais) {
				List<Palestrante> palestrantesCasal = palestranteService.buscarPorCasal(casal);
				for (Palestrante p : palestrantesCasal) {
					for (Palestra pal : p.getPalestras()) {
						if (!todasPalestras.contains(pal)) {
							todasPalestras.add(pal);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Erro ao carregar palestras para pessoa {}", pessoa.getId(), e);
		}

		this.palestrasRealizadas = todasPalestras;
		this.detalhesModalVisible = true;
		
	}

	public void fecharDetalhes() {
		this.detalhesModalVisible = false;
		this.pessoaDetalhes = null;
		this.encontrosParticipados = null;
		this.equipesTrabalhou = null;
		this.palestrasRealizadas = null;
	}

	public String getFotoDetalhes() {
		if (pessoaDetalhes != null && pessoaDetalhes.getFoto() != null && !pessoaDetalhes.getFoto().isEmpty()) {
			return "/fotos/" + pessoaDetalhes.getFoto() + "?t=" + System.currentTimeMillis();
		}
		return null;
	}

	public Pessoa getPessoaDetalhes() {
		return pessoaDetalhes;
	}

	public List<Encontro> getEncontrosParticipados() {
		return encontrosParticipados;
	}

	public List<Trabalhador> getEquipesTrabalhou() {
		return equipesTrabalhou;
	}

	public List<Palestra> getPalestrasRealizadas() {
		return palestrasRealizadas;
	}

	public boolean isDetalhesModalVisible() {
		return detalhesModalVisible;
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
