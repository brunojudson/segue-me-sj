package br.com.segueme.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.entity.Trabalhador;
import br.com.segueme.entity.VendaArtigo;
import br.com.segueme.entity.VendaItemPedido;
import br.com.segueme.entity.VendaPedido;
import br.com.segueme.entity.VendaPedidoPessoa;
import br.com.segueme.entity.enums.StatusPedido;
import br.com.segueme.entity.enums.TipoAssociacaoPedido;
import br.com.segueme.repository.VendaPedidoRepository;

/**
 * Service para gerenciar pedidos de venda
 * Contém todas as regras de negócio do módulo de vendas
 */
@ApplicationScoped
public class VendaPedidoService implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private VendaPedidoRepository pedidoRepository;
    
    @Inject
    private EncontroService encontroService;
    
    @Inject
    private TrabalhadorService trabalhadorService;
    
    @Inject
    private VendaArtigoService artigoService;
    
    /**
     * Inicia uma nova venda/conta aberta
     * REGRA: Só pode iniciar venda se houver um encontro ativo
     */
    public VendaPedido iniciarNovaVenda(Long encontroId, Long trabalhadorId) {
        // Validar parâmetros
        if (encontroId == null) {
            throw new IllegalArgumentException("Encontro é obrigatório para iniciar uma venda");
        }
        if (trabalhadorId == null) {
            throw new IllegalArgumentException("Trabalhador responsável é obrigatório");
        }
        
        // REGRA DE NEGÓCIO: Verificar se o encontro está ativo
        Optional<Encontro> optEncontro = encontroService.buscarPorId(encontroId);
        if (!optEncontro.isPresent()) {
            throw new IllegalArgumentException("Encontro não encontrado com ID: " + encontroId);
        }
        
        Encontro encontro = optEncontro.get();
        if (encontro.getAtivo() == null || !encontro.getAtivo()) {
            throw new IllegalStateException("Não há encontro ativo. Ative um encontro para iniciar vendas.");
        }
        
        // Verificar se trabalhador existe
        Optional<Trabalhador> optTrabalhador = trabalhadorService.buscarPorId(trabalhadorId);
        if (!optTrabalhador.isPresent()) {
            throw new IllegalArgumentException("Trabalhador não encontrado com ID: " + trabalhadorId);
        }
        
        // REGRA: se já existe um pedido ABERTO para este encontro e trabalhador, reutilizar
        List<VendaPedido> abertosNoEncontro = pedidoRepository.findByEncontroAndStatus(encontroId, StatusPedido.ABERTO);
        for (VendaPedido p : abertosNoEncontro) {
            if (p.getTrabalhadorResponsavel() != null && p.getTrabalhadorResponsavel().getId().equals(trabalhadorId)) {
                // Retornar pedido existente (já com associações via JOIN FETCH)
                return p;
            }
        }

        // Caso não exista, criar novo pedido ABERTO
        VendaPedido pedido = new VendaPedido(encontro, optTrabalhador.get());
        pedido.setStatus(StatusPedido.ABERTO);

        return pedidoRepository.save(pedido);
    }

    /**
     * Busca um pedido aberto por encontro e trabalhador, se existir
     */
    public java.util.Optional<VendaPedido> buscarPedidoAbertoPorEncontroETrabalhador(Long encontroId, Long trabalhadorId) {
        if (encontroId == null || trabalhadorId == null) {
            return java.util.Optional.empty();
        }
        List<VendaPedido> abertos = pedidoRepository.findByEncontroAndStatus(encontroId, StatusPedido.ABERTO);
        for (VendaPedido p : abertos) {
            if (p.getTrabalhadorResponsavel() != null && p.getTrabalhadorResponsavel().getId().equals(trabalhadorId)) {
                return java.util.Optional.of(p);
            }
        }
        return java.util.Optional.empty();
    }
    
    /**
     * Adiciona um item a um pedido aberto
     * REGRA: Só pode adicionar itens a pedidos com status ABERTO
     */
    public VendaPedido adicionarItem(Long pedidoId, Long artigoId, Integer quantidade) {
        return adicionarItem(pedidoId, artigoId, quantidade, null);
    }
    
    /**
     * Adiciona um item a um pedido aberto com valor unitário customizado
     */
    public VendaPedido adicionarItem(Long pedidoId, Long artigoId, Integer quantidade, BigDecimal valorUnitario) {
        // Validações
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido é obrigatório");
        }
        if (artigoId == null) {
            throw new IllegalArgumentException("Artigo é obrigatório");
        }
        if (quantidade == null || quantidade < 1) {
            throw new IllegalArgumentException("Quantidade do item deve ser maior que zero");
        }
        
        // Buscar pedido
        Optional<VendaPedido> optPedido = pedidoRepository.findById(pedidoId);
        if (!optPedido.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado com ID: " + pedidoId);
        }
        
        VendaPedido pedido = optPedido.get();
        
        // REGRA: Verificar se pedido permite alteração
        if (!pedido.getStatus().permiteAlteracao()) {
            throw new IllegalStateException("Não é possível adicionar itens a um pedido com status: " + pedido.getStatus().getDescricao());
        }
        
        // Buscar artigo
        Optional<VendaArtigo> optArtigo = artigoService.buscarPorId(artigoId);
        if (!optArtigo.isPresent()) {
            throw new IllegalArgumentException("Artigo não encontrado com ID: " + artigoId);
        }
        
        VendaArtigo artigo = optArtigo.get();
        
        // Verificar se artigo está ativo
        if (!artigo.getAtivo()) {
            throw new IllegalStateException("Artigo está inativo e não pode ser vendido");
        }
        
        // Determinar valor unitário
        BigDecimal valorUnit = valorUnitario != null ? valorUnitario : artigo.getPrecoBase();
        
        if (valorUnit == null || valorUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor unitário não pode ser negativo");
        }
        
        // Verificar estoque disponível antes de adicionar
        if (artigo.getEstoqueAtual() != null && artigo.getEstoqueAtual() < quantidade) {
            throw new IllegalStateException("Estoque insuficiente para o artigo: " + artigo.getNome());
        }

        // Criar item e adicionar ao pedido
        VendaItemPedido item = new VendaItemPedido(pedido, artigo, quantidade, valorUnit);
        pedido.adicionarItem(item);

        // Debitar estoque do artigo e persistir alteração
        artigo.debitarEstoque(quantidade);
        artigoService.atualizar(artigo);

        // Atualizar pedido (o valor total será recalculado pelo trigger do banco)
        pedidoRepository.update(pedido);
        
        // Recarregar pedido com todas as associações inicializadas via JOIN FETCH
        return pedidoRepository.findById(pedidoId).orElse(pedido);
    }
    
    /**
     * Remove um item de um pedido aberto
     */
    public VendaPedido removerItem(Long pedidoId, Long itemId) {
        if (pedidoId == null || itemId == null) {
            throw new IllegalArgumentException("ID do pedido e do item são obrigatórios");
        }
        
        Optional<VendaPedido> optPedido = pedidoRepository.findById(pedidoId);
        if (!optPedido.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }
        
        VendaPedido pedido = optPedido.get();
        
        if (!pedido.getStatus().permiteAlteracao()) {
            throw new IllegalStateException("Não é possível remover itens de um pedido com status: " + pedido.getStatus().getDescricao());
        }
        
        // Buscar e remover item
        VendaItemPedido itemRemover = null;
        for (VendaItemPedido item : pedido.getItens()) {
            if (item.getId().equals(itemId)) {
                itemRemover = item;
                break;
            }
        }
        
        if (itemRemover == null) {
            throw new IllegalArgumentException("Item não encontrado no pedido");
        }
        
        pedido.removerItem(itemRemover);

        // Creditar estoque do artigo removido
        VendaArtigo artigoRem = itemRemover.getArtigo();
        if (artigoRem != null) {
            artigoRem.creditarEstoque(itemRemover.getQuantidade());
            artigoService.atualizar(artigoRem);
        }

        // Atualiza pedido e garante remoção do item no DB (caso orphanRemoval não tenha funcionado)
        pedidoRepository.update(pedido);
        pedidoRepository.deleteItemById(itemId);
        
        // Recarregar pedido com todas as associações inicializadas via JOIN FETCH
        return pedidoRepository.findById(pedidoId).orElse(pedido);
    }
    
    /**
     * Associa uma pessoa ao pedido
     * Permite criar contas compartilhadas ou unir contas
     */
    public VendaPedido associarPessoa(Long pedidoId, Long pessoaId, TipoAssociacaoPedido tipoAssociacao, BigDecimal percentualRateio) {
        if (pedidoId == null || pessoaId == null) {
            throw new IllegalArgumentException("Pedido e pessoa são obrigatórios");
        }
        
        Optional<VendaPedido> optPedido = pedidoRepository.findById(pedidoId);
        if (!optPedido.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }
        
        VendaPedido pedido = optPedido.get();
        
        // Buscar pessoa (pode usar PessoaService se necessário)
        // Por ora, vamos assumir que a validação será feita na camada de repository
        
        VendaPedidoPessoa associacao = new VendaPedidoPessoa();
        associacao.setPedido(pedido);
        // associacao.setPessoa(pessoa); - seria necessário buscar a pessoa
        associacao.setTipoAssociacao(tipoAssociacao != null ? tipoAssociacao : TipoAssociacaoPedido.CONSUMIDOR);
        associacao.setPercentualRateio(percentualRateio);
        
        pedido.associarPessoa(associacao);
        pedidoRepository.update(pedido);
        
        // Recarregar pedido com todas as associações inicializadas via JOIN FETCH
        return pedidoRepository.findById(pedidoId).orElse(pedido);
    }
    
    /**
     * Fecha um pedido aberto
     * REGRA: Deve ter pelo menos um item antes de fechar
     * REGRA: Deve informar se será pago agora ou não
     */
    public VendaPedido fecharPedido(Long pedidoId, Long fechadoPorTrabalhadorId, boolean pago, String formaPagamento) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido é obrigatório");
        }
        if (fechadoPorTrabalhadorId == null) {
            throw new IllegalArgumentException("Trabalhador que está fechando o pedido é obrigatório");
        }
        
        Optional<VendaPedido> optPedido = pedidoRepository.findById(pedidoId);
        if (!optPedido.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }
        
        VendaPedido pedido = optPedido.get();
        
        // REGRA: Verificar se pedido tem itens
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new IllegalStateException("A venda precisa ter pelo menos um item antes de ser fechada.");
        }
        
        // Buscar trabalhador que está fechando
        Optional<Trabalhador> optTrabalhador = trabalhadorService.buscarPorId(fechadoPorTrabalhadorId);
        if (!optTrabalhador.isPresent()) {
            throw new IllegalArgumentException("Trabalhador não encontrado");
        }
        
        if (pago && (formaPagamento == null || formaPagamento.trim().isEmpty())) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória quando o pedido é marcado como pago");
        }
        
        // Fechar pedido
        pedido.fechar(optTrabalhador.get(), pago, formaPagamento);
        
        pedidoRepository.update(pedido);
        
        // Recarregar pedido com todas as associações inicializadas via JOIN FETCH
        return pedidoRepository.findById(pedidoId).orElse(pedido);
    }
    
    /**
     * Marca um pedido como pago
     * REGRA: Apenas pedidos AGUARDANDO_PAGAMENTO podem ser marcados como pagos
     */
    public VendaPedido marcarComoPago(Long pedidoId, BigDecimal valorPago, String formaPagamento) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido é obrigatório");
        }
        if (formaPagamento == null || formaPagamento.trim().isEmpty()) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória");
        }
        
        Optional<VendaPedido> optPedido = pedidoRepository.findById(pedidoId);
        if (!optPedido.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }
        
        VendaPedido pedido = optPedido.get();
        
        BigDecimal valor = valorPago != null ? valorPago : pedido.getValorTotal();
        pedido.marcarComoPago(valor, formaPagamento);
        
        pedidoRepository.update(pedido);
        
        // Recarregar pedido com todas as associações inicializadas via JOIN FETCH
        return pedidoRepository.findById(pedidoId).orElse(pedido);
    }
    
    /**
     * Cancela um pedido
     */
    public VendaPedido cancelarPedido(Long pedidoId) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido é obrigatório");
        }
        
        Optional<VendaPedido> optPedido = pedidoRepository.findById(pedidoId);
        if (!optPedido.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }
        
        VendaPedido pedido = optPedido.get();
        // Ao cancelar, devolver itens ao estoque
        for (VendaItemPedido item : pedido.getItens()) {
            VendaArtigo art = item.getArtigo();
            if (art != null && item.getQuantidade() != null && item.getQuantidade() > 0) {
                art.creditarEstoque(item.getQuantidade());
                artigoService.atualizar(art);
            }
        }

        pedido.cancelar();

        pedidoRepository.update(pedido);
        
        // Recarregar pedido com todas as associações inicializadas via JOIN FETCH
        return pedidoRepository.findById(pedidoId).orElse(pedido);
    }
    
    /**
     * Reabre um pedido que está aguardando pagamento
     */
    public VendaPedido reabrirPedido(Long pedidoId) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido é obrigatório");
        }
        
        Optional<VendaPedido> optPedido = pedidoRepository.findById(pedidoId);
        if (!optPedido.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }
        
        VendaPedido pedido = optPedido.get();
        pedido.reabrir();
        
        pedidoRepository.update(pedido);
        
        // Recarregar pedido com todas as associações inicializadas via JOIN FETCH
        return pedidoRepository.findById(pedidoId).orElse(pedido);
    }
    
    // Métodos de consulta
    
    public Optional<VendaPedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }
    
    public List<VendaPedido> buscarTodos() {
        return pedidoRepository.findAll();
    }
    
    public List<VendaPedido> buscarPorEncontro(Long encontroId) {
        return pedidoRepository.findByEncontro(encontroId);
    }
    
    public List<VendaPedido> buscarPorTrabalhador(Long trabalhadorId) {
        return pedidoRepository.findByTrabalhador(trabalhadorId);
    }
    
    public List<VendaPedido> buscarPorStatus(StatusPedido status) {
        return pedidoRepository.findByStatus(status);
    }

    public List<VendaPedido> buscarPorEncontroAndStatus(Long encontroId, StatusPedido status) {
        return pedidoRepository.findByEncontroAndStatus(encontroId, status);
    }
    
    public List<VendaPedido> buscarAbertosPorEncontro(Long encontroId) {
        return pedidoRepository.findAbertosByEncontro(encontroId);
    }
    
    public List<VendaPedido> buscarAguardoPagamentoPorEncontro(Long encontroId) {
        return pedidoRepository.findAguardoPagamentoByEncontro(encontroId);
    }
    
    public List<VendaPedido> buscarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return pedidoRepository.findByPeriodo(dataInicio, dataFim);
    }
    
    /**
     * Busca todo o consumo de um trabalhador em um encontro
     */
    public List<VendaPedido> consultarConsumoPorTrabalhador(Long trabalhadorId, Long encontroId) {
        List<VendaPedido> pedidos = pedidoRepository.findByTrabalhador(trabalhadorId);
        
        // Filtrar por encontro se especificado
        if (encontroId != null) {
            pedidos.removeIf(p -> !p.getEncontro().getId().equals(encontroId));
        }
        
        return pedidos;
    }
}
