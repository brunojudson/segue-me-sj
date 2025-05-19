package br.com.segueme.repository;

import br.com.segueme.entity.TipoEquipe;
import java.util.List;
import java.util.Optional;

public interface TipoEquipeRepository {
    
    /**
     * Salva um tipo de equipe no banco de dados
     * @param tipoEquipe Objeto tipo de equipe a ser salvo
     * @return TipoEquipe salvo com ID gerado
     */
    TipoEquipe save(TipoEquipe tipoEquipe);
    
    /**
     * Atualiza um tipo de equipe existente no banco de dados
     * @param tipoEquipe Objeto tipo de equipe a ser atualizado
     * @return TipoEquipe atualizado
     */
    TipoEquipe update(TipoEquipe tipoEquipe);
    
    /**
     * Busca um tipo de equipe pelo ID
     * @param id ID do tipo de equipe
     * @return Optional contendo o tipo de equipe, se encontrado
     */
    Optional<TipoEquipe> findById(Long id);
    
    /**
     * Busca todos os tipos de equipe cadastrados
     * @return Lista de tipos de equipe
     */
    List<TipoEquipe> findAll();
    
    /**
     * Busca tipos de equipe pelo nome (busca parcial)
     * @param nome Nome ou parte do nome
     * @return Lista de tipos de equipe que correspondem ao critério
     */
    List<TipoEquipe> findByNome(String nome);
    
    /**
     * Busca tipos de equipe dirigente
     * @return Lista de tipos de equipe dirigente
     */
    List<TipoEquipe> findDirigentes();
    
    /**
     * Remove um tipo de equipe do banco de dados
     * @param id ID do tipo de equipe a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    boolean delete(Long id);
    
    /**
     * Verifica se um tipo de equipe possui equipes associadas
     * @param id ID do tipo de equipe
     * @return true se possui associações, false caso contrário
     */
    boolean hasAssociations(Long id);
}
