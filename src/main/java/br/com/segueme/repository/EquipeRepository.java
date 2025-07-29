package br.com.segueme.repository;

import java.util.List;
import java.util.Optional;

import br.com.segueme.entity.Equipe;

public interface EquipeRepository {
    
    /**
     * Salva uma equipe no banco de dados
     * @param equipe Objeto equipe a ser salvo
     * @return Equipe salva com ID gerado
     */
    Equipe save(Equipe equipe);
    
    /**
     * Atualiza uma equipe existente no banco de dados
     * @param equipe Objeto equipe a ser atualizado
     * @return Equipe atualizada
     */
    Equipe update(Equipe equipe);
    
    /**
     * Busca uma equipe pelo ID
     * @param id ID da equipe
     * @return Optional contendo a equipe, se encontrada
     */
    Optional<Equipe> findById(Long id);
    
    /**
     * Busca todas as equipes cadastradas
     * @return Lista de equipes
     */
    List<Equipe> findAll();
    
    /**
     * Busca equipes pelo nome (busca parcial)
     * @param nome Nome ou parte do nome
     * @return Lista de equipes que correspondem ao critério
     */
    List<Equipe> findByNome(String nome);
    
    /**
     * Busca equipes por tipo
     * @param tipoEquipeId ID do tipo de equipe
     * @return Lista de equipes do tipo especificado
     */
    List<Equipe> findByTipoEquipe(Long tipoEquipeId);
    
    /**
     * Busca equipes por encontro
     * @param encontroId ID do encontro
     * @return Lista de equipes do encontro
     */
    List<Equipe> findByEncontro(Long encontroId);
    
    /**
     * Busca equipes dirigentes
     * @return Lista de equipes dirigentes
     */
    List<Equipe> findDirigentes();
    
    /**
     * Busca equipes ativas
     * @return Lista de equipes ativas
     */
    List<Equipe> findAtivas();
    
    /**
     * Remove uma equipe do banco de dados
     * @param id ID da equipe a ser removida
     * @return true se removida com sucesso, false caso contrário
     */
    boolean delete(Long id);
    
    /**
     * Desativa uma equipe (exclusão lógica)
     * @param id ID da equipe a ser desativada
     * @return true se desativada com sucesso, false caso contrário
     */
    boolean deactivate(Long id);
    
    /**
     * Verifica se uma equipe possui trabalhadores ou pastas associadas
     * @param id ID da equipe
     * @return true se possui associações, false caso contrário
     */
    boolean hasAssociations(Long id);
}
