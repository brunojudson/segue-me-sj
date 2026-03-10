package br.com.segueme.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.entity.Trabalhador;
import br.com.segueme.entity.VendaArtigo;
import br.com.segueme.entity.VendaItemPedido;
import br.com.segueme.entity.VendaPedido;
import br.com.segueme.entity.enums.StatusPedido;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.PessoaService;
import br.com.segueme.service.TrabalhadorService;
import br.com.segueme.service.VendaArtigoService;
import br.com.segueme.service.VendaPedidoService;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.pie.PieChartOptions;
import org.primefaces.model.charts.bar.BarChartOptions;

/**
 * Managed Bean para gerenciar pedidos de venda
 */
@Named
@ViewScoped
public class VendaPedidoController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private VendaPedidoService pedidoService;
    
    @Inject
    private VendaArtigoService artigoService;
    
    @Inject
    private EncontroService encontroService;
    
    @Inject
    private TrabalhadorService trabalhadorService;
    
    @Inject
    private PessoaService pessoaService;
    
    // Listas
    private List<VendaPedido> pedidos;
    private List<VendaPedido> pedidosAbertos;
    private List<VendaPedido> pedidosAguardandoPagamento;
    private List<Encontro> encontros;
    private List<Trabalhador> trabalhadores;
    private List<VendaArtigo> artigos;
    private List<Pessoa> pessoas;
    
    // Pedido atual
    private VendaPedido pedidoAtual;
    private VendaPedido pedidoSelecionado;
    // Auxiliar para seleção via f:setPropertyActionListener
    private Long pedidoIdToSelect;
    
    // Seleções para criar novo pedido / adicionar item
    private Long encontroSelecionadoId;
    private Long trabalhadorSelecionadoId;
    private Long artigoSelecionadoId;
    private Integer quantidadeItem;
    private BigDecimal valorUnitarioItem;
    
    // Fechamento de pedido
    private boolean pagarAgora;
    private String formaPagamento;
    private BigDecimal valorPagamento;
    
    // Filtros
    private Long filtroEncontroId;
    private StatusPedido filtroStatus;
    
    @PostConstruct
    public void init() {
        carregarListas();
        limpar();
        verificarEncontroAtivo();
    }
    
    /**
     * Verifica se há um encontro ativo
     */
    public void verificarEncontroAtivo() {
        try {
            List<Encontro> encontrosAtivos = encontroService.buscarAtivos();
            if (encontrosAtivos.isEmpty()) {
                addWarnMessage("Não há encontro ativo. Ative um encontro para iniciar vendas.");
            } else if (encontrosAtivos.size() == 1) {
                // Se houver apenas um encontro ativo, seleciona automaticamente
                encontroSelecionadoId = encontrosAtivos.get(0).getId();
                carregarPedidosAbertosPorEncontro();
            }
        } catch (Exception e) {
            addErrorMessage("Erro ao verificar encontros ativos: " + e.getMessage());
        }
    }
    
    public void carregarListas() {
        try {
            pedidos = pedidoService.buscarTodos();
            encontros = encontroService.buscarTodos();
            trabalhadores = trabalhadorService.buscarTodosAtivos();
            artigos = artigoService.buscarAtivos();
            pessoas = pessoaService.buscarTodos();
        } catch (Exception e) {
            addErrorMessage("Erro ao carregar listas: " + e.getMessage());
        }
    }
    
    public void carregarPedidosAbertosPorEncontro() {
        if (encontroSelecionadoId != null) {
            try {
                pedidosAbertos = pedidoService.buscarAbertosPorEncontro(encontroSelecionadoId);
                pedidosAguardandoPagamento = pedidoService.buscarAguardoPagamentoPorEncontro(encontroSelecionadoId);
            } catch (Exception e) {
                addErrorMessage("Erro ao carregar pedidos: " + e.getMessage());
            }
        }
    }

    public void onEncontroChanged() {
        carregarPedidosAbertosPorEncontro();
    }
    
    public void limpar() {
        pedidoAtual = null;
        artigoSelecionadoId = null;
        quantidadeItem = 1;
        valorUnitarioItem = null;
        pagarAgora = false;
        formaPagamento = null;
        valorPagamento = null;
    }
    
    /**
     * Inicia uma nova venda (Alt+N no teclado)
     */
    public void iniciarNovaVenda() {
        try {
            // diagnóstico rápido para ajudar na depuração em tempo de execução
            addInfoMessage("Iniciando fluxo de nova venda (debug)...");
            if (encontroSelecionadoId == null) {
                addWarnMessage("Selecione um encontro para iniciar a venda");
                return;
            }
            
            if (trabalhadorSelecionadoId == null) {
                addWarnMessage("Selecione o trabalhador responsável pela conta");
                return;
            }
            
            // Se já existe um pedido aberto para este trabalhador no encontro selecionado, reutiliza
            java.util.Optional<VendaPedido> optExistente = pedidoService.buscarPedidoAbertoPorEncontroETrabalhador(encontroSelecionadoId, trabalhadorSelecionadoId);
            if (optExistente.isPresent()) {
                pedidoAtual = optExistente.get();
                addInfoMessage("Pedido aberto encontrado e reutilizado. Número: " + pedidoAtual.getNumeroPedido());
            } else {
                pedidoAtual = pedidoService.iniciarNovaVenda(encontroSelecionadoId, trabalhadorSelecionadoId);
                addInfoMessage("Nova venda iniciada! Número: " + pedidoAtual.getNumeroPedido());
            }
            carregarPedidosAbertosPorEncontro();
            
        } catch (IllegalStateException e) {
            // Erro de regra de negócio (ex: encontro não ativo)
            addErrorMessage(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            addErrorMessage("Erro ao iniciar nova venda: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Adicionar item ao pedido aberto (Alt+I no teclado)
     */
    public void adicionarItem() {
        try {
            if (pedidoAtual == null) {
                addWarnMessage("Selecione ou inicie um pedido primeiro");
                return;
            }
            
            if (artigoSelecionadoId == null) {
                addWarnMessage("Selecione um artigo");
                return;
            }
            
            if (quantidadeItem == null || quantidadeItem < 1) {
                addWarnMessage("Quantidade do item deve ser maior que zero");
                return;
            }
            
            pedidoAtual = pedidoService.adicionarItem(
                pedidoAtual.getId(), 
                artigoSelecionadoId, 
                quantidadeItem, 
                valorUnitarioItem
            );
            
            addInfoMessage("Item adicionado ao pedido com sucesso!");
            
            // Limpar seleção
            artigoSelecionadoId = null;
            quantidadeItem = 1;
            valorUnitarioItem = null;
            
            // Recarregar lista de pedidos abertos para garantir associações inicializadas
            carregarPedidosAbertosPorEncontro();
            
        } catch (Exception e) {
            addErrorMessage("Erro ao adicionar item: " + e.getMessage());
        }
    }
    
    /**
     * Remove um item do pedido
     */
    public void removerItem(VendaItemPedido item) {
        try {
            if (pedidoAtual == null || item == null) {
                return;
            }
            
            pedidoAtual = pedidoService.removerItem(pedidoAtual.getId(), item.getId());
            addInfoMessage("Item removido do pedido");
            
        } catch (Exception e) {
            addErrorMessage("Erro ao remover item: " + e.getMessage());
        }
    }
    
    /**
     * Remove um item do pedido por ID (usado pela XHTML)
     */
    public void removerItem(Long itemId) {
        try {
            if (pedidoAtual == null || itemId == null) {
                return;
            }
            
            pedidoAtual = pedidoService.removerItem(pedidoAtual.getId(), itemId);
            addInfoMessage("Item removido do pedido");
            
        } catch (Exception e) {
            addErrorMessage("Erro ao remover item: " + e.getMessage());
        }
    }
    
    /**
     * Fechar pedido/conta (Alt+F no teclado)
     */
    public void fecharPedido() {
        try {
            if (pedidoAtual == null) {
                addWarnMessage("Nenhum pedido selecionado");
                return;
            }
            
            if (pagarAgora && (formaPagamento == null || formaPagamento.trim().isEmpty())) {
                addWarnMessage("Informe a forma de pagamento");
                return;
            }
            
            // Obter trabalhador logado (assumindo que há um LoginController)
            // Por ora, vamos usar o trabalhador responsável
            Long fechadoPorId = pedidoAtual.getTrabalhadorResponsavel().getId();
            
            pedidoAtual = pedidoService.fecharPedido(
                pedidoAtual.getId(), 
                fechadoPorId, 
                pagarAgora, 
                formaPagamento
            );
            
            String mensagem = pagarAgora ? 
                "Pedido fechado e pago com sucesso!" : 
                "Pedido fechado. Aguardando pagamento.";
            addInfoMessage(mensagem);
            
            carregarPedidosAbertosPorEncontro();
            limpar();
            
        } catch (Exception e) {
            addErrorMessage("Erro ao fechar pedido: " + e.getMessage());
        }
    }
    
    /**
     * Marcar pedido como pago (Alt+P no teclado)
     */
    public void marcarComoPago() {
        try {
            if (pedidoSelecionado == null) {
                addWarnMessage("Selecione um pedido");
                return;
            }
            
            if (formaPagamento == null || formaPagamento.trim().isEmpty()) {
                addWarnMessage("Informe a forma de pagamento");
                return;
            }
            
            BigDecimal valor = valorPagamento != null ? valorPagamento : pedidoSelecionado.getValorTotal();
            
            pedidoService.marcarComoPago(pedidoSelecionado.getId(), valor, formaPagamento);
            addInfoMessage("Pedido marcado como pago!");
            
            carregarPedidosAbertosPorEncontro();
            limpar();
            
        } catch (Exception e) {
            addErrorMessage("Erro ao marcar pedido como pago: " + e.getMessage());
        }
    }
    
    /**
     * Seleciona um pedido existente para edição
     */
    public void selecionarPedido(VendaPedido pedido) {
        if (pedido == null || pedido.getId() == null) {
            this.pedidoAtual = null;
            return;
        }
        // Recarrega pedido completo via service para garantir que coleções lazy sejam inicializadas
        try {
            VendaPedido pedidoRecarregado = pedidoService.buscarPorId(pedido.getId()).orElse(pedido);
            this.pedidoAtual = pedidoRecarregado;
            addInfoMessage("Pedido selecionado: " + this.pedidoAtual.getNumeroPedido());
        } catch (Exception e) {
            e.printStackTrace();
            this.pedidoAtual = pedido; // fallback: mantém o objeto original
            addErrorMessage("Erro ao carregar pedido: " + e.getMessage());
        }
    }

    /**
     * Seleciona pedido por ID — usa quando o objeto da linha pode estar detached/proxy
     */
    public void selecionarPedidoPorId(Long id) {
        if (id == null) {
            this.pedidoAtual = null;
            return;
        }
        try {
            VendaPedido pedidoRecarregado = pedidoService.buscarPorId(id).orElse(null);
            if (pedidoRecarregado != null) {
                this.pedidoAtual = pedidoRecarregado;
                addInfoMessage("Pedido selecionado: " + this.pedidoAtual.getNumeroPedido());
            } else {
                addErrorMessage("Pedido não encontrado ao recarregar ID: " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Erro ao carregar pedido: " + e.getMessage());
        }
    }

    /**
     * Action wrapper usado por commandButtons que não suportam passagem direta de parâmetro.
     */
    public void selecionarPedidoPorIdAction() {
        selecionarPedidoPorId(this.pedidoIdToSelect);
    }

    public Long getPedidoIdToSelect() {
        return pedidoIdToSelect;
    }

    public void setPedidoIdToSelect(Long pedidoIdToSelect) {
        this.pedidoIdToSelect = pedidoIdToSelect;
    }
    
    /**
     * Seleciona pedido (usado pelo ajax rowSelect)
     */
    public void selecionarPedido() {
        // O pedidoAtual já foi setado pelo selection binding; recarrega via service
        if (this.pedidoAtual == null || this.pedidoAtual.getId() == null) {
            return;
        }
        try {
            VendaPedido pedidoRecarregado = pedidoService.buscarPorId(this.pedidoAtual.getId()).orElse(this.pedidoAtual);
            this.pedidoAtual = pedidoRecarregado;
                int itensCount = this.pedidoAtual.getItens() != null ? this.pedidoAtual.getItens().size() : 0;
                addInfoMessage("Pedido selecionado via rowSelect: " + this.pedidoAtual.getNumeroPedido() + " (itens=" + itensCount + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Prepara dialog para marcar como pago
     */
    public void prepararMarcarComoPago(VendaPedido pedido) {
        this.pedidoSelecionado = pedido;
        this.valorPagamento = pedido.getValorTotal();
        this.formaPagamento = null;
    }
    
    /**
     * Cancela um pedido
     */
    public void cancelarPedido(VendaPedido pedido) {
        try {
            pedidoService.cancelarPedido(pedido.getId());
            addInfoMessage("Pedido cancelado com sucesso!");
            carregarPedidosAbertosPorEncontro();
        } catch (Exception e) {
            addErrorMessage("Erro ao cancelar pedido: " + e.getMessage());
        }
    }
    
    /**
     * Cancela um pedido por ID (usado pela XHTML)
     */
    public void cancelarPedido(Long pedidoId) {
        try {
            pedidoService.cancelarPedido(pedidoId);
            addInfoMessage("Pedido cancelado com sucesso!");
            carregarPedidosAbertosPorEncontro();
        } catch (Exception e) {
            addErrorMessage("Erro ao cancelar pedido: " + e.getMessage());
        }
    }
    
    /**
     * Visualiza detalhes de um pedido (para dialog)
     */
    public void visualizarDetalhes(VendaPedido pedido) {
        if (pedido == null || pedido.getId() == null) {
            this.pedidoSelecionado = null;
            return;
        }
        try {
            VendaPedido recarregado = pedidoService.buscarPorId(pedido.getId()).orElse(pedido);
            this.pedidoSelecionado = recarregado;
            // Também define como pedidoAtual para permitir ações de fechamento direto
            this.pedidoAtual = recarregado;
            // resetar flags de pagamento
            this.pagarAgora = false;
            this.formaPagamento = null;
        } catch (Exception e) {
            this.pedidoSelecionado = pedido;
            this.pedidoAtual = pedido;
        }
    }
    
    /**
     * Limpa todos os filtros
     */
    public void limparFiltros() {
        filtroEncontroId = null;
        filtroStatus = null;
        dataInicio = null;
        dataFim = null;
        carregarListas();
    }
    
    /**
     * Atualiza o valor unitário quando o artigo é selecionado
     */
    public void onArtigoSelecionado() {
        if (artigoSelecionadoId != null) {
            artigoService.buscarPorId(artigoSelecionadoId).ifPresent(artigo -> {
                valorUnitarioItem = artigo.getPrecoBase();
            });
        }
    }
    
    /**
     * Filtra pedidos
     */
    public void filtrarPedidos() {
        try {
            // Se houver filtro por período, buscar por período e então aplicar outros filtros em memória
            if (dataInicio != null || dataFim != null) {
                LocalDateTime inicio = null;
                LocalDateTime fim = null;
                if (dataInicio != null) {
                    inicio = dataInicio.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
                }
                if (dataFim != null) {
                    fim = dataFim.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(LocalTime.MAX);
                }
                pedidos = pedidoService.buscarPorPeriodo(inicio, fim);

                if (filtroEncontroId != null) {
                    Long encId = filtroEncontroId;
                    pedidos.removeIf(p -> p.getEncontro() == null || !p.getEncontro().getId().equals(encId));
                }
                if (filtroStatus != null) {
                    pedidos.removeIf(p -> p.getStatus() == null || !p.getStatus().equals(filtroStatus));
                }
            } else if (filtroEncontroId != null && filtroStatus != null) {
                pedidos = pedidoService.buscarPorEncontroAndStatus(filtroEncontroId, filtroStatus);
            } else if (filtroEncontroId != null) {
                pedidos = pedidoService.buscarPorEncontro(filtroEncontroId);
            } else if (filtroStatus != null) {
                pedidos = pedidoService.buscarPorStatus(filtroStatus);
            } else {
                carregarListas();
            }
        } catch (Exception e) {
            addErrorMessage("Erro ao filtrar pedidos: " + e.getMessage());
        }
    }

    // Campos para filtros por período (bind com p:calendar usa java.util.Date)
    private Date dataInicio;
    private Date dataFim;

    public Date getDataInicio() { return dataInicio; }
    public void setDataInicio(Date dataInicio) { this.dataInicio = dataInicio; }

    public Date getDataFim() { return dataFim; }
    public void setDataFim(Date dataFim) { this.dataFim = dataFim; }

    // Propriedade para exibir status do encontro selecionado
    public boolean isEncontroAtivo() {
        if (encontroSelecionadoId == null) return false;
        try {
            return encontroService.buscarPorId(encontroSelecionadoId)
                    .map(e -> Boolean.TRUE.equals(e.getAtivo()))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    // Getter usado pela dialog de marcar como pago (compatibilidade com templates)
    public VendaPedido getPedidoParaPagamento() {
        return this.pedidoSelecionado;
    }

    // Lista de artigos ativos para seleção rápida
    public List<VendaArtigo> getArtigosAtivos() {
        try {
            return artigoService.buscarAtivos();
        } catch (Exception e) {
            return artigos;
        }
    }
    
    // Métodos auxiliares para mensagens
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", message));
    }
    
    private void addWarnMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", message));
    }
    
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", message));
    }
    
    // Getters e Setters (apenas os principais para exemplo)
    public List<VendaPedido> getPedidos() { return pedidos; }
    public void setPedidos(List<VendaPedido> pedidos) { this.pedidos = pedidos; }
    
    public List<VendaPedido> getPedidosAbertos() { return pedidosAbertos; }
    public void setPedidosAbertos(List<VendaPedido> pedidosAbertos) { this.pedidosAbertos = pedidosAbertos; }
    
    public List<VendaPedido> getPedidosAguardandoPagamento() { return pedidosAguardandoPagamento; }
    
    public VendaPedido getPedidoAtual() { return pedidoAtual; }
    public void setPedidoAtual(VendaPedido pedidoAtual) { this.pedidoAtual = pedidoAtual; }
    
    public VendaPedido getPedidoSelecionado() { return pedidoSelecionado; }
    public void setPedidoSelecionado(VendaPedido pedidoSelecionado) { this.pedidoSelecionado = pedidoSelecionado; }
    
    public List<Encontro> getEncontros() { return encontros; }
    public List<Trabalhador> getTrabalhadores() { return trabalhadores; }
    public List<VendaArtigo> getArtigos() { return artigos; }
    public List<Pessoa> getPessoas() { return pessoas; }
    
    public Long getEncontroSelecionadoId() { return encontroSelecionadoId; }
    public void setEncontroSelecionadoId(Long encontroSelecionadoId) { 
        this.encontroSelecionadoId = encontroSelecionadoId;
        carregarPedidosAbertosPorEncontro();
    }
    
    public Long getTrabalhadorSelecionadoId() { return trabalhadorSelecionadoId; }
    public void setTrabalhadorSelecionadoId(Long trabalhadorSelecionadoId) { this.trabalhadorSelecionadoId = trabalhadorSelecionadoId; }
    
    public Long getArtigoSelecionadoId() { return artigoSelecionadoId; }
    public void setArtigoSelecionadoId(Long artigoSelecionadoId) { this.artigoSelecionadoId = artigoSelecionadoId; }
    
    public Integer getQuantidadeItem() { return quantidadeItem; }
    public void setQuantidadeItem(Integer quantidadeItem) { this.quantidadeItem = quantidadeItem; }
    
    public BigDecimal getValorUnitarioItem() { return valorUnitarioItem; }
    public void setValorUnitarioItem(BigDecimal valorUnitarioItem) { this.valorUnitarioItem = valorUnitarioItem; }
    
    public boolean isPagarAgora() { return pagarAgora; }
    public void setPagarAgora(boolean pagarAgora) { this.pagarAgora = pagarAgora; }
    
    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }
    
    public BigDecimal getValorPagamento() { return valorPagamento; }
    public void setValorPagamento(BigDecimal valorPagamento) { this.valorPagamento = valorPagamento; }
    
    public Long getFiltroEncontroId() { return filtroEncontroId; }
    public void setFiltroEncontroId(Long filtroEncontroId) { this.filtroEncontroId = filtroEncontroId; }
    
    public StatusPedido getFiltroStatus() { return filtroStatus; }
    public void setFiltroStatus(StatusPedido filtroStatus) { this.filtroStatus = filtroStatus; }
    
    public StatusPedido[] getTodosStatus() { return StatusPedido.values(); }

    // Compatibilidade com as páginas XHTML que esperam essas propriedades
    public List<Encontro> getEncontrosAtivos() {
        try {
            return encontroService.buscarAtivos();
        } catch (Exception e) {
            return encontros; // retorna cache ou null em erro
        }
    }

    public List<Encontro> getTodosEncontros() {
        try {
            return encontroService.buscarTodos();
        } catch (Exception e) {
            return encontros;
        }
    }
    
    /**
     * Propriedades calculadas para totalizadores e relatórios
     */
    public List<VendaPedido> getPedidosFiltrados() {
        return pedidos != null ? pedidos : new ArrayList<>();
    }
    
    public int getTotalPedidos() {
        return pedidos != null ? pedidos.size() : 0;
    }
    
    public BigDecimal getTotalValorPago() {
        if (pedidos == null) return BigDecimal.ZERO;
        return pedidos.stream()
                .filter(p -> p.getStatus() == StatusPedido.PAGO)
                .map(VendaPedido::getValorPago)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalAguardandoPagamento() {
        if (pedidos == null) return BigDecimal.ZERO;
        return pedidos.stream()
                .filter(p -> p.getStatus() == StatusPedido.AGUARDO_PAGAMENTO)
                .map(VendaPedido::getValorTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Valor total de pedidos com status ABERTO (usado no card de totalizadores)
     */
    public BigDecimal getTotalPedidosAbertosValor() {
        if (pedidos == null) return BigDecimal.ZERO;
        return pedidos.stream()
                .filter(p -> p.getStatus() == StatusPedido.ABERTO)
                .map(VendaPedido::getValorTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getValorTotalGeral() {
        if (pedidos == null) return BigDecimal.ZERO;
        return pedidos.stream()
                .filter(p -> p.getStatus() != StatusPedido.CANCELADO)
                .map(VendaPedido::getValorTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public String getDataExportacao() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
    }

    // ===== GRÁFICOS =====

    /**
     * Gráfico de pizza: artigos mais vendidos por quantidade
     */
    public PieChartModel getGraficoArtigosMaisVendidos() {
        PieChartModel model = new PieChartModel();
        ChartData data = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();

        Map<String, Integer> artigosQtd = new HashMap<>();
        List<VendaPedido> lista = getPedidosFiltrados();

        for (VendaPedido p : lista) {
            if (p.getStatus() == StatusPedido.CANCELADO || p.getItens() == null) continue;
            for (VendaItemPedido item : p.getItens()) {
                String nome = item.getArtigo() != null ? item.getArtigo().getNome() : "Desconhecido";
                artigosQtd.merge(nome, item.getQuantidade(), Integer::sum);
            }
        }

        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> bgColors = new ArrayList<>();
        String[] cores = {
            "rgb(13, 150, 104)", "rgb(5, 150, 105)", "rgb(16, 185, 129)",
            "rgb(52, 211, 153)", "rgb(124, 58, 237)", "rgb(167, 139, 250)",
            "rgb(217, 119, 6)", "rgb(251, 191, 36)", "rgb(220, 38, 38)",
            "rgb(244, 114, 182)"
        };

        // Ordenar por quantidade decrescente e pegar top 10
        artigosQtd.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .forEach(entry -> {
                labels.add(entry.getKey());
                values.add(entry.getValue());
                bgColors.add(cores[labels.size() - 1 < cores.length ? labels.size() - 1 : 0]);
            });

        dataSet.setData(values);
        dataSet.setBackgroundColor(bgColors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        model.setData(data);
        model.setOptions(new PieChartOptions());
        return model;
    }

    /**
     * Gráfico de barras: receita por forma de pagamento
     */
    public BarChartModel getGraficoReceitaPorFormaPagamento() {
        BarChartModel model = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet dataSet = new BarChartDataSet();

        Map<String, BigDecimal> receitaPorForma = new HashMap<>();
        List<VendaPedido> lista = getPedidosFiltrados();

        for (VendaPedido p : lista) {
            if (p.getStatus() != StatusPedido.PAGO || p.getFormaPagamento() == null) continue;
            receitaPorForma.merge(p.getFormaPagamento(), 
                p.getValorPago() != null ? p.getValorPago() : BigDecimal.ZERO, 
                BigDecimal::add);
        }

        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> bgColors = new ArrayList<>();
        Map<String, String> formaLabels = new HashMap<>();
        formaLabels.put("DINHEIRO", "Dinheiro");
        formaLabels.put("PIX", "PIX");
        formaLabels.put("DEBITO", "Cartão Débito");
        formaLabels.put("CREDITO", "Cartão Crédito");

        String[] cores = {
            "rgba(13, 150, 104, 0.8)", "rgba(5, 150, 105, 0.8)",
            "rgba(124, 58, 237, 0.8)", "rgba(217, 119, 6, 0.8)"
        };

        int i = 0;
        for (Map.Entry<String, BigDecimal> entry : receitaPorForma.entrySet()) {
            labels.add(formaLabels.getOrDefault(entry.getKey(), entry.getKey()));
            values.add(entry.getValue());
            bgColors.add(cores[i % cores.length]);
            i++;
        }

        dataSet.setData(values);
        dataSet.setLabel("Receita (R$)");
        dataSet.setBackgroundColor(bgColors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        model.setData(data);
        model.setOptions(new BarChartOptions());
        return model;
    }

    /**
     * Gráfico de pizza: distribuição de pedidos por status
     */
    public PieChartModel getGraficoPedidosPorStatus() {
        PieChartModel model = new PieChartModel();
        ChartData data = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();

        Map<String, Integer> statusCount = new HashMap<>();
        List<VendaPedido> lista = getPedidosFiltrados();

        for (VendaPedido p : lista) {
            if (p.getStatus() == null) continue;
            statusCount.merge(p.getStatus().getDescricao(), 1, Integer::sum);
        }

        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> bgColors = new ArrayList<>();
        Map<String, String> statusCores = new HashMap<>();
        statusCores.put("Aberto", "rgb(59, 130, 246)");
        statusCores.put("Aguardando Pagamento", "rgb(217, 119, 6)");
        statusCores.put("Pago", "rgb(13, 150, 104)");
        statusCores.put("Cancelado", "rgb(220, 38, 38)");

        for (Map.Entry<String, Integer> entry : statusCount.entrySet()) {
            labels.add(entry.getKey());
            values.add(entry.getValue());
            bgColors.add(statusCores.getOrDefault(entry.getKey(), "rgb(108, 117, 125)"));
        }

        dataSet.setData(values);
        dataSet.setBackgroundColor(bgColors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        model.setData(data);
        model.setOptions(new PieChartOptions());
        return model;
    }

    /**
     * Gráfico de barras: quantidade e valor vendido por categoria
     */
    public BarChartModel getGraficoVendaPorCategoria() {
        BarChartModel model = new BarChartModel();
        ChartData data = new ChartData();

        Map<String, Integer> qtdPorCategoria = new HashMap<>();
        Map<String, java.math.BigDecimal> valorPorCategoria = new HashMap<>();
        List<VendaPedido> lista = getPedidosFiltrados();

        for (VendaPedido p : lista) {
            if (p.getStatus() == StatusPedido.CANCELADO || p.getItens() == null) continue;
            for (VendaItemPedido item : p.getItens()) {
                String categoria = (item.getArtigo() != null && item.getArtigo().getCategoria() != null)
                        ? item.getArtigo().getCategoria() : "Sem Categoria";
                qtdPorCategoria.merge(categoria, item.getQuantidade(), Integer::sum);
                java.math.BigDecimal valorItem = item.getValorTotalItem() != null ? item.getValorTotalItem() : java.math.BigDecimal.ZERO;
                valorPorCategoria.merge(categoria, valorItem, java.math.BigDecimal::add);
            }
        }

        // Ordenar categorias por valor total decrescente e limitar a top 10
        List<String> categorias = valorPorCategoria.entrySet().stream()
            .sorted(Map.Entry.<String, java.math.BigDecimal>comparingByValue().reversed())
            .limit(10)
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toList());

        List<Number> valoresQtd = new ArrayList<>();
        List<Number> valoresValor = new ArrayList<>();

        for (String cat : categorias) {
            valoresQtd.add(qtdPorCategoria.getOrDefault(cat, 0));
            valoresValor.add(valorPorCategoria.getOrDefault(cat, java.math.BigDecimal.ZERO));
        }

        BarChartDataSet dsQtd = new BarChartDataSet();
        dsQtd.setLabel("Quantidade");
        dsQtd.setBackgroundColor("rgba(59,130,246,0.8)");
        dsQtd.setData(valoresQtd);

        BarChartDataSet dsValor = new BarChartDataSet();
        dsValor.setLabel("Valor (R$)");
        dsValor.setBackgroundColor("rgba(16,185,129,0.8)");
        dsValor.setData(valoresValor);

        data.addChartDataSet(dsQtd);
        data.addChartDataSet(dsValor);
        data.setLabels(categorias);

        model.setData(data);
        model.setOptions(new BarChartOptions());
        return model;
    }
}
