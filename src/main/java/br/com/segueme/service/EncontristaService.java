package br.com.segueme.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.Encontrista;
import br.com.segueme.entity.Encontro;
import br.com.segueme.repository.EncontristaRepository;

@ApplicationScoped
public class EncontristaService implements Serializable {
	private static final long serialVersionUID = 1L;

	@Inject
	private EncontristaRepository encontristaRepository;
	
	@Inject
    private AuditoriaService auditoriaService;

    @Inject
    private UsuarioService usuarioService;
	/**
	 * Salva um novo encontrista
	 * 
	 * @param encontrista Objeto encontrista a ser salvo
	 * @return Encontrista salvo com ID gerado
	 */
	public Encontrista salvar(Encontrista encontrista) {
		// Validações básicas
		if (encontrista == null) {
			throw new IllegalArgumentException("Seguimista não pode ser nulo");
		}

		if (encontrista.getPessoa() == null || encontrista.getPessoa().getId() == null) {
			throw new IllegalArgumentException("Pessoa do seguimista é obrigatória");
		}

		if (encontrista.getEncontro() == null || encontrista.getEncontro().getId() == null) {
			throw new IllegalArgumentException("Encontro do encontrista é obrigatório");
		}

		// Verificar se a pessoa já participou de algum encontro
		List<Encontrista> participacoes = encontristaRepository.findByPessoa(encontrista.getPessoa().getId());
		if (!participacoes.isEmpty()) {
			throw new IllegalArgumentException(
					"A pessoa já participou de um encontro e não pode ser adicionada novamente");
		}

		// Verificar se a pessoa e o encontro já estão associados
		Optional<Encontrista> encontristaExistente = encontristaRepository
				.findByPessoaAndEncontro(encontrista.getPessoa().getId(), encontrista.getEncontro().getId());
		if (encontristaExistente.isPresent()) {
			throw new IllegalArgumentException("Já existe um encontrista cadastrado para esta pessoa e encontro");
		}
		encontrista.calcularIdade();
		Encontrista encontristaSalva = encontristaRepository.save(encontrista);
		
		auditoriaService.registrar("Encontrista", encontristaSalva.getId(), "INCLUÍDO", usuarioService.getUsuarioLogadoNome(),
        		"Dados Salvo: " + encontrista.toString()); 
		
		// Salvar encontrista
		return encontristaSalva;
	}

	/**
	 * Atualiza um encontrista existente
	 * 
	 * @param encontrista Objeto encontrista a ser atualizado
	 * @return Encontrista atualizado
	 */
	public Encontrista atualizar(Encontrista encontrista) {
		// Validações
		if (encontrista == null) {
			throw new IllegalArgumentException("Seguimista não pode ser nulo");
		}

		if (encontrista.getId() == null) {
			throw new IllegalArgumentException("ID do encontrista é obrigatório para atualização");
		}

		// Verificar se encontrista existe
		Optional<Encontrista> encontristaExistente = encontristaRepository.findById(encontrista.getId());
		if (!encontristaExistente.isPresent()) {
			throw new IllegalArgumentException("Seguimista não encontrado com o ID: " + encontrista.getId());
		}

		// Verificar se o encontro está ativo
		Encontro encontro = encontristaExistente.get().getEncontro();
		if (encontro != null && Boolean.FALSE.equals(encontro.getAtivo())) {
			throw new IllegalArgumentException(
					"Não é possível atualizar o encontrista, pois o encontro está finalizado.");
		}
		encontrista.calcularIdade();
		
		auditoriaService.registrar("Encontrista", encontrista.getId(), "ATUALIZADO", usuarioService.getUsuarioLogadoNome(),
        		"Dados atualizados: " + encontrista.toString()); 
		// Atualizar encontrista
		return encontristaRepository.update(encontrista);
	}

	/**
	 * Busca um encontrista pelo ID
	 * 
	 * @param id ID do encontrista
	 * @return Optional contendo o encontrista, se encontrado
	 */
	public Optional<Encontrista> buscarPorId(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("ID do encontrista não pode ser nulo");
		}

		return encontristaRepository.findById(id);
	}

	/**
	 * Busca todos os encontristas cadastrados
	 * 
	 * @return Lista de encontristas
	 */
	public List<Encontrista> buscarTodos() {
		return encontristaRepository.findAll();
	}

	/**
	 * Busca encontristas por encontro
	 * 
	 * @param encontroId ID do encontro
	 * @return Lista de encontristas do encontro
	 */
	public List<Encontrista> buscarPorEncontro(Long encontroId) {
		if (encontroId == null) {
			throw new IllegalArgumentException("ID do encontro não pode ser nulo");
		}

		return encontristaRepository.findByEncontro(encontroId);
	}

	/**
	 * Busca encontristas por pessoa
	 * 
	 * @param pessoaId ID da pessoa
	 * @return Lista de encontristas da pessoa
	 */
	public List<Encontrista> buscarPorPessoa(Long pessoaId) {
		if (pessoaId == null) {
			throw new IllegalArgumentException("ID da pessoa não pode ser nulo");
		}

		return encontristaRepository.findByPessoa(pessoaId);
	}

	/**
	 * Busca encontrista por pessoa e encontro
	 * 
	 * @param pessoaId   ID da pessoa
	 * @param encontroId ID do encontro
	 * @return Optional contendo o encontrista, se encontrado
	 */
	public Optional<Encontrista> buscarPorPessoaEEncontro(Long pessoaId, Long encontroId) {
		if (pessoaId == null || encontroId == null) {
			throw new IllegalArgumentException("IDs da pessoa e do encontro são obrigatórios");
		}

		return encontristaRepository.findByPessoaAndEncontro(pessoaId, encontroId);
	}

	/**
	 * Busca encontristas que ainda não se tornaram trabalhadores
	 * 
	 * @return Lista de encontristas sem associação com trabalhadores
	 */
	public List<Encontrista> buscarSemTrabalhador() {
		return encontristaRepository.findSemTrabalhador();
	}

	/**
	 * Remove um encontrista do banco de dados
	 * 
	 * @param id ID do encontrista a ser removido
	 * @return true se removido com sucesso, false caso contrário
	 */
	public boolean remover(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("ID do encontrista não pode ser nulo");
		}

		// Verificar se encontrista existe
		Optional<Encontrista> encontristaExistente = encontristaRepository.findById(id);
		if (!encontristaExistente.isPresent()) {
			throw new IllegalArgumentException("Seguimista não encontrado com o ID: " + id);
		}

		// Verificar se o encontro está ativo
		Encontro encontro = encontristaExistente.get().getEncontro();
		if (encontro != null && Boolean.FALSE.equals(encontro.getAtivo())) {
			throw new IllegalArgumentException(
					"Não é possível remover o encontrista, pois o encontro está finalizado.");
		}

		// Verificar se encontrista possui associações
		if (encontristaRepository.hasAssociations(id)) {
			throw new IllegalArgumentException("Não é possível remover o encontrista pois ele possui associações");
		}
		auditoriaService.registrar("Encontrista", id , "EXCLUÍDO", usuarioService.getUsuarioLogadoNome(),
        		"Dados Excluído: " + encontristaExistente.toString());
		return encontristaRepository.deleteDirect(id);
	}

	public void desativarPorEncontro(Long encontroId) {
		if (encontroId == null) {
			throw new IllegalArgumentException("ID do encontro não pode ser nulo");
		}

		List<Encontrista> encontristas = encontristaRepository.findByEncontro(encontroId);
		for (Encontrista encontrista : encontristas) {
			encontrista.setAtivo(false);
			encontristaRepository.update(encontrista);
		}
	}
	public List<Encontrista> buscarAtivos() {
		return encontristaRepository.findAll()
			.stream()
			.filter(Encontrista::getAtivo)
			.collect(Collectors.toList());
	}
}
