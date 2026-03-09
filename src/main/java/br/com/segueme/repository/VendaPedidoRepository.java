package br.com.segueme.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import br.com.segueme.entity.VendaPedido;
import br.com.segueme.entity.enums.StatusPedido;

/**
 * Repository para operações de persistência da entidade VendaPedido
 */
public interface VendaPedidoRepository {
    
    /**
     * Salva um pedido no banco de dados
     * @param pedido Pedido a ser salvo
     * @return Pedido salvo com ID e número gerados
     */
    VendaPedido save(VendaPedido pedido);
    
    /**
     * Atualiza um pedido existente
     * @param pedido Pedido a ser atualizado
     * @return Pedido atualizado
     */
    VendaPedido update(VendaPedido pedido);
    
    /**
     * Busca um pedido pelo ID com todos os relacionamentos carregados
     * @param id ID do pedido
     * @return Optional contendo o pedido, se encontrado
     */
    Optional<VendaPedido> findById(Long id);
    
    /**
     * Busca um pedido pelo número
     * @param numeroPedido Número do pedido
     * @return Optional contendo o pedido, se encontrado
     */
    Optional<VendaPedido> findByNumeroPedido(String numeroPedido);
    
    /**
     * Busca todos os pedidos
     * @return Lista de todos os pedidos
     */
    List<VendaPedido> findAll();
    
    /**
     * Busca pedidos por encontro
     * @param encontroId ID do encontro
     * @return Lista de pedidos do encontro
     */
    List<VendaPedido> findByEncontro(Long encontroId);
    
    /**
     * Busca pedidos por trabalhador responsável
     * @param trabalhadorId ID do trabalhador
     * @return Lista de pedidos do trabalhador
     */
    List<VendaPedido> findByTrabalhador(Long trabalhadorId);
    
    /**
     * Busca pedidos por status
     * @param status Status do pedido
     * @return Lista de pedidos com o status especificado
     */
    List<VendaPedido> findByStatus(StatusPedido status);
    
    /**
     * Busca pedidos abertos de um encontro
     * @param encontroId ID do encontro
     * @return Lista de pedidos abertos do encontro
     */
    List<VendaPedido> findAbertosByEncontro(Long encontroId);
    
    /**
     * Busca pedidos aguardando pagamento de um encontro
     * @param encontroId ID do encontro
     * @return Lista de pedidos aguardando pagamento
     */
    List<VendaPedido> findAguardoPagamentoByEncontro(Long encontroId);
    
    /**
     * Busca pedidos por encontro e status
     * @param encontroId ID do encontro
     * @param status Status do pedido
     * @return Lista de pedidos
     */
    List<VendaPedido> findByEncontroAndStatus(Long encontroId, StatusPedido status);
    
    /**
     * Busca pedidos por período
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Lista de pedidos no período
     */
    List<VendaPedido> findByPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim);
    
    /**
     * Busca pedidos relacionados a uma pessoa (via venda_pedido_pessoa)
     * @param pessoaId ID da pessoa
     * @return Lista de pedidos associados à pessoa
     */
    List<VendaPedido> findByPessoa(Long pessoaId);
    
    /**
     * Remove um pedido
     * @param id ID do pedido a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    boolean delete(Long id);
}
