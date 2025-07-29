package br.com.segueme.repository;

import br.com.segueme.entity.Encontro;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EncontroRepository {
    
    /**
     * Salva um encontro no banco de dados
     * @param encontro Objeto encontro a ser salvo
     * @return Encontro salvo com ID gerado
     */
    Encontro save(Encontro encontro);
    
    /**
     * Atualiza um encontro existente no banco de dados
     * @param encontro Objeto encontro a ser atualizado
     * @return Encontro atualizado
     */
    Encontro update(Encontro encontro);
    
    /**
     * Busca um encontro pelo ID
     * @param id ID do encontro
     * @return Optional contendo o encontro, se encontrado
     */
    Optional<Encontro> findById(Long id);
    
    /**
     * Busca todos os encontros cadastrados
     * @return Lista de encontros
     */
    List<Encontro> findAll();
    
    /**
     * Busca encontros pelo nome (busca parcial)
     * @param nome Nome ou parte do nome
     * @return Lista de encontros que correspondem ao critério
     */
    List<Encontro> findByNome(String nome);
    
    /**
     * Busca encontros por período
     * @param dataInicio Data de início
     * @param dataFim Data de fim
     * @return Lista de encontros no período
     */
    List<Encontro> findByPeriodo(LocalDate dataInicio, LocalDate dataFim);
    
    /**
     * Busca encontros ativos
     * @return Lista de encontros ativos
     */
    List<Encontro> findAtivos();
    
    /**
     * Remove um encontro do banco de dados
     * @param id ID do encontro a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    boolean delete(Long id);
    
    /**
     * Desativa um encontro (exclusão lógica)
     * @param id ID do encontro a ser desativado
     * @return true se desativado com sucesso, false caso contrário
     */
    boolean deactivate(Long id);
    
    /**
     * Verifica se um encontro possui encontristas ou equipes associadas
     * @param id ID do encontro
     * @return true se possui associações, false caso contrário
     */
    boolean hasAssociations(Long id);
    
    /**
     * Retorna os dois encontros mais recentes, ordenados pela data de início decrescente.
     */
    List<Encontro> findUltimosDois();
}
