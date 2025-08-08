package br.com.segueme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import br.com.segueme.entity.Pasta;

public interface PastaRepository {
    
    /**
     * Salva uma pasta no banco de dados
     * @param pasta Objeto pasta a ser salvo
     * @return Pasta salva com ID gerado
     */
    Pasta save(Pasta pasta);
    
    /**
     * Atualiza uma pasta existente no banco de dados
     * @param pasta Objeto pasta a ser atualizado
     * @return Pasta atualizada
     */
    Pasta update(Pasta pasta);
    
    /**
     * Busca uma pasta pelo ID
     * @param id ID da pasta
     * @return Optional contendo a pasta, se encontrada
     */
    Optional<Pasta> findById(Long id);
    
    /**
     * Busca todas as pastas cadastradas
     * @return Lista de pastas
     */
    List<Pasta> findAll();
    
    /**
     * Busca pastas pelo nome (busca parcial)
     * @param nome Nome ou parte do nome
     * @return Lista de pastas que correspondem ao critério
     */
    List<Pasta> findByNome(String nome);
    
    /**
     * Busca pastas por equipe
     * @param equipeId ID da equipe
     * @return Lista de pastas da equipe
     */
    List<Pasta> findByEquipe(Long equipeId);
    
    /**
     * Busca pastas por período de mandato
     * @param dataInicio Data de início
     * @param dataFim Data de fim
     * @return Lista de pastas no período
     */
    List<Pasta> findByPeriodo(LocalDate dataInicio, LocalDate dataFim);
    
    /**
     * Busca pastas com mandato vigente na data atual
     * @return Lista de pastas com mandato vigente
     */
    List<Pasta> findVigentes();
    
    /**
     * Remove uma pasta do banco de dados
     * @param id ID da pasta a ser removida
     * @return true se removida com sucesso, false caso contrário
     */
    boolean delete(Long id);
    
    /**
     * Verifica se uma pasta possui dirigentes associados
     * @param id ID da pasta
     * @return true se possui associações, false caso contrário
     */
    boolean hasAssociations(Long id);
}
