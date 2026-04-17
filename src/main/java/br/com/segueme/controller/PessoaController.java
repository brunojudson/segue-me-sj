package br.com.segueme.controller;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
import br.com.segueme.entity.Transferencia;
import br.com.segueme.enums.Escolaridade;
import br.com.segueme.enums.Sacramento;
import br.com.segueme.enums.StatusTransferencia;
import br.com.segueme.service.CasalService;
import br.com.segueme.service.EncontristaService;
import br.com.segueme.service.PalestranteService;
import br.com.segueme.service.PdfService;
import br.com.segueme.service.PessoaService;
import br.com.segueme.service.TrabalhadorService;
import br.com.segueme.service.TransferenciaService;

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

	@Inject
	private TransferenciaService transferenciaService;

	// Campos de transferência
	private String transferenciaParoquiaDestino;
	private String transferenciaParoquiaOrigem;
	private String transferenciaObservacoes;
	private List<Transferencia> historicoTransferencias = new ArrayList<>();

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
	private byte[] fotoBytesUpload = null;
	private String fotoContentTypeUpload = null;
	private List<Pessoa> filhosDaPessoaDetalhes = new ArrayList<>();

	private static final String CAMINHO_FOTOS = System.getProperty("caminho_fotos", "C:\\Desenvolvimento\\fotos") + java.io.File.separator;

	@PostConstruct
	public void init() {
		carregarPessoas();
		limpar();
	}

	public String getFoto() {
		if (pessoa != null && pessoa.getFoto() != null && !pessoa.getFoto().isEmpty()) {
			File f = new File(CAMINHO_FOTOS + pessoa.getFoto());
			if (f.exists()) {
				return "/fotos/" + pessoa.getFoto() + "?t=" + System.currentTimeMillis();
			}
			log.warn("Foto órfã detectada para pessoa {}: arquivo '{}' não encontrado no filesystem.",
					pessoa.getId(), pessoa.getFoto());
		}
		return "/resources/images/default_avatar.png";
	}

	public boolean isFotoExiste() {
		if (pessoa == null || pessoa.getFoto() == null || pessoa.getFoto().isEmpty()) {
			return false;
		}
		return new File(CAMINHO_FOTOS + pessoa.getFoto()).exists();
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
		if (pessoa.getSacramentos() == null) {
			pessoa.setSacramentos(new ArrayList<>());
		}
		pessoa.setSacramentos(new ArrayList<>(this.sacramentosSelecionados));

		try {
			// Verificar se há uma nova foto para upload (via advanced mode)
			if (fotoBytesUpload != null && fotoBytesUpload.length > 0) {
				String ext = getExtensaoDoTipo(fotoContentTypeUpload);
				String nomeArquivo = gerarNomeArquivoComExt(ext);

				// Garantir que o diretório existe
				File dir = new File(CAMINHO_FOTOS);
				if (!dir.exists()) {
					dir.mkdirs();
				}

				// Apagar a foto anterior, se existir fisicamente
				if (pessoa.getFoto() != null) {
					File fotoAnterior = new File(CAMINHO_FOTOS + pessoa.getFoto());
					if (fotoAnterior.exists()) {
						fotoAnterior.delete();
					}
				}
				// Salvar o novo arquivo no diretório
				Files.write(Paths.get(CAMINHO_FOTOS + nomeArquivo), fotoBytesUpload);

				// Atualizar o nome do arquivo no objeto pessoa
				pessoa.setFoto(nomeArquivo);

				// Limpar estado de upload após salvar
				this.fotoBytesUpload = null;
				this.fotoContentTypeUpload = null;
				this.fotoPreview = null;
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


	private String gerarNomeArquivoComExt(String ext) {
		String nome = pessoa.getNome() != null ? pessoa.getNome() : "pessoa";
		long timestamp = System.currentTimeMillis();
		return nome.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ext;
	}

	private String getExtensaoDoTipo(String contentType) {
		if (contentType == null) return ".jpg";
		if (contentType.contains("png")) return ".png";
		if (contentType.contains("gif")) return ".gif";
		if (contentType.contains("webp")) return ".webp";
		return ".jpg";
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
				// Usar buscarPorIdComPais para carregar relacionamentos pai e mãe
				pessoaService.buscarPorIdComPais(id).ifPresent(p -> {
					this.pessoa = p;
					
					if (this.pessoa.getSacramentos() != null) {
						this.sacramentosSelecionados = new ArrayList<>(this.pessoa.getSacramentos());
					} else {
						this.sacramentosSelecionados = new ArrayList<>();
					}
					// Carrega histórico de transferências se o serviço estiver disponível
					try {
						if (transferenciaService != null) {
							this.historicoTransferencias = transferenciaService.buscarPorPessoa(id);
						}
					} catch (Exception e) {
						log.error("Erro ao carregar histórico de transferências para pessoa {}", id, e);
						this.historicoTransferencias = new ArrayList<>();
					}
				});
				if (this.pessoa == null) {
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Pessoa não encontrada."));
				}
			} catch (NumberFormatException e) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "ID inválido para carregar pessoa."));
				limpar();
			}
		} else {
			limpar();
		}
	}

	// -------------------------------------------------------------------------
	// Métodos de transferência
	// -------------------------------------------------------------------------

	public void registrarSaida() {
		try {
			String usuarioLogado = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal() != null
					? FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName()
					: "sistema";
			transferenciaService.registrarSaida(pessoa, transferenciaParoquiaDestino,
					transferenciaObservacoes, usuarioLogado);
			historicoTransferencias = transferenciaService.buscarPorPessoa(pessoa.getId());
			limparCamposTransferencia();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
							"Transferência de saída registrada com sucesso!"));
		} catch (IllegalStateException | IllegalArgumentException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public void registrarEntrada() {
		try {
			String usuarioLogado = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal() != null
					? FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName()
					: "sistema";
			transferenciaService.registrarEntrada(pessoa, transferenciaParoquiaOrigem,
					transferenciaObservacoes, usuarioLogado);
			historicoTransferencias = transferenciaService.buscarPorPessoa(pessoa.getId());
			limparCamposTransferencia();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
							"Entrada de seguidor externo registrada com sucesso!"));
		} catch (IllegalStateException | IllegalArgumentException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public void registrarRetorno() {
		try {
			String usuarioLogado = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal() != null
					? FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName()
					: "sistema";
			transferenciaService.registrarRetorno(pessoa, transferenciaObservacoes, usuarioLogado);
			historicoTransferencias = transferenciaService.buscarPorPessoa(pessoa.getId());
			limparCamposTransferencia();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
							"Retorno do seguidor registrado com sucesso!"));
		} catch (IllegalStateException | IllegalArgumentException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public boolean isPodeRegistrarSaida() {
		return pessoa != null && pessoa.getId() != null
				&& pessoa.getStatusTransferencia() != StatusTransferencia.TRANSFERIDO_SAIDA;
	}

	public boolean isPodeRegistrarRetorno() {
		return pessoa != null && pessoa.getId() != null
				&& pessoa.getStatusTransferencia() == StatusTransferencia.TRANSFERIDO_SAIDA;
	}

	public boolean isPodeRegistrarEntrada() {
		return pessoa != null && pessoa.getId() != null;
	}

	private void limparCamposTransferencia() {
		this.transferenciaParoquiaDestino = null;
		this.transferenciaParoquiaOrigem = null;
		this.transferenciaObservacoes = null;
	}

	public StatusTransferencia[] getTodosStatusTransferencia() {
		return StatusTransferencia.values();
	}

	public List<Transferencia> getHistoricoTransferencias() {
		return historicoTransferencias;
	}

	public String getTransferenciaParoquiaDestino() {
		return transferenciaParoquiaDestino;
	}

	public void setTransferenciaParoquiaDestino(String transferenciaParoquiaDestino) {
		this.transferenciaParoquiaDestino = transferenciaParoquiaDestino;
	}

	public String getTransferenciaParoquiaOrigem() {
		return transferenciaParoquiaOrigem;
	}

	public void setTransferenciaParoquiaOrigem(String transferenciaParoquiaOrigem) {
		this.transferenciaParoquiaOrigem = transferenciaParoquiaOrigem;
	}

	public String getTransferenciaObservacoes() {
		return transferenciaObservacoes;
	}

	public void setTransferenciaObservacoes(String transferenciaObservacoes) {
		this.transferenciaObservacoes = transferenciaObservacoes;
	}

	public void gerarFichaInscricao(Pessoa pessoa) {
		pdfService.gerarFichaInscricaoPessoa(pessoa);
	}
	public void handleFileUpload(FileUploadEvent event) {
		UploadedFile file = event.getFile();
		if (file != null && file.getSize() > 0) {
			try {
				this.fotoBytesUpload = file.getContent();
				this.fotoContentTypeUpload = file.getContentType();
				String base64Image = Base64.getEncoder().encodeToString(this.fotoBytesUpload);
				this.fotoPreview = "data:" + this.fotoContentTypeUpload + ";base64," + base64Image;
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Foto carregada",
								file.getFileName() + " pronto para salvar."));
			} catch (Exception e) {
				this.fotoPreview = null;
				this.fotoBytesUpload = null;
				this.fotoContentTypeUpload = null;
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro",
								"Não foi possível processar a imagem."));
			}
		} else {
			this.fotoPreview = null;
			this.fotoBytesUpload = null;
		}
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

	/**
	 * Retorna lista de pessoas do sexo masculino para seleção como pai.
	 * Exclui a própria pessoa sendo editada.
	 */
	public List<Pessoa> getPessoasDisponiveisParaPai() {
		List<Pessoa> disponiveis = new ArrayList<>();
		for (Pessoa p : pessoaService.buscarTodos()) {
			// Apenas homens e não pode ser a própria pessoa
			if (p.getSexo() != null && (p.getSexo() == 'M' || p.getSexo() == 'm')) {
				if (pessoa == null || pessoa.getId() == null || !p.getId().equals(pessoa.getId())) {
					disponiveis.add(p);
				}
			}
		}
		return disponiveis;
	}

	/**
	 * Retorna lista de pessoas do sexo feminino para seleção como mãe.
	 * Exclui a própria pessoa sendo editada.
	 */
	public List<Pessoa> getPessoasDisponiveisParaMae() {
		List<Pessoa> disponiveis = new ArrayList<>();
		for (Pessoa p : pessoaService.buscarTodos()) {
			// Apenas mulheres e não pode ser a própria pessoa
			if (p.getSexo() != null && (p.getSexo() == 'F' || p.getSexo() == 'f')) {
				if (pessoa == null || pessoa.getId() == null || !p.getId().equals(pessoa.getId())) {
					disponiveis.add(p);
				}
			}
		}
		return disponiveis;
	}

	/**
	 * Retorna todos os filhos da pessoa sendo editada.
	 * Útil para exibir na tela de edição.
	 */
	public List<Pessoa> getFilhosDaPessoaAtual() {
		if (pessoa == null || pessoa.getId() == null) {
			return new ArrayList<>();
		}
		
		try {
			// Buscar filhos diretamente do banco de dados
			return pessoaService.buscarFilhos(pessoa.getId());
		} catch (Exception e) {
			log.error("Erro ao carregar filhos da pessoa {}", pessoa.getId(), e);
			return new ArrayList<>();
		}
	}

	/**
	 * Retorna os filhos da pessoa em detalhes (carregados em abrirDetalhes).
	 */
	public List<Pessoa> getFilhosDaPessoaDetalhes() {
		return filhosDaPessoaDetalhes;
	}

	// Modal de detalhes da pessoa
	public void abrirDetalhes(Pessoa pessoa) {
		
		// Usar buscarPorIdComPais para carregar relacionamentos pai e mãe
		this.pessoaDetalhes = pessoaService.buscarPorIdComPais(pessoa.getId()).orElse(pessoa);

		// Verificar existência física da foto (tratar fotos órfãs)
		if (this.pessoaDetalhes.getFoto() != null && !this.pessoaDetalhes.getFoto().isEmpty()) {
			File fotoFile = new File(CAMINHO_FOTOS + this.pessoaDetalhes.getFoto());
			if (!fotoFile.exists()) {
				log.warn("Foto órfã detectada para pessoa {}: '{}' não existe no filesystem.",
						pessoa.getId(), this.pessoaDetalhes.getFoto());
				this.pessoaDetalhes.setFoto(null);
			}
		}

		// Carregar filhos
		try {
			this.filhosDaPessoaDetalhes = pessoaService.buscarFilhos(pessoa.getId());
		} catch (Exception e) {
			log.error("Erro ao carregar filhos da pessoa {}", pessoa.getId(), e);
			this.filhosDaPessoaDetalhes = new ArrayList<>();
		}

		// Transferências
		try {
			List<Transferencia> transferencias = transferenciaService.buscarPorPessoa(pessoa.getId());
			this.pessoaDetalhes.setTransferencias(transferencias);
		} catch (Exception e) {
			log.error("Erro ao carregar transferências para pessoa {}", pessoa.getId(), e);
			this.pessoaDetalhes.setTransferencias(new ArrayList<>());
		}

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
		this.filhosDaPessoaDetalhes = new ArrayList<>();
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
