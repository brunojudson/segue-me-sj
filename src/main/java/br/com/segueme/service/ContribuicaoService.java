package br.com.segueme.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.Contribuicao;
import br.com.segueme.entity.Trabalhador;
import br.com.segueme.repository.ContribuicaoRepository;
import br.com.segueme.repository.TrabalhadorRepository;

@ApplicationScoped
public class ContribuicaoService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Inject
    private ContribuicaoRepository contribuicaoRepository;
    
    @Inject
    private TrabalhadorRepository trabalhadorRepository;
    
    /**
     * Salva uma nova contribuição
     * @param contribuicao Objeto contribuição a ser salvo
     * @return Contribuição salva com ID gerado
     */
    public Contribuicao salvar(Contribuicao contribuicao) {
        // Validações
        if (contribuicao == null) {
            throw new IllegalArgumentException("Contribuição não pode ser nula");
        }
        
        if (contribuicao.getTrabalhador() == null || contribuicao.getTrabalhador().getId() == null) {
            throw new IllegalArgumentException("Trabalhador da contribuição é obrigatório");
        }
        
        if (contribuicao.getValor() == null || contribuicao.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da contribuição deve ser maior que zero");
        }
        
        if (contribuicao.getFormaPagamento() == null || contribuicao.getFormaPagamento().trim().isEmpty()) {
            throw new IllegalArgumentException("Forma de pagamento da contribuição é obrigatória");
        }
        
        // Verificar se trabalhador existe
        Optional<Trabalhador> trabalhadorExistente = trabalhadorRepository.findById(contribuicao.getTrabalhador().getId());
        if (!trabalhadorExistente.isPresent()) {
            throw new IllegalArgumentException("Trabalhador não encontrado com o ID: " + contribuicao.getTrabalhador().getId());
        }
        
        // Verificar se já existe uma contribuição para o trabalhador
        List<Contribuicao> contribuicoesDoTrabalhador = contribuicaoRepository.findByTrabalhador(contribuicao.getTrabalhador().getId());
        if (!contribuicoesDoTrabalhador.isEmpty()) {
            throw new IllegalArgumentException("Já existe uma contribuição cadastrada para este trabalhador.");
        }
        
        // Salvar contribuição
        return contribuicaoRepository.save(contribuicao);
    }
    
    /**
     * Atualiza uma contribuição existente
     * @param contribuicao Objeto contribuição a ser atualizado
     * @return Contribuição atualizada
     */
    public Contribuicao atualizar(Contribuicao contribuicao) {
        // Validações
        if (contribuicao == null) {
            throw new IllegalArgumentException("Contribuição não pode ser nula");
        }
        
        if (contribuicao.getId() == null) {
            throw new IllegalArgumentException("ID da contribuição é obrigatório para atualização");
        }
        
        // Verificar se contribuição existe
        Optional<Contribuicao> contribuicaoExistente = contribuicaoRepository.findById(contribuicao.getId());
        if (!contribuicaoExistente.isPresent()) {
            throw new IllegalArgumentException("Contribuição não encontrada com o ID: " + contribuicao.getId());
        }
        
        // Atualizar contribuição
        return contribuicaoRepository.update(contribuicao);
    }
    
    /**
     * Busca uma contribuição pelo ID
     * @param id ID da contribuição
     * @return Optional contendo a contribuição, se encontrada
     */
    public Optional<Contribuicao> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da contribuição não pode ser nulo");
        }
        
        return contribuicaoRepository.findById(id);
    }
    
    /**
     * Busca todas as contribuições cadastradas
     * @return Lista de contribuições
     */
    public List<Contribuicao> buscarTodos() {
        return contribuicaoRepository.findAll();
    }
    
    /**
     * Busca contribuições por trabalhador
     * @param trabalhadorId ID do trabalhador
     * @return Lista de contribuições do trabalhador
     */
    public List<Contribuicao> buscarPorTrabalhador(Long trabalhadorId) {
        if (trabalhadorId == null) {
            throw new IllegalArgumentException("ID do trabalhador não pode ser nulo");
        }
        
        return contribuicaoRepository.findByTrabalhador(trabalhadorId);
    }
    
    /**
     * Busca contribuições por período
     * @param dataInicio Data de início
     * @param dataFim Data de fim
     * @return Lista de contribuições no período
     */
    public List<Contribuicao> buscarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }
        
        return contribuicaoRepository.findByPeriodo(dataInicio, dataFim);
    }
    
    /**
     * Busca contribuições por forma de pagamento
     * @param formaPagamento Forma de pagamento
     * @return Lista de contribuições com a forma de pagamento especificada
     */
    public List<Contribuicao> buscarPorFormaPagamento(String formaPagamento) {
        if (formaPagamento == null || formaPagamento.trim().isEmpty()) {
            throw new IllegalArgumentException("Forma de pagamento para busca não pode ser vazia");
        }
        
        return contribuicaoRepository.findByFormaPagamento(formaPagamento);
    }
    
    /**
     * Busca contribuições por valor
     * @param valorMinimo Valor mínimo
     * @param valorMaximo Valor máximo
     * @return Lista de contribuições com valor no intervalo especificado
     */
    public List<Contribuicao> buscarPorValor(BigDecimal valorMinimo, BigDecimal valorMaximo) {
        if (valorMinimo == null || valorMaximo == null) {
            throw new IllegalArgumentException("Valores mínimo e máximo são obrigatórios");
        }
        
        if (valorMaximo.compareTo(valorMinimo) < 0) {
            throw new IllegalArgumentException("Valor máximo deve ser maior ou igual ao valor mínimo");
        }
        
        return contribuicaoRepository.findByValor(valorMinimo, valorMaximo);
    }
    
    /**
     * Calcula o total de contribuições por trabalhador
     * @param trabalhadorId ID do trabalhador
     * @return Valor total das contribuições
     */
    public BigDecimal calcularTotalPorTrabalhador(Long trabalhadorId) {
        if (trabalhadorId == null) {
            throw new IllegalArgumentException("ID do trabalhador não pode ser nulo");
        }
        
        return contribuicaoRepository.calcularTotalPorTrabalhador(trabalhadorId);
    }
    
    /**
     * Remove uma contribuição do banco de dados
     * @param id ID da contribuição a ser removida
     * @return true se removida com sucesso, false caso contrário
     */
    public boolean remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da contribuição não pode ser nulo");
        }
        
        // Verificar se contribuição existe
        Optional<Contribuicao> contribuicaoExistente = contribuicaoRepository.findById(id);
        if (!contribuicaoExistente.isPresent()) {
            throw new IllegalArgumentException("Contribuição não encontrada com o ID: " + id);
        }
        
        return contribuicaoRepository.delete(id);
    }
    
    public BigDecimal calcularTotalGeral() {
        return contribuicaoRepository.calcularTotalGeral();
    }
}
