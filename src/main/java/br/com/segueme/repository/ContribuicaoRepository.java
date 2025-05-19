package br.com.segueme.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import br.com.segueme.entity.Contribuicao;

public interface ContribuicaoRepository {
    
    /**
     * Salva uma contribuição no banco de dados
     * @param contribuicao Objeto contribuição a ser salvo
     * @return Contribuição salva com ID gerado
     */
    Contribuicao save(Contribuicao contribuicao);
    
    /**
     * Atualiza uma contribuição existente no banco de dados
     * @param contribuicao Objeto contribuição a ser atualizado
     * @return Contribuição atualizada
     */
    Contribuicao update(Contribuicao contribuicao);
    
    /**
     * Busca uma contribuição pelo ID
     * @param id ID da contribuição
     * @return Optional contendo a contribuição, se encontrada
     */
    Optional<Contribuicao> findById(Long id);
    
    /**
     * Busca todas as contribuições cadastradas
     * @return Lista de contribuições
     */
    List<Contribuicao> findAll();
    
    /**
     * Busca contribuições por trabalhador
     * @param trabalhadorId ID do trabalhador
     * @return Lista de contribuições do trabalhador
     */
    List<Contribuicao> findByTrabalhador(Long trabalhadorId);
    
    /**
     * Busca contribuições por período
     * @param dataInicio Data de início
     * @param dataFim Data de fim
     * @return Lista de contribuições no período
     */
    List<Contribuicao> findByPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim);
    
    /**
     * Busca contribuições por forma de pagamento
     * @param formaPagamento Forma de pagamento
     * @return Lista de contribuições com a forma de pagamento especificada
     */
    List<Contribuicao> findByFormaPagamento(String formaPagamento);
    
    /**
     * Busca contribuições por valor
     * @param valorMinimo Valor mínimo
     * @param valorMaximo Valor máximo
     * @return Lista de contribuições com valor no intervalo especificado
     */
    List<Contribuicao> findByValor(BigDecimal valorMinimo, BigDecimal valorMaximo);
    
    /**
     * Calcula o total de contribuições por trabalhador
     * @param trabalhadorId ID do trabalhador
     * @return Valor total das contribuições
     */
    BigDecimal calcularTotalPorTrabalhador(Long trabalhadorId);
    
    /**
     * Remove uma contribuição do banco de dados
     * @param id ID da contribuição a ser removida
     * @return true se removida com sucesso, false caso contrário
     */
    boolean delete(Long id);
    
    BigDecimal calcularTotalGeral();
}
