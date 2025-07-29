package br.com.segueme.service;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.Equipe;
import br.com.segueme.entity.Pasta;
import br.com.segueme.repository.EquipeRepository;
import br.com.segueme.repository.PastaRepository;
@ApplicationScoped
public class PastaService  implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Inject
    private PastaRepository pastaRepository;
    
    @Inject
    private EquipeRepository equipeRepository;
    
    /**
     * Salva uma nova pasta
     * @param pasta Objeto pasta a ser salvo
     * @return Pasta salva com ID gerado
     */
    public Pasta salvar(Pasta pasta) {
        // Validações
        if (pasta == null) {
            throw new IllegalArgumentException("Pasta não pode ser nula");
        }
        
        if (pasta.getNome() == null || pasta.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da pasta é obrigatório");
        }
        
        if (pasta.getEquipe() == null || pasta.getEquipe().getId() == null) {
            throw new IllegalArgumentException("Equipe da pasta é obrigatória");
        }
        
        if (pasta.getDataInicio() == null) {
            throw new IllegalArgumentException("Data de início da pasta é obrigatória");
        }
        
        if (pasta.getDataFim() == null) {
            throw new IllegalArgumentException("Data de fim da pasta é obrigatória");
        }
        
        // Verificar se equipe existe
        Optional<Equipe> equipeExistente = equipeRepository.findById(pasta.getEquipe().getId());
        if (!equipeExistente.isPresent()) {
            throw new IllegalArgumentException("Equipe não encontrada com o ID: " + pasta.getEquipe().getId());
        }
        
        // Verificar se equipe é do tipo dirigente
        Equipe equipe = equipeExistente.get();
        if (!equipe.ehEquipeDirigente()) {
            throw new IllegalArgumentException("Pasta só pode ser associada a uma equipe dirigente");
        }
        
        // Verificar se data de fim é posterior à data de início
        if (pasta.getDataFim().isBefore(pasta.getDataInicio())) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }
        
        // Verificar mandato de 2 anos
        if (!pasta.verificarMandatoDoisAnos()) {
            throw new IllegalArgumentException("Mandato de pasta deve ser de aproximadamente 2 anos (720-732 dias)");
        }
        
        // Salvar pasta
        return pastaRepository.save(pasta);
    }
    
    /**
     * Atualiza uma pasta existente
     * @param pasta Objeto pasta a ser atualizado
     * @return Pasta atualizada
     */
    public Pasta atualizar(Pasta pasta) {
         
        if (pasta.getId() == null) {
            throw new IllegalArgumentException("ID da pasta é obrigatório para atualização");
        }
        
        // Verificar se pasta existe
        Optional<Pasta> pastaExistente = pastaRepository.findById(pasta.getId());
        if (!pastaExistente.isPresent()) {
            throw new IllegalArgumentException("Pasta não encontrada com o ID: " + pasta.getId());
        }
        
        // Verificar mandato de 2 anos
        if (!pasta.verificarMandatoDoisAnos()) {
            throw new IllegalArgumentException("Mandato de pasta deve ser de aproximadamente 2 anos (720-732 dias)");
        }
        
        // Atualizar pasta
        return pastaRepository.update(pasta);
    }
    
    /**
     * Busca uma pasta pelo ID
     * @param id ID da pasta
     * @return Optional contendo a pasta, se encontrada
     */
    public Optional<Pasta> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da pasta não pode ser nulo");
        }
        
        return pastaRepository.findById(id);
    }
    
    /**
     * Busca todas as pastas cadastradas
     * @return Lista de pastas
     */
    public List<Pasta> buscarTodos() {
        return pastaRepository.findAll();
    }
    
    /**
     * Busca pastas pelo nome (busca parcial)
     * @param nome Nome ou parte do nome
     * @return Lista de pastas que correspondem ao critério
     */
    public List<Pasta> buscarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome para busca não pode ser vazio");
        }
        
        return pastaRepository.findByNome(nome);
    }
    
    /**
     * Busca pastas por equipe
     * @param equipeId ID da equipe
     * @return Lista de pastas da equipe
     */
    public List<Pasta> buscarPorEquipe(Long equipeId) {
        if (equipeId == null) {
            throw new IllegalArgumentException("ID da equipe não pode ser nulo");
        }
        
        return pastaRepository.findByEquipe(equipeId);
    }
    
    /**
     * Busca pastas por período de mandato
     * @param dataInicio Data de início
     * @param dataFim Data de fim
     * @return Lista de pastas no período
     */
    public List<Pasta> buscarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }
        
        return pastaRepository.findByPeriodo(dataInicio, dataFim);
    }
    
    /**
     * Busca pastas com mandato vigente na data atual
     * @return Lista de pastas com mandato vigente
     */
    public List<Pasta> buscarVigentes() {
        return pastaRepository.findVigentes();
    }
    
    /**
     * Remove uma pasta do banco de dados
     * @param id ID da pasta a ser removida
     * @return true se removida com sucesso, false caso contrário
     */
    public boolean remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da pasta não pode ser nulo");
        }
        
        // Verificar se pasta existe
        Optional<Pasta> pastaExistente = pastaRepository.findById(id);
        if (!pastaExistente.isPresent()) {
            throw new IllegalArgumentException("Pasta não encontrada com o ID: " + id);
        }
        
        // Verificar se pasta possui associações
        if (pastaRepository.hasAssociations(id)) {
            throw new IllegalArgumentException("Não é possível remover a pasta pois ela possui associações");
        }
        // Verificar se pasta está ativa
        if (!pastaExistente.get().isAtivo()) {
            throw new IllegalArgumentException("Não é possível remover uma pasta inativa");
        }
        return pastaRepository.delete(id);
    }
}
