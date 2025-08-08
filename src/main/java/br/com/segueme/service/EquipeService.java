package br.com.segueme.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Equipe;
import br.com.segueme.entity.TipoEquipe;
import br.com.segueme.repository.EncontroRepository;
import br.com.segueme.repository.EquipeRepository;
import br.com.segueme.repository.TipoEquipeRepository;

@ApplicationScoped
public class EquipeService implements Serializable {

    @Inject
    private TrabalhadorService trabalhadorService;

    /**
     * Busca todos os membros de uma equipe pelo id da equipe
     */
    public List<br.com.segueme.entity.Pessoa> buscarMembrosDaEquipe(Long equipeId) {
        // Supondo que existe um método no repositório ou lógica para buscar membros
        // Aqui, para exemplo, retorna todos trabalhadores da equipe e extrai as pessoas
        List<br.com.segueme.entity.Trabalhador> trabalhadores = trabalhadorService.buscarPorEquipe(equipeId);
        return trabalhadores.stream().map(br.com.segueme.entity.Trabalhador::getPessoa).collect(java.util.stream.Collectors.toList());
    }
    private static final long serialVersionUID = 1L;
    
    @Inject
    private EquipeRepository equipeRepository;
    
    @Inject
    private TipoEquipeRepository tipoEquipeRepository;
    
    @Inject
    private EncontroRepository encontroRepository;
    
    /**
     * Salva uma nova equipe
     * @param equipe Objeto equipe a ser salvo
     * @return Equipe salva com ID gerado
     */
    public Equipe salvar(Equipe equipe) {
        // Validações
        if (equipe == null) {
            throw new IllegalArgumentException("Equipe não pode ser nula");
        }
        
        if (equipe.getNome() == null || equipe.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da equipe é obrigatório");
        }
        
        if (equipe.getTipoEquipe() == null || equipe.getTipoEquipe().getId() == null) {
            throw new IllegalArgumentException("Tipo de equipe é obrigatório");
        }
        
        // Verificar se tipo de equipe existe
        Optional<TipoEquipe> tipoEquipeExistente = tipoEquipeRepository.findById(equipe.getTipoEquipe().getId());
        if (!tipoEquipeExistente.isPresent()) {
            throw new IllegalArgumentException("Tipo de equipe não encontrado com o ID: " + equipe.getTipoEquipe().getId());
        }
        
        // Verificar se encontro existe, se informado
        if (equipe.getEncontro() != null && equipe.getEncontro().getId() != null) {
            Optional<Encontro> encontroExistente = encontroRepository.findById(equipe.getEncontro().getId());
            if (!encontroExistente.isPresent()) {
                throw new IllegalArgumentException("Encontro não encontrado com o ID: " + equipe.getEncontro().getId());
            }
        }
        
        // Salvar equipe
        return equipeRepository.save(equipe);
    }
    
    /**
     * Atualiza uma equipe existente
     * @param equipe Objeto equipe a ser atualizado
     * @return Equipe atualizada
     */
    public Equipe atualizar(Equipe equipe) {
        // Validações
        if (equipe == null) {
            throw new IllegalArgumentException("Equipe não pode ser nula");
        }
        
        if (equipe.getId() == null) {
            throw new IllegalArgumentException("ID da equipe é obrigatório para atualização");
        }
        
        // Verificar se equipe existe
        Optional<Equipe> equipeExistente = equipeRepository.findById(equipe.getId());
        if (!equipeExistente.isPresent()) {
            throw new IllegalArgumentException("Equipe não encontrada com o ID: " + equipe.getId());
        }
        
        // Atualizar equipe
        return equipeRepository.update(equipe);
    }
    
    /**
     * Busca uma equipe pelo ID
     * @param id ID da equipe
     * @return Optional contendo a equipe, se encontrada
     */
    public Optional<Equipe> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da equipe não pode ser nulo");
        }
        
        return equipeRepository.findById(id);
    }
    
    /**
     * Busca todas as equipes cadastradas
     * @return Lista de equipes
     */
    public List<Equipe> buscarTodos() {
        return equipeRepository.findAll();
    }
    
    /**
     * Busca equipes pelo nome (busca parcial)
     * @param nome Nome ou parte do nome
     * @return Lista de equipes que correspondem ao critério
     */
    public List<Equipe> buscarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome para busca não pode ser vazio");
        }
        
        return equipeRepository.findByNome(nome);
    }
    
    /**
     * Busca equipes por tipo
     * @param tipoEquipeId ID do tipo de equipe
     * @return Lista de equipes do tipo especificado
     */
    public List<Equipe> buscarPorTipoEquipe(Long tipoEquipeId) {
        if (tipoEquipeId == null) {
            throw new IllegalArgumentException("ID do tipo de equipe não pode ser nulo");
        }
        
        return equipeRepository.findByTipoEquipe(tipoEquipeId);
    }
    
    /**
     * Busca equipes por encontro
     * @param encontroId ID do encontro
     * @return Lista de equipes do encontro
     */
    public List<Equipe> buscarPorEncontro(Long encontroId) {
        if (encontroId == null) {
            throw new IllegalArgumentException("ID do encontro não pode ser nulo");
        }
        
        return equipeRepository.findByEncontro(encontroId);
    }
    
    /**
     * Busca equipes dirigentes
     * @return Lista de equipes dirigentes
     */
    public List<Equipe> buscarDirigentes() {
        return equipeRepository.findDirigentes();
    }
    
    /**
     * Busca equipes ativas
     * @return Lista de equipes ativas
     */
    public List<Equipe> buscarAtivas() {
        return equipeRepository.findAtivas();
    }
    
    /**
     * Remove uma equipe do banco de dados
     * @param id ID da equipe a ser removida
     * @return true se removida com sucesso, false caso contrário
     */
    @Transactional
    public boolean remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da equipe não pode ser nulo");
        }
        
        // Verificar se equipe existe
        Optional<Equipe> equipeExistente = equipeRepository.findById(id);
        if (!equipeExistente.isPresent()) {
            throw new IllegalArgumentException("Equipe não encontrada com o ID: " + id);
        }
        
        // Verificar se equipe possui associações
        if (equipeRepository.hasAssociations(id)) {
            throw new IllegalArgumentException("Não é possível remover a equipe pois ela possui associações");
        }
        
        return equipeRepository.delete(id);
    }
    
    /**
     * Desativa uma equipe (exclusão lógica)
     * @param id ID da equipe a ser desativada
     * @return true se desativada com sucesso, false caso contrário
     */
    public boolean desativar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da equipe não pode ser nulo");
        }
        
        // Verificar se equipe existe
        Optional<Equipe> equipeExistente = equipeRepository.findById(id);
        if (!equipeExistente.isPresent()) {
            throw new IllegalArgumentException("Equipe não encontrada com o ID: " + id);
        }
        
        return equipeRepository.deactivate(id);
    }
    public void desativarPorEncontro(Long encontroId) {
        List<Equipe> equipes = equipeRepository.findByEncontro(encontroId);
        for (Equipe equipe : equipes) {
            equipe.setAtivo(false);
            equipeRepository.update(equipe);
        }
    }
}
