package br.com.segueme.service;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.Dirigente;
import br.com.segueme.entity.Pasta;
import br.com.segueme.entity.Trabalhador;
import br.com.segueme.repository.DirigenteRepository;
import br.com.segueme.repository.PastaRepository;
import br.com.segueme.repository.TrabalhadorRepository;

@ApplicationScoped
public class DirigenteService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Inject
    private DirigenteRepository dirigenteRepository;
    
    @Inject
    private TrabalhadorRepository trabalhadorRepository;
    
    @Inject
    private PastaRepository pastaRepository;
    
    /**
     * Salva um novo dirigente
     * @param dirigente Objeto dirigente a ser salvo
     * @return Dirigente salvo com ID gerado
     */
    public Dirigente salvar(Dirigente dirigente) {
        // Validações
        if (dirigente == null) {
            throw new IllegalArgumentException("Dirigente não pode ser nulo");
        }
        
        if (dirigente.getTrabalhador() == null || dirigente.getTrabalhador().getId() == null) {
            throw new IllegalArgumentException("Trabalhador do dirigente é obrigatório");
        }
        
        if (dirigente.getPasta() == null || dirigente.getPasta().getId() == null) {
            throw new IllegalArgumentException("Pasta do dirigente é obrigatória");
        }
        
        if (dirigente.getDataInicio() == null) {
            throw new IllegalArgumentException("Data de início do dirigente é obrigatória");
        }
        
        if (dirigente.getDataFim() == null) {
            throw new IllegalArgumentException("Data de fim do dirigente é obrigatória");
        }
        
        // Verificar se trabalhador existe
        Optional<Trabalhador> trabalhadorExistente = trabalhadorRepository.findById(dirigente.getTrabalhador().getId());
        if (!trabalhadorExistente.isPresent()) {
            throw new IllegalArgumentException("Trabalhador não encontrado com o ID: " + dirigente.getTrabalhador().getId());
        }
        
        // Verificar se pasta existe
        Optional<Pasta> pastaExistente = pastaRepository.findById(dirigente.getPasta().getId());
        if (!pastaExistente.isPresent()) {
            throw new IllegalArgumentException("Pasta não encontrada com o ID: " + dirigente.getPasta().getId());
        }
        
        // Verificar mandato de 2 anos
        if (!dirigente.verificarMandatoDoisAnos()) {
            throw new IllegalArgumentException("Mandato de dirigente deve ser de aproximadamente 2 anos (720-732 dias)");
        }
        
        // Verificar se já existe um dirigente para este trabalhador e pasta
        Optional<Dirigente> dirigenteExistente = dirigenteRepository.findByTrabalhadorAndPasta(
                dirigente.getTrabalhador().getId(), dirigente.getPasta().getId());
        if (dirigenteExistente.isPresent()) {
            throw new IllegalArgumentException("Já existe um dirigente cadastrado para este trabalhador e pasta");
        }
        
        // Salvar dirigente
        return dirigenteRepository.save(dirigente);
    }
    
    /**
     * Atualiza um dirigente existente
     * @param dirigente Objeto dirigente a ser atualizado
     * @return Dirigente atualizado
     */
    public Dirigente atualizar(Dirigente dirigente) {
        // Validações
        if (dirigente == null) {
            throw new IllegalArgumentException("Dirigente não pode ser nulo");
        }
        
        if (dirigente.getId() == null) {
            throw new IllegalArgumentException("ID do dirigente é obrigatório para atualização");
        }
        
        // Verificar se dirigente existe
        Optional<Dirigente> dirigenteExistente = dirigenteRepository.findById(dirigente.getId());
        if (!dirigenteExistente.isPresent()) {
            throw new IllegalArgumentException("Dirigente não encontrado com o ID: " + dirigente.getId());
        }
        
        // Verificar mandato de 2 anos
        if (!dirigente.verificarMandatoDoisAnos()) {
            throw new IllegalArgumentException("Mandato de dirigente deve ser de aproximadamente 2 anos (720-732 dias)");
        }
        
        // Atualizar dirigente
        return dirigenteRepository.update(dirigente);
    }
    
    /**
     * Busca um dirigente pelo ID
     * @param id ID do dirigente
     * @return Optional contendo o dirigente, se encontrado
     */
    public Optional<Dirigente> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do dirigente não pode ser nulo");
        }
        
        return dirigenteRepository.findById(id);
    }
    
    /**
     * Busca todos os dirigentes cadastrados
     * @return Lista de dirigentes
     */
    public List<Dirigente> buscarTodos() {
        return dirigenteRepository.findAll();
    }
    
    /**
     * Busca dirigentes por trabalhador
     * @param trabalhadorId ID do trabalhador
     * @return Lista de dirigentes do trabalhador
     */
    public List<Dirigente> buscarPorTrabalhador(Long trabalhadorId) {
        if (trabalhadorId == null) {
            throw new IllegalArgumentException("ID do trabalhador não pode ser nulo");
        }
        
        return dirigenteRepository.findByTrabalhador(trabalhadorId);
    }
    
    /**
     * Busca dirigentes por pasta
     * @param pastaId ID da pasta
     * @return Lista de dirigentes da pasta
     */
    public List<Dirigente> buscarPorPasta(Long pastaId) {
        if (pastaId == null) {
            throw new IllegalArgumentException("ID da pasta não pode ser nulo");
        }
        
        return dirigenteRepository.findByPasta(pastaId);
    }
    
    /**
     * Busca dirigentes por período de mandato
     * @param dataInicio Data de início
     * @param dataFim Data de fim
     * @return Lista de dirigentes no período
     */
    public List<Dirigente> buscarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }
        
        return dirigenteRepository.findByPeriodo(dataInicio, dataFim);
    }
    
    /**
     * Busca dirigentes com mandato vigente na data atual
     * @return Lista de dirigentes com mandato vigente
     */
    public List<Dirigente> buscarVigentes() {
        return dirigenteRepository.findVigentes();
    }
    
    /**
     * Busca dirigente por trabalhador e pasta
     * @param trabalhadorId ID do trabalhador
     * @param pastaId ID da pasta
     * @return Optional contendo o dirigente, se encontrado
     */
    public Optional<Dirigente> buscarPorTrabalhadorEPasta(Long trabalhadorId, Long pastaId) {
        if (trabalhadorId == null || pastaId == null) {
            throw new IllegalArgumentException("IDs do trabalhador e da pasta são obrigatórios");
        }
        
        return dirigenteRepository.findByTrabalhadorAndPasta(trabalhadorId, pastaId);
    }
    
    /**
     * Remove um dirigente do banco de dados
     * @param id ID do dirigente a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    public boolean remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do dirigente não pode ser nulo");
        }
        
        // Verificar se dirigente existe
        Optional<Dirigente> dirigenteExistente = dirigenteRepository.findById(id);
        if (!dirigenteExistente.isPresent()) {
            throw new IllegalArgumentException("Dirigente não encontrado com o ID: " + id);
        }
        
        return dirigenteRepository.delete(id);
    }
}
