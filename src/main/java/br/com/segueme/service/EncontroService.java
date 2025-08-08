package br.com.segueme.service;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.Encontro;
import br.com.segueme.repository.EncontroRepository;

@ApplicationScoped
public class EncontroService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Inject
    private EncontroRepository encontroRepository;
    
    /**
     * Salva um novo encontro
     * @param encontro Objeto encontro a ser salvo
     * @return Encontro salvo com ID gerado
     */
    public Encontro salvar(Encontro encontro) {
        // Validações
        if (encontro == null) {
            throw new IllegalArgumentException("Encontro não pode ser nulo");
        }
        
        if (encontro.getNome() == null || encontro.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do encontro é obrigatório");
        }
        
        if (encontro.getDataInicio() == null) {
            throw new IllegalArgumentException("Data de início do encontro é obrigatória");
        }
        
        if (encontro.getDataFim() == null) {
            throw new IllegalArgumentException("Data de fim do encontro é obrigatória");
        }
        
        if (encontro.getLocal() == null || encontro.getLocal().trim().isEmpty()) {
            throw new IllegalArgumentException("Local do encontro é obrigatório");
        }
        
        // Verificar se data de fim é posterior à data de início
        if (encontro.getDataFim().isBefore(encontro.getDataInicio())) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }
        
        // Salvar encontro
        return encontroRepository.save(encontro);
    }
    
    /**
     * Atualiza um encontro existente
     * @param encontro Objeto encontro a ser atualizado
     * @return Encontro atualizado
     */
    public Encontro atualizar(Encontro encontro) {
        // Validações
        if (encontro == null) {
            throw new IllegalArgumentException("Encontro não pode ser nulo");
        }
        
        if (encontro.getId() == null) {
            throw new IllegalArgumentException("ID do encontro é obrigatório para atualização");
        }
        
        // Verificar se encontro existe
        Optional<Encontro> encontroExistente = encontroRepository.findById(encontro.getId());
        if (!encontroExistente.isPresent()) {
            throw new IllegalArgumentException("Encontro não encontrado com o ID: " + encontro.getId());
        }
        
        // Verificar se data de fim é posterior à data de início
        if (encontro.getDataFim().isBefore(encontro.getDataInicio())) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }
        
        // Atualizar encontro
        return encontroRepository.update(encontro);
    }
    
    /**
     * Busca um encontro pelo ID
     * @param id ID do encontro
     * @return Optional contendo o encontro, se encontrado
     */
    public Optional<Encontro> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do encontro não pode ser nulo");
        }
        
        return encontroRepository.findById(id);
    }
    
    /**
     * Busca todos os encontros cadastrados
     * @return Lista de encontros
     */
    public List<Encontro> buscarTodos() {
        return encontroRepository.findAll();
    }
    
    /**
     * Busca encontros pelo nome (busca parcial)
     * @param nome Nome ou parte do nome
     * @return Lista de encontros que correspondem ao critério
     */
    public List<Encontro> buscarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome para busca não pode ser vazio");
        }
        
        return encontroRepository.findByNome(nome);
    }
    
    /**
     * Busca encontros por período
     * @param dataInicio Data de início
     * @param dataFim Data de fim
     * @return Lista de encontros no período
     */
    public List<Encontro> buscarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }
        
        return encontroRepository.findByPeriodo(dataInicio, dataFim);
    }
    
    /**
     * Busca encontros ativos
     * @return Lista de encontros ativos
     */
    public List<Encontro> buscarAtivos() {
        return encontroRepository.findAtivos();
    }
    
    /**
	 * Busca os últimos dois encontros cadastrados
	 * @return Lista com os dois últimos encontros
	 */
    public List<Encontro> buscaUltimosDois() {
        return encontroRepository.findUltimosDois();
    }
    
    /**
     * Remove um encontro do banco de dados
     * @param id ID do encontro a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    public boolean remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do encontro não pode ser nulo");
        }
        
        // Verificar se encontro existe
        Optional<Encontro> encontroExistente = encontroRepository.findById(id);
        if (!encontroExistente.isPresent()) {
            throw new IllegalArgumentException("Encontro não encontrado com o ID: " + id);
        }
        
        // Verificar se encontro possui associações
        if (encontroRepository.hasAssociations(id)) {
            throw new IllegalArgumentException("Não é possível remover o encontro pois ele possui associações");
        }
        
        return encontroRepository.delete(id);
    }
    
    /**
     * Desativa um encontro (exclusão lógica)
     * @param id ID do encontro a ser desativado
     * @return true se desativado com sucesso, false caso contrário
     */
    public boolean desativar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do encontro não pode ser nulo");
        }
        
        // Verificar se encontro existe
        Optional<Encontro> encontroExistente = encontroRepository.findById(id);
        if (!encontroExistente.isPresent()) {
            throw new IllegalArgumentException("Encontro não encontrado com o ID: " + id);
        }
        
        return encontroRepository.deactivate(id);
    }
}
