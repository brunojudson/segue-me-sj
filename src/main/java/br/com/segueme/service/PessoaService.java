  package br.com.segueme.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import br.com.segueme.entity.Pessoa;
import br.com.segueme.repository.PessoaRepository;

@ApplicationScoped
public class PessoaService implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private PessoaRepository pessoaRepository;


    /**
     * Salva uma nova pessoa
     * @param pessoa Objeto pessoa a ser salvo
     * @return Pessoa salva com ID gerado
     */
    public Pessoa salvar(Pessoa pessoa) {
        // Validações
        if (pessoa == null) {
            throw new IllegalArgumentException("Pessoa não pode ser nula");
        }

        if (pessoa.getNome() == null || pessoa.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da pessoa é obrigatório");
        }

		/*
		 * if (pessoa.getCpf() == null || pessoa.getCpf().trim().isEmpty()) { throw new
		 * IllegalArgumentException("CPF da pessoa é obrigatório"); }
		 */

        if (pessoa.getDataNascimento() == null) {
            throw new IllegalArgumentException("Data de nascimento da pessoa é obrigatória");
        }

		/*
		 * // Verificar se CPF já existe Optional<Pessoa> pessoaExistente =
		 * pessoaRepository.findByCpf(pessoa.getCpf()); if (pessoaExistente.isPresent()
		 * && !pessoaExistente.get().getId().equals(pessoa.getId())) { throw new
		 * IllegalArgumentException("CPF já cadastrado para outra pessoa"); }
		 */
        pessoa.calcularIdade();
        // Salvar pessoa
        return pessoaRepository.save(pessoa);
    }

    /**
     * Atualiza uma pessoa existente
     * @param pessoa Objeto pessoa a ser atualizado
     * @return Pessoa atualizada
     */
    public Pessoa atualizar(Pessoa pessoa) {
        // Validações
        if (pessoa == null) {
            throw new IllegalArgumentException("Pessoa não pode ser nula");
        }

        if (pessoa.getId() == null) {
            throw new IllegalArgumentException("ID da pessoa é obrigatório para atualização");
        }

        // Verificar se pessoa existe
        Optional<Pessoa> pessoaExistente = pessoaRepository.findById(pessoa.getId());
        if (!pessoaExistente.isPresent()) {
            throw new IllegalArgumentException("Pessoa não encontrada com o ID: " + pessoa.getId());
        }

        // Verificar se CPF já existe para outra pessoa
		/*
		 * Optional<Pessoa> pessoaPorCpf = pessoaRepository.findByCpf(pessoa.getCpf());
		 * if (pessoaPorCpf.isPresent() &&
		 * !pessoaPorCpf.get().getId().equals(pessoa.getId())) { throw new
		 * IllegalArgumentException("CPF já cadastrado para outra pessoa"); }
		 */
        pessoa.calcularIdade();
        // Atualizar pessoa
        return pessoaRepository.update(pessoa);
    }

    /**
     * Busca uma pessoa pelo ID
     * @param id ID da pessoa
     * @return Optional contendo a pessoa, se encontrada
     */
    public Optional<Pessoa> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da pessoa não pode ser nulo");
        }

        return pessoaRepository.findById(id);
    }

    /**
     * Busca todas as pessoas cadastradas
     * @return Lista de pessoas
     */
    public List<Pessoa> buscarTodos() {
        return pessoaRepository.findAll();
    }

    /**
	 * Busca todas as pessoas que não são encontristas ativos
	 * @return Lista de pessoas
	 */
    public List<Pessoa> buscarTodosEcluindoEncotristasAtivos() {
		return pessoaRepository.findAllExcludingActiveEncontristas();
	}

    public List<Pessoa> buscarTodosExcetoEncotristas() {
		return pessoaRepository.findAllExcludingEncontristas();
	}

    /**
     * Busca pessoas pelo nome (busca parcial)
     * @param nome Nome ou parte do nome
     * @return Lista de pessoas que correspondem ao critério
     */
    public List<Pessoa> buscarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome para busca não pode ser vazio");
        }

        return pessoaRepository.findByNome(nome);
    }

    /**
     * Busca pessoa pelo CPF (busca exata)
     * @param cpf CPF da pessoa
     * @return Optional contendo a pessoa, se encontrada
     */
    public Optional<Pessoa> buscarPorCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF para busca não pode ser vazio");
        }

        return pessoaRepository.findByCpf(cpf);
    }

    /**
     * Remove uma pessoa do banco de dados
     * @param id ID da pessoa a ser removida
     * @return true se removida com sucesso, false caso contrário
     */
    public boolean remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da pessoa não pode ser nulo");
        }

        // Verificar se pessoa existe
        Optional<Pessoa> pessoaExistente = pessoaRepository.findById(id);
        if (!pessoaExistente.isPresent()) {
            throw new IllegalArgumentException("Pessoa não encontrada com o ID: " + id);
        }

        // Verificar se pessoa possui associações
        if (pessoaRepository.hasAssociations(id)) {
            throw new IllegalArgumentException("Não é possível remover a pessoa pois ela possui associações");
        }

        return pessoaRepository.delete(id);
    }

    /**
     * Desativa uma pessoa (exclusão lógica)
     * @param id ID da pessoa a ser desativada
     * @return true se desativada com sucesso, false caso contrário
     */
    public boolean desativar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da pessoa não pode ser nulo");
        }

        // Verificar se pessoa existe
        Optional<Pessoa> pessoaExistente = pessoaRepository.findById(id);
        if (!pessoaExistente.isPresent()) {
            throw new IllegalArgumentException("Pessoa não encontrada com o ID: " + id);
        }

        return pessoaRepository.deactivate(id);
    }
}
