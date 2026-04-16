package br.com.segueme.repository;

import br.com.segueme.entity.Pessoa;
import java.util.List;
import java.util.Optional;

public interface PessoaRepository {

    List<Pessoa> findByPai(String nomePai);
    List<Pessoa> findByMae(String nomeMae);
    
    /**
     * Salva uma pessoa no banco de dados
     * @param pessoa Objeto pessoa a ser salvo
     * @return Pessoa salva com ID gerado
     */
    Pessoa save(Pessoa pessoa);
    
    /**
     * Atualiza uma pessoa existente no banco de dados
     * @param pessoa Objeto pessoa a ser atualizado
     * @return Pessoa atualizada
     */
    Pessoa update(Pessoa pessoa);
    
    /**
     * Busca uma pessoa pelo ID
     * @param id ID da pessoa
     * @return Optional contendo a pessoa, se encontrada
     */
    Optional<Pessoa> findById(Long id);
    
    /**
     * Busca uma pessoa pelo ID com relacionamentos pai e mãe carregados (eager fetch).
     * Usa LEFT JOIN FETCH para evitar LazyInitializationException.
     * @param id ID da pessoa
     * @return Optional contendo a pessoa com relacionamentos, se encontrada
     */
    Optional<Pessoa> findByIdWithParents(Long id);
    
    /**
     * Busca todos os filhos de uma pessoa (pessoas que têm essa pessoa como pai ou mãe).
     * @param pessoaId ID da pessoa para buscar filhos
     * @return Lista de filhos (pessoas onde pai_id ou mae_id = pessoaId)
     */
    List<Pessoa> findFilhosByPessoaId(Long pessoaId);
    
    /**
     * Busca todas as pessoas cadastradas
     * @return Lista de pessoas
     */
    List<Pessoa> findAll();

    /**
     * Busca todas as pessoas com sacramentos carregados (eager fetch).
     * Usa LEFT JOIN FETCH para evitar LazyInitializationException.
     * @return Lista de pessoas com sacramentos
     */
    List<Pessoa> findAllWithSacramentos();
    

    List<Pessoa> findAllExcludingActiveEncontristas();
    
    List<Pessoa> findAllExcludingEncontristas();
    /**
     * Busca pessoas pelo nome (busca parcial)
     * @param nome Nome ou parte do nome
     * @return Lista de pessoas que correspondem ao critério
     */
    List<Pessoa> findByNome(String nome);
    
    /**
     * Busca pessoa pelo CPF (busca exata)
     * @param cpf CPF da pessoa
     * @return Optional contendo a pessoa, se encontrada
     */
    Optional<Pessoa> findByCpf(String cpf);
    
    /**
     * Remove uma pessoa do banco de dados
     * @param id ID da pessoa a ser removida
     * @return true se removida com sucesso, false caso contrário
     */
    boolean delete(Long id);
    
    /**
     * Desativa uma pessoa (exclusão lógica)
     * @param id ID da pessoa a ser desativada
     * @return true se desativada com sucesso, false caso contrário
     */
    boolean deactivate(Long id);
    
    /**
     * Verifica se uma pessoa está associada a algum encontrista ou trabalhador
     * @param id ID da pessoa
     * @return true se possui associações, false caso contrário
     */
    boolean hasAssociations(Long id);
    
    void atualizarIdades();
    
    Optional<Pessoa> findByTelefone(String telefone);
}
