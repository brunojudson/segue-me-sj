package br.com.segueme.repository;

import java.util.List;
import java.util.Optional;

import br.com.segueme.entity.Casal;

public interface CasalRepository {
    
    /**
     * Salva um casal no banco de dados
     * @param casal Objeto casal a ser salvo
     * @return Casal salvo com ID gerado
     */
    Casal save(Casal casal);
    
    /**
     * Atualiza um casal existente no banco de dados
     * @param casal Objeto casal a ser atualizado
     * @return Casal atualizado
     */
    Casal update(Casal casal);
    
    /**
     * Busca um casal pelo ID
     * @param id ID do casal
     * @return Optional contendo o casal, se encontrado
     */
    Optional<Casal> findById(Long id);
    
    /**
     * Busca todos os casais cadastrados
     * @return Lista de casais
     */
    List<Casal> findAll();
    
    /**
     * Busca casais por pessoa (busca por qualquer um dos cônjuges)
     * @param pessoaId ID da pessoa
     * @return Lista de casais que contêm a pessoa
     */
    List<Casal> findByPessoa(Long pessoaId);
    
    /**
     * Busca casal por par de pessoas
     * @param pessoa1Id ID da primeira pessoa
     * @param pessoa2Id ID da segunda pessoa
     * @return Optional contendo o casal, se encontrado
     */
    Optional<Casal> findByPessoas(Long pessoa1Id, Long pessoa2Id);
    
    /**
     * Remove um casal do banco de dados
     * @param id ID do casal a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    boolean delete(Long id);
}
