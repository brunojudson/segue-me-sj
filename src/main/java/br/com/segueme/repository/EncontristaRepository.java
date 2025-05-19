package br.com.segueme.repository;

import java.util.List;
import java.util.Optional;

import br.com.segueme.entity.Encontrista;

public interface EncontristaRepository {
    
    /**
     * Salva um encontrista no banco de dados
     * @param encontrista Objeto encontrista a ser salvo
     * @return Encontrista salvo com ID gerado
     */
    Encontrista save(Encontrista encontrista);
    
    /**
     * Atualiza um encontrista existente no banco de dados
     * @param encontrista Objeto encontrista a ser atualizado
     * @return Encontrista atualizado
     */
    Encontrista update(Encontrista encontrista);
    
    /**
     * Busca um encontrista pelo ID
     * @param id ID do encontrista
     * @return Optional contendo o encontrista, se encontrado
     */
    Optional<Encontrista> findById(Long id);
    
    /**
     * Busca todos os encontristas cadastrados
     * @return Lista de encontristas
     */
    List<Encontrista> findAll();
    
    /**
     * Busca encontristas por encontro
     * @param encontroId ID do encontro
     * @return Lista de encontristas do encontro
     */
    List<Encontrista> findByEncontro(Long encontroId);
    
    /**
     * Busca encontristas por pessoa
     * @param pessoaId ID da pessoa
     * @return Lista de encontristas da pessoa
     */
    List<Encontrista> findByPessoa(Long pessoaId);
    
    /**
     * Busca encontrista por pessoa e encontro
     * @param pessoaId ID da pessoa
     * @param encontroId ID do encontro
     * @return Optional contendo o encontrista, se encontrado
     */
    Optional<Encontrista> findByPessoaAndEncontro(Long pessoaId, Long encontroId);
    
    /**
     * Busca encontristas que ainda não se tornaram trabalhadores
     * @return Lista de encontristas sem associação com trabalhadores
     */
    List<Encontrista> findSemTrabalhador();
    
    /**
     * Remove um encontrista do banco de dados
     * @param id ID do encontrista a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    boolean delete(Long id);
    
    boolean deleteDirect(Long id);
    /**
     * Verifica se um encontrista está associado a algum trabalhador
     * @param id ID do encontrista
     * @return true se possui associações, false caso contrário
     */
    boolean hasAssociations(Long id);
}
