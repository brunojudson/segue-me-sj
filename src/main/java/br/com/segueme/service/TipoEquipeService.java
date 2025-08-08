package br.com.segueme.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.TipoEquipe;
import br.com.segueme.repository.TipoEquipeRepository;
@ApplicationScoped
public class TipoEquipeService  implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Inject
    private TipoEquipeRepository tipoEquipeRepository;
    
    /**
     * Salva um novo tipo de equipe
     * @param tipoEquipe Objeto tipo de equipe a ser salvo
     * @return TipoEquipe salvo com ID gerado
     */
    public TipoEquipe salvar(TipoEquipe tipoEquipe) {
        // Validações
        if (tipoEquipe == null) {
            throw new IllegalArgumentException("Tipo de equipe não pode ser nulo");
        }
        
        if (tipoEquipe.getNome() == null || tipoEquipe.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do tipo de equipe é obrigatório");
        }
        
        // Salvar tipo de equipe
        return tipoEquipeRepository.save(tipoEquipe);
    }
    
    /**
     * Atualiza um tipo de equipe existente
     * @param tipoEquipe Objeto tipo de equipe a ser atualizado
     * @return TipoEquipe atualizado
     */
    public TipoEquipe atualizar(TipoEquipe tipoEquipe) {
        // Validações
        if (tipoEquipe == null) {
            throw new IllegalArgumentException("Tipo de equipe não pode ser nulo");
        }
        
        if (tipoEquipe.getId() == null) {
            throw new IllegalArgumentException("ID do tipo de equipe é obrigatório para atualização");
        }
        
        // Verificar se tipo de equipe existe
        Optional<TipoEquipe> tipoEquipeExistente = tipoEquipeRepository.findById(tipoEquipe.getId());
        if (!tipoEquipeExistente.isPresent()) {
            throw new IllegalArgumentException("Tipo de equipe não encontrado com o ID: " + tipoEquipe.getId());
        }
        
        // Atualizar tipo de equipe
        return tipoEquipeRepository.update(tipoEquipe);
    }
    
    /**
     * Busca um tipo de equipe pelo ID
     * @param id ID do tipo de equipe
     * @return Optional contendo o tipo de equipe, se encontrado
     */
    public Optional<TipoEquipe> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do tipo de equipe não pode ser nulo");
        }
        
        return tipoEquipeRepository.findById(id);
    }
    
    /**
     * Busca todos os tipos de equipe cadastrados
     * @return Lista de tipos de equipe
     */
    public List<TipoEquipe> buscarTodos() {
        return tipoEquipeRepository.findAll();
    }
    
    /**
     * Busca tipos de equipe pelo nome (busca parcial)
     * @param nome Nome ou parte do nome
     * @return Lista de tipos de equipe que correspondem ao critério
     */
    public List<TipoEquipe> buscarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome para busca não pode ser vazio");
        }
        
        return tipoEquipeRepository.findByNome(nome);
    }
    
    /**
     * Busca tipos de equipe dirigente
     * @return Lista de tipos de equipe dirigente
     */
    public List<TipoEquipe> buscarDirigentes() {
        return tipoEquipeRepository.findDirigentes();
    }
    
    /**
     * Remove um tipo de equipe do banco de dados
     * @param id ID do tipo de equipe a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    public boolean remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do tipo de equipe não pode ser nulo");
        }
        
        // Verificar se tipo de equipe existe
        Optional<TipoEquipe> tipoEquipeExistente = tipoEquipeRepository.findById(id);
        if (!tipoEquipeExistente.isPresent()) {
            throw new IllegalArgumentException("Tipo de equipe não encontrado com o ID: " + id);
        }
        
        // Verificar se tipo de equipe possui associações
        if (tipoEquipeRepository.hasAssociations(id)) {
            throw new IllegalArgumentException("Não é possível remover o tipo de equipe pois ele possui associações");
        }
        
        return tipoEquipeRepository.delete(id);
    }
}
