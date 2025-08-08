package br.com.segueme.repository;

import java.util.List;
import java.util.Optional;

import br.com.segueme.entity.Trabalhador;

public interface TrabalhadorRepository {
    
    /**
     * Salva um trabalhador no banco de dados
     * @param trabalhador Objeto trabalhador a ser salvo
     * @return Trabalhador salvo com ID gerado
     */
    Trabalhador save(Trabalhador trabalhador);
    
    /**
     * Atualiza um trabalhador existente no banco de dados
     * @param trabalhador Objeto trabalhador a ser atualizado
     * @return Trabalhador atualizado
     */
    Trabalhador update(Trabalhador trabalhador);
    
    /**
     * Busca um trabalhador pelo ID
     * @param id ID do trabalhador
     * @return Optional contendo o trabalhador, se encontrado
     */
    Optional<Trabalhador> findById(Long id);
    
    /**
     * Busca todos os trabalhadores cadastrados
     * @return Lista de trabalhadores
     */
    List<Trabalhador> findAll();

    List<Trabalhador> findAllAtivos();
    
    List<Trabalhador> findAllDistinct();
    
    /**
     * Busca trabalhadores por pessoa
     * @param pessoaId ID da pessoa
     * @return Lista de trabalhadores da pessoa
     */
    List<Trabalhador> findByPessoa(Long pessoaId);
    
    /**
     * Busca trabalhadores por equipe
     * @param equipeId ID da equipe
     * @return Lista de trabalhadores da equipe
     */
    List<Trabalhador> findByEquipe(Long equipeId);
    
    /**
     * Busca trabalhadores por encontro
     * @param encontroId ID do encontro
     * @return Lista de trabalhadores do encontro
     */
    List<Trabalhador> findByEncontro(Long encontroId);
    
    /**
     * Busca trabalhadores que são coordenadores
     * @return Lista de trabalhadores coordenadores
     */
    List<Trabalhador> findCoordenadores();
    
    /**
     * Busca trabalhadores que foram encontristas
     * @return Lista de trabalhadores que foram encontristas
     */
    List<Trabalhador> findExEncontristas();
    
    /**
     * Busca trabalhador por pessoa, equipe e encontro
     * @param pessoaId ID da pessoa
     * @param equipeId ID da equipe
     * @param encontroId ID do encontro
     * @return Optional contendo o trabalhador, se encontrado
     */
    Optional<Trabalhador> findByPessoaEquipeEncontro(Long pessoaId, Long equipeId, Long encontroId);

    Optional<Trabalhador> findByPessoaEncontro(Long pessoaId, Long encontroId);
    
    /**
     * Remove um trabalhador do banco de dados
     * @param id ID do trabalhador a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    boolean delete(Long id);
    
    /**
     * Verifica se um trabalhador possui contribuições ou cargos associados
     * @param id ID do trabalhador
     * @return true se possui associações, false caso contrário
     */
    boolean hasAssociations(Long id);
}
