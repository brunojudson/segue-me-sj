package br.com.segueme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import br.com.segueme.entity.Dirigente;

public interface DirigenteRepository {
    
    /**
     * Salva um dirigente no banco de dados
     * @param dirigente Objeto dirigente a ser salvo
     * @return Dirigente salvo com ID gerado
     */
    Dirigente save(Dirigente dirigente);
    
    /**
     * Atualiza um dirigente existente no banco de dados
     * @param dirigente Objeto dirigente a ser atualizado
     * @return Dirigente atualizado
     */
    Dirigente update(Dirigente dirigente);
    
    /**
     * Busca um dirigente pelo ID
     * @param id ID do dirigente
     * @return Optional contendo o dirigente, se encontrado
     */
    Optional<Dirigente> findById(Long id);
    
    /**
     * Busca todos os dirigentes cadastrados
     * @return Lista de dirigentes
     */
    List<Dirigente> findAll();
    
    /**
     * Busca dirigentes por trabalhador
     * @param trabalhadorId ID do trabalhador
     * @return Lista de dirigentes do trabalhador
     */
    List<Dirigente> findByTrabalhador(Long trabalhadorId);
    
    /**
     * Busca dirigentes por pasta
     * @param pastaId ID da pasta
     * @return Lista de dirigentes da pasta
     */
    List<Dirigente> findByPasta(Long pastaId);
    
    /**
     * Busca dirigentes por período de mandato
     * @param dataInicio Data de início
     * @param dataFim Data de fim
     * @return Lista de dirigentes no período
     */
    List<Dirigente> findByPeriodo(LocalDate dataInicio, LocalDate dataFim);
    
    /**
     * Busca dirigentes com mandato vigente na data atual
     * @return Lista de dirigentes com mandato vigente
     */
    List<Dirigente> findVigentes();
    
    /**
     * Busca dirigente por trabalhador e pasta
     * @param trabalhadorId ID do trabalhador
     * @param pastaId ID da pasta
     * @return Optional contendo o dirigente, se encontrado
     */
    Optional<Dirigente> findByTrabalhadorAndPasta(Long trabalhadorId, Long pastaId);
    
    /**
     * Remove um dirigente do banco de dados
     * @param id ID do dirigente a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    boolean delete(Long id);
}
