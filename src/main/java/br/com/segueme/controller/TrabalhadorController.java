package br.com.segueme.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Encontrista;
import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Equipe;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.entity.Trabalhador;
import br.com.segueme.service.EncontristaService;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.EquipeService;
import br.com.segueme.service.PessoaService;
import br.com.segueme.service.TrabalhadorService;

import java.util.stream.Collectors;
import java.text.Normalizer;
import java.util.Objects;
import java.util.logging.Logger;

@Named
@ViewScoped
public class TrabalhadorController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private TrabalhadorService trabalhadorService;

	@Inject
	private PessoaService pessoaService;

	@Inject
	private EquipeService equipeService;

	@Inject
	private transient EncontroService encontroService;

	@Inject
	private EncontristaService encontristaService;

    private static final Logger logger = Logger.getLogger(TrabalhadorController.class.getName());

	private List<Trabalhador> trabalhadores;
	private Trabalhador trabalhador;
	private Trabalhador trabalhadorSelecionado;
	private Trabalhador trabalhadorDetalhes;

	private List<Pessoa> pessoas;
	private List<Equipe> equipes;
	private List<Encontro> encontros;
	private List<Encontro> encontrosParaFiltro; // Lista para filtro incluindo finalizados


	// Filtros para a tabela
	private String filtroNome;
	private Equipe filtroEquipe;
	private Encontro filtroEncontro;
	// IDs para evitar necessidade de converters em selects
	private Long filtroEquipeId;
	private Long filtroEncontroId;
	private Boolean filtroAptoParaPalestrar = null;
	private Boolean filtroAptoParaCoordenar = null;

	@PostConstruct
	public void init() {
		carregarPessoas();
		carregarEquipes();
		carregarEncontros();
		carregarEncontrosParaFiltro();
		limpar();
		// Não carregar a lista completa ao abrir a página — aguardar filtros
		this.trabalhadores = java.util.Collections.emptyList();
	}

	// Limpa filtros e recarrega todos
	public void limparFiltros() {
		filtroNome = null;
		filtroEquipe = null;
		filtroEncontro = null;
		filtroEquipeId = null;
		filtroEncontroId = null;
		filtroAptoParaPalestrar = null;
		filtroAptoParaCoordenar = null;
		// limpar lista exibida
		this.trabalhadores = java.util.Collections.emptyList();
	}

	// Aplica filtros à lista de trabalhadores
	public void aplicarFiltros() {
		boolean filtroSelecionado =
			(filtroNome != null && !filtroNome.trim().isEmpty()) ||
			(filtroEquipeId != null) ||
			(filtroEncontroId != null) ||
			(filtroAptoParaPalestrar != null && filtroAptoParaPalestrar) ||
			(filtroAptoParaCoordenar != null && filtroAptoParaCoordenar);

		if (!filtroSelecionado) {
			this.trabalhadores = java.util.Collections.emptyList();
			FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione ao menos um filtro antes de pesquisar."));
			return;
		}

		// Use serviço com query otimizada se disponível
		try {
			this.trabalhadores = trabalhadorService.buscarPorFiltros(
				filtroNome,
				filtroEquipeId,
				filtroEncontroId,
				filtroAptoParaPalestrar,
				filtroAptoParaCoordenar);
		} catch (Exception e) {
			// Fallback para filtragem em memória caso o serviço falhe
			List<Trabalhador> filtrados = trabalhadorService.buscarTodos();
			if (filtroNome != null && !filtroNome.trim().isEmpty()) {
				filtrados = filtrados.stream()
					.filter(t -> t.getPessoa() != null && t.getPessoa().getNome() != null && t.getPessoa().getNome().toLowerCase().contains(filtroNome.toLowerCase()))
					.collect(java.util.stream.Collectors.toList());
			}
			if (filtroEquipeId != null) {
				filtrados = filtrados.stream()
					.filter(t -> t.getEquipe() != null && Objects.equals(t.getEquipe().getId(), filtroEquipeId))
					.collect(java.util.stream.Collectors.toList());
			}
			if (filtroEncontroId != null) {
				filtrados = filtrados.stream()
					.filter(t -> t.getEncontro() != null && Objects.equals(t.getEncontro().getId(), filtroEncontroId))
					.collect(java.util.stream.Collectors.toList());
			}
			if (filtroAptoParaPalestrar != null && filtroAptoParaPalestrar) {
				filtrados = filtrados.stream()
					.filter(t -> Boolean.TRUE.equals(t.getAptoParaPalestrar()))
					.collect(java.util.stream.Collectors.toList());
			}
			if (filtroAptoParaCoordenar != null && filtroAptoParaCoordenar) {
				filtrados = filtrados.stream()
					.filter(t -> Boolean.TRUE.equals(t.getAptoParaCoordenar()))
					.collect(java.util.stream.Collectors.toList());
			}
			this.trabalhadores = filtrados;
		}
	}

	public void carregarTrabalhadores() {
		trabalhadores = trabalhadorService.buscarTodos();
	}
	public void atualizarDataInicio() {
		if (trabalhador.getEncontro() != null) {
			// Define a data de início do encontro selecionado no trabalhador
			trabalhador.setDataInicio(trabalhador.getEncontro().getDataInicio());
		}
	}

	public void carregarPessoas() {
		pessoas = pessoaService.buscarTodosEcluindoEncotristasAtivos();
	}

	public void carregarEquipes() {
		// Carrega equipes ativas por padrão
		equipes = equipeService.buscarAtivas();
	}

	public void carregarEncontros() {
		// Carrega apenas encontros ativos para evitar adicionar trabalhadores em encontros finalizados
		encontros = encontroService.buscarAtivos();
	}

	public void carregarEncontrosParaFiltro() {
		// Carrega todos os encontros (incluindo finalizados) para o filtro da listagem
		encontrosParaFiltro = encontroService.buscarTodos();
	}

	/**
	 * Carrega as equipes vinculadas ao encontro selecionado nos filtros.
	 * Se nenhum encontro for selecionado, carrega equipes ativas.
	 */
	/**
	 * Marca automaticamente 'Apto para coordenar' quando marcar como coordenador.
	 * Vinculação obrigatória: coordenador deve ser apto para coordenar.
	 */
	public void marcarAptoParaCoordenar() {
		if (Boolean.TRUE.equals(trabalhador.getEhCoordenador())) {
			trabalhador.setAptoParaCoordenar(true);
		}
	}

	public void carregarEquipesPorEncontro() {
		try {
			if (this.filtroEncontroId != null) {
				equipes = equipeService.buscarPorEncontro(this.filtroEncontroId);
			} else {
				equipes = equipeService.buscarAtivas();
			}
		} catch (Exception e) {
			// Em caso de erro, garante que a lista não seja nula
			equipes = java.util.Collections.emptyList();
		}
	}

	public void limpar() {
		trabalhador = new Trabalhador();
		trabalhador.setDataInicio(LocalDate.now());
	}

	/**
	 * Valida se há parentes próximos (pai, mãe, filhos ou irmãos) na equipe selecionada.
	 * Usa os relacionamentos de entidade (pai_id, mae_id) para validação precisa.
	 * 
	 * @param pessoa A pessoa que será adicionada como trabalhador
	 * @param equipeId O ID da equipe onde será adicionado
	 * @return true se não houver conflito de parentesco, false caso contrário
	 * @throws IllegalArgumentException se houver parente na mesma equipe
	 */
	private boolean validarParentescoNaEquipe(Pessoa pessoa, Long equipeId) {
		if (pessoa == null || equipeId == null) {
			return true;
		}

		try {
			// Buscar o nome da equipe para usar nas mensagens
			String nomeEquipe = equipeService.buscarPorId(equipeId)
				.map(Equipe::getNome)
				.orElse("equipe");
			
			// Recarregar a pessoa com relacionamentos pai/mãe para validação
			Pessoa pessoaComPais = pessoaService.buscarPorIdComPais(pessoa.getId()).orElse(pessoa);
			
			logger.info(String.format("Validando parentesco - Pessoa ID: %d, Nome: %s, Pai ID: %s, Mãe ID: %s",
				pessoaComPais.getId(),
				pessoaComPais.getNome(),
				pessoaComPais.getPai() != null ? pessoaComPais.getPai().getId() : "null",
				pessoaComPais.getMae() != null ? pessoaComPais.getMae().getId() : "null"));
			
			// Buscar todos os trabalhadores ativos da equipe
			List<Trabalhador> trabalhadoresDaEquipe = trabalhadorService.buscarPorEquipe(equipeId)
				.stream()
				.filter(Trabalhador::isAtivo)
				.collect(Collectors.toList());

			logger.info(String.format("Equipe ID: %d (%s) tem %d trabalhadores ativos", equipeId, nomeEquipe, trabalhadoresDaEquipe.size()));

			// Verificar se há parentes próximos
			for (Trabalhador trabalhadorExistente : trabalhadoresDaEquipe) {
				Pessoa pessoaExistente = trabalhadorExistente.getPessoa();
				
				// Pular se for a mesma pessoa (caso de edição)
				if (pessoaExistente != null && pessoaExistente.getId().equals(pessoaComPais.getId())) {
					continue;
				}

				// Recarregar pessoa existente com relacionamentos para validação
				Pessoa pessoaExistenteComPais = pessoaService.buscarPorIdComPais(pessoaExistente.getId()).orElse(pessoaExistente);

				logger.info(String.format("  Verificando contra - Pessoa ID: %d, Nome: %s, Pai ID: %s, Mãe ID: %s",
					pessoaExistenteComPais.getId(),
					pessoaExistenteComPais.getNome(),
					pessoaExistenteComPais.getPai() != null ? pessoaExistenteComPais.getPai().getId() : "null",
					pessoaExistenteComPais.getMae() != null ? pessoaExistenteComPais.getMae().getId() : "null"));

				// Usar o método da entidade Pessoa para verificar parentesco
				if (pessoaComPais.ehParenteProximoDe(pessoaExistenteComPais)) {
					String tipoParentesco = determinarTipoParentesco(pessoaComPais, pessoaExistenteComPais);
					logger.warning(String.format("PARENTESCO DETECTADO! %s é %s de %s",
						pessoaComPais.getNome(), tipoParentesco, pessoaExistenteComPais.getNome()));
					throw new IllegalArgumentException(
						String.format("Não é permitido adicionar %s na equipe '%s' pois %s '%s' já faz parte dela.",
							pessoaComPais.getNome(),
							nomeEquipe,
							tipoParentesco,
							pessoaExistenteComPais.getNome()));
				}
			}

			logger.info("Validação de parentesco OK - Nenhum parente encontrado na equipe");
			return true;

		} catch (IllegalArgumentException e) {
			throw e; // Repassa a exceção com a mensagem formatada
		} catch (Exception e) {
			logger.warning("Erro ao validar parentesco: " + e.getMessage());
			e.printStackTrace();
			// Em caso de erro na validação, permite continuar mas loga o problema
			return true;
		}
	}

	/**
	 * Determina o tipo de parentesco entre duas pessoas para mensagem amigável.
	 */
	private String determinarTipoParentesco(Pessoa pessoa1, Pessoa pessoa2) {
		// Pessoa2 é pai/mãe de Pessoa1
		if (pessoa2.getId() != null && pessoa1.getPai() != null && pessoa1.getPai().getId() != null
				&& pessoa2.getId().equals(pessoa1.getPai().getId())) {
			return "o pai";
		}
		if (pessoa2.getId() != null && pessoa1.getMae() != null && pessoa1.getMae().getId() != null
				&& pessoa2.getId().equals(pessoa1.getMae().getId())) {
			return "a mãe";
		}
		
		// Pessoa1 é pai/mãe de Pessoa2
		if (pessoa1.getId() != null && pessoa2.getPai() != null && pessoa2.getPai().getId() != null
				&& pessoa1.getId().equals(pessoa2.getPai().getId())) {
			return pessoa1.getSexo() != null && pessoa1.getSexo() == 'F' ? "a filha" : "o filho";
		}
		if (pessoa1.getId() != null && pessoa2.getMae() != null && pessoa2.getMae().getId() != null
				&& pessoa1.getId().equals(pessoa2.getMae().getId())) {
			return pessoa1.getSexo() != null && pessoa1.getSexo() == 'F' ? "a filha" : "o filho";
		}
		
		// São irmãos
		if ((pessoa1.getPai() != null && pessoa1.getPai().getId() != null
				&& pessoa2.getPai() != null && pessoa2.getPai().getId() != null
				&& pessoa1.getPai().getId().equals(pessoa2.getPai().getId())) ||
		    (pessoa1.getMae() != null && pessoa1.getMae().getId() != null
				&& pessoa2.getMae() != null && pessoa2.getMae().getId() != null
				&& pessoa1.getMae().getId().equals(pessoa2.getMae().getId()))) {
			return pessoa2.getSexo() != null && pessoa2.getSexo() == 'F' ? "a irmã" : "o irmão";
		}
		
		return "o(a) parente";
	}

	public String salvar() {
		return salvarInterno(true);
	}
	
	public String salvarEContinuar() {
		return salvarInterno(false);
	}

	private String salvarInterno(boolean redirecionarParaLista) {
		try {
			// Validar se o encontro está ativo (se informado)
			if (trabalhador.getEncontro() != null && trabalhador.getEncontro().getId() != null) {
				encontroService.buscarPorId(trabalhador.getEncontro().getId()).ifPresent(enc -> {
					if (enc.getAtivo() != null && !enc.getAtivo()) {
						throw new IllegalArgumentException(
							"Não é permitido adicionar trabalhadores em encontros finalizados/inativos.");
					}
				});
			}
			
			// Validar vinculação obrigatória: coordenador deve ser apto para coordenar
			if (Boolean.TRUE.equals(trabalhador.getEhCoordenador()) && 
				!Boolean.TRUE.equals(trabalhador.getAptoParaCoordenar())) {
				throw new IllegalArgumentException(
					"Um coordenador deve estar marcado como 'Apto para coordenar'.");
			}
			
			// Validar parentesco na equipe
			if (trabalhador.getEquipe() != null && trabalhador.getPessoa() != null) {
				validarParentescoNaEquipe(trabalhador.getPessoa(), trabalhador.getEquipe().getId());
			}

			// Verificar se a pessoa já foi encontrista
			if (trabalhador.getPessoa() != null) {
				List<Encontrista> encontristas = encontristaService.buscarPorPessoa(trabalhador.getPessoa().getId());
				trabalhador.setFoiEncontrista(!encontristas.isEmpty());
			}

			if (trabalhador.getId() == null) {
				trabalhadorService.salvar(trabalhador);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Trabalhador cadastrado com sucesso!"));
			} else {
				trabalhadorService.atualizar(trabalhador);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Trabalhador atualizado com sucesso!"));
			}

			carregarTrabalhadores();
			limpar();
			return redirecionarParaLista ? "lista?faces-redirect=true" : "";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
			return null;
		}
	}

	public String visualizar(Trabalhador trabalhador) {
		this.trabalhadorSelecionado = trabalhador;
		return "visualizar?faces-redirect=true&id=" + trabalhador.getId();
	}

	public String editar(Trabalhador trabalhador) {
		this.trabalhador = trabalhador;
		return "cadastro?faces-redirect=true&id=" + trabalhador.getId();
	}

	public void prepararExclusao(Trabalhador trabalhador) {
		this.trabalhadorSelecionado = trabalhador;
	}

	public void excluir() {
		try {
			trabalhadorService.remover(trabalhadorSelecionado.getId());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Trabalhador excluído com sucesso!"));
			carregarTrabalhadores();
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public void carregarTrabalhador() {
		String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		if (idParam != null && !idParam.isEmpty()) {
			Long id = Long.valueOf(idParam);
			trabalhadorService.buscarPorId(id).ifPresent(t -> this.trabalhador = t);
		}
	}

	public void verificarSeEncontrista() {
		if (trabalhador.getPessoa() != null) {
			List<Encontrista> encontristas = encontristaService.buscarPorPessoa(trabalhador.getPessoa().getId());
			trabalhador.setFoiEncontrista(!encontristas.isEmpty());
		}
	}

	public void abrirDetalhes(Trabalhador trabalhador) {
		trabalhadorService.buscarPorId(trabalhador.getId()).ifPresent(t -> this.trabalhadorDetalhes = t);
	}

	public void fecharDetalhes() {
		this.trabalhadorDetalhes = null;
	}

	public Trabalhador getTrabalhadorDetalhes() {
		return trabalhadorDetalhes;
	}

	// Getters e Setters

	/**
	 * Verifica se os pais do trabalhador já estão na equipe selecionada e se o trabalhador possui filhos cadastrados.
	 * Exibe mensagem no selectOneMenu se necessário.
	 */
	public void verificarParentescoEquipe() {
		FacesContext context = FacesContext.getCurrentInstance();
		// Limpa mensagens anteriores do select
		context.getMessages("trabalhadorForm:equipe").forEachRemaining(m -> {}); // força limpeza

		if (trabalhador == null || trabalhador.getPessoa() == null || trabalhador.getEquipe() == null) {
			return;
		}

		Pessoa pessoa = trabalhador.getPessoa();
		Equipe equipe = trabalhador.getEquipe();

		// Verificação 1: pais na equipe
		String nomePai = pessoa.getFiliacaoPai();
		String nomeMae = pessoa.getFiliacaoMae();
		List<Pessoa> membrosEquipe = equipeService.buscarMembrosDaEquipe(equipe.getId());
		boolean paiNaEquipe = false;
		boolean maeNaEquipe = false;
		if (nomePai != null && !nomePai.trim().isEmpty()) {
			paiNaEquipe = membrosEquipe.stream().anyMatch(p -> nomePai.equalsIgnoreCase(p.getNome()));
		}
		if (nomeMae != null && !nomeMae.trim().isEmpty()) {
			maeNaEquipe = membrosEquipe.stream().anyMatch(p -> nomeMae.equalsIgnoreCase(p.getNome()));
		}
		if (paiNaEquipe || maeNaEquipe) {
			String msg = "Atenção: " + (paiNaEquipe ? "O pai " + nomePai : "") + (paiNaEquipe && maeNaEquipe ? " e " : "") + (maeNaEquipe ? "a mãe " + nomeMae : "") + " já faz(em) parte da equipe.";
			context.addMessage("trabalhadorForm:equipe", new FacesMessage(FacesMessage.SEVERITY_WARN, msg, null));
		}

		// Verificação 2: filhos cadastrados
		Character sexo = pessoa.getSexo();
		String nomeTrabalhador = pessoa.getNome();
		List<Pessoa> filhos = null;
		if (sexo != null && (sexo == 'M' || sexo == 'm')) {
			filhos = pessoaService.buscarPorPai(nomeTrabalhador);
		} else if (sexo != null && (sexo == 'F' || sexo == 'f')) {
			filhos = pessoaService.buscarPorMae(nomeTrabalhador);
		}
		if (filhos != null && !filhos.isEmpty()) {
			// Filtrar filhos que estão na mesma equipe
			List<Pessoa> filhosNaEquipe = filhos.stream()
				.filter(filho -> membrosEquipe.stream().anyMatch(m -> m.getId().equals(filho.getId())))
				.collect(Collectors.toList());
			if (!filhosNaEquipe.isEmpty()) {
				String nomesFilhos = filhosNaEquipe.stream().map(Pessoa::getNome).collect(Collectors.joining(", "));
				String msg = "Atenção, filho(s): " + nomesFilhos + " já faz(em) parte da equipe.";
				context.addMessage("trabalhadorForm:equipe", new FacesMessage(FacesMessage.SEVERITY_WARN, msg, null));
			}
		}

		// Verificação 3: já trabalhou em equipe com nome igual ou semelhante
		try {
			List<Trabalhador> historico = trabalhadorService.buscarPorPessoa(pessoa.getId());
			if (historico != null && !historico.isEmpty()) {
				String nomeSelecionado = equipe.getNome();
				if (nomeSelecionado != null && !nomeSelecionado.trim().isEmpty()) {
					String normSelecionado = normalizeNome(nomeSelecionado);
					List<String> equipesHistoricas = historico.stream()
						.map(t -> t.getEquipe() != null ? t.getEquipe().getNome() : null)
						.filter(Objects::nonNull)
						.distinct()
						.collect(Collectors.toList());
					List<String> similares = equipesHistoricas.stream()
						.filter(n -> n != null && !n.trim().isEmpty())
						.filter(n -> normalizeNome(n).equals(normSelecionado))
						.collect(Collectors.toList());
					if (!similares.isEmpty()) {
						String msg = "Atenção: a pessoa já trabalhou em equipe(s) com nome semelhante: " + String.join(", ", similares) + ".";
						context.addMessage("trabalhadorForm:equipe", new FacesMessage(FacesMessage.SEVERITY_WARN, msg, null));
					}
				}
			}
		} catch (Exception ex) {
			// Não quebrar o fluxo por erro nessa verificação; apenas logue se necessário
		}
	}

	private String normalizeNome(String s) {
		if (s == null) return null;
		String n = Normalizer.normalize(s, Normalizer.Form.NFD);
		n = n.replaceAll("\\p{M}", ""); // remove diacríticos
		n = n.replaceAll("[^A-Za-z0-9]", ""); // remove espaços e pontuação
		return n.toUpperCase();
	}

	public List<Trabalhador> getTrabalhadores() {
		return trabalhadores;
	}

	public void setTrabalhadores(List<Trabalhador> trabalhadores) {
		this.trabalhadores = trabalhadores;
	}

	public Trabalhador getTrabalhador() {
		return trabalhador;
	}

	public void setTrabalhador(Trabalhador trabalhador) {
		this.trabalhador = trabalhador;
	}

	public Trabalhador getTrabalhadorSelecionado() {
		return trabalhadorSelecionado;
	}

	public void setTrabalhadorSelecionado(Trabalhador trabalhadorSelecionado) {
		this.trabalhadorSelecionado = trabalhadorSelecionado;
	}

	public List<Pessoa> getPessoas() {
		return pessoas;
	}

	public void setPessoas(List<Pessoa> pessoas) {
		this.pessoas = pessoas;
	}

	public List<Equipe> getEquipes() {
		return equipes;
	}

	public void setEquipes(List<Equipe> equipes) {
		this.equipes = equipes;
	}

	public List<Encontro> getEncontros() {
		return encontros;
	}

	public void setEncontros(List<Encontro> encontros) {
		this.encontros = encontros;
	}

	public List<Encontro> getEncontrosParaFiltro() {
		return encontrosParaFiltro;
	}

	public void setEncontrosParaFiltro(List<Encontro> encontrosParaFiltro) {
		this.encontrosParaFiltro = encontrosParaFiltro;
	}

	// Getters e setters dos filtros
	public String getFiltroNome() { return filtroNome; }
	public void setFiltroNome(String filtroNome) { this.filtroNome = filtroNome; }
	public Equipe getFiltroEquipe() { return filtroEquipe; }
	public void setFiltroEquipe(Equipe filtroEquipe) { this.filtroEquipe = filtroEquipe; }
	public Encontro getFiltroEncontro() { return filtroEncontro; }
	public void setFiltroEncontro(Encontro filtroEncontro) { this.filtroEncontro = filtroEncontro; }
	public Long getFiltroEquipeId() { return filtroEquipeId; }
	public void setFiltroEquipeId(Long filtroEquipeId) { this.filtroEquipeId = filtroEquipeId; }
	public Long getFiltroEncontroId() { return filtroEncontroId; }
	public void setFiltroEncontroId(Long filtroEncontroId) { this.filtroEncontroId = filtroEncontroId; }
	public Boolean getFiltroAptoParaPalestrar() { return filtroAptoParaPalestrar; }
	public void setFiltroAptoParaPalestrar(Boolean filtroAptoParaPalestrar) { this.filtroAptoParaPalestrar = filtroAptoParaPalestrar; }
	public Boolean getFiltroAptoParaCoordenar() { return filtroAptoParaCoordenar; }
	public void setFiltroAptoParaCoordenar(Boolean filtroAptoParaCoordenar) { this.filtroAptoParaCoordenar = filtroAptoParaCoordenar; }
}
