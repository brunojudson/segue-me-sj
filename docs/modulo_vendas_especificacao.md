# MÓDULO DE VENDAS - Sistema Segue-me-sj  
## Documentação Completa e Especificação Técnica

---

## 1. RESUMO DO ENTENDIMENTO DO SISTEMA ATUAL E OBJETIVO DO MÓDULO

### 1.1 Sistema Atual
O sistema "Segue-me" é uma aplicação Java EE para gerenciamento de encontros de jovens, com:
- **Tecnologias**: JSF + PrimeFaces, JPA/Hibernate, PostgreSQL, WildFly/JBoss
- **Arquitetura**: Camadas (Entity → Repository → Service → Controller → View)
- **Padrão repository/service**: Sem DAO, usando interfaces + implementações @Stateless
- **Entidades principais**: Pessoa, Trabalhador, Encontrista, Encontro, Equipe, Contribuicao, MovimentoFinanceiro
- **Conceito de Encontro Ativo**: O campo `encontro.ativo` controla qual encontro está em andamento

### 1.2 Objetivo do Novo Módulo de Vendas
Criar um módulo integrado para venda de artigos durante encontros, com:
- **Controle de contas abertas**: Trabalhadores podem pegar artigos ao longo do tempo
- **Fechamento flexível**: Fechar conta com ou sem pagamento imediato
- **Compartilhamento de contas**: Associar múltiplas pessoas a um mesmo pedido
- **Atalhos de teclado**: Navegação rápida via accesskey/p:hotkey
- **Integração total**: Utilizar entidades e services existentes (Encontro, Trabalhador, Pessoa)

---

## 2. MODELO DE DADOS (DDL PostgreSQL)

### 2.1 Estrutura das Tabelas

O modelo completo está implementado em `modelo/modelo_venda.sql` com:

#### Tabela: `venda_artigo`
- **Finalidade**: Cadastro de artigos/produtos para venda
- **Campos principais**:
  - `id` (BIGSERIAL PRIMARY KEY)
  - `codigo` VARCHAR(50) UNIQUE
  - `nome` VARCHAR(100) NOT NULL
  - `descricao` VARCHAR(500)
  - `preco_base` NUMERIC(10,2) NOT NULL
  - `ativo` BOOLEAN DEFAULT TRUE
  - `categoria` VARCHAR(50)
  - `estoque_atual` INTEGER DEFAULT 0
  - `estoque_minimo` INTEGER DEFAULT 0
  - `foto_url` VARCHAR(255)
  - `data_cadastro` TIMESTAMP

#### Tabela: `venda_pedido`
- **Finalidade**: Cabeçalho do pedido/conta
- **Campos principais**:
  - `id` (BIGSERIAL PRIMARY KEY)
  - `numero_pedido` VARCHAR(20) UNIQUE (gerado auto via trigger)
  - `encontro_id` BIGINT NOT NULL FK → encontro
  - `trabalhador_responsavel_id` BIGINT NOT NULL FK → trabalhador
  - `data_abertura` TIMESTAMP NOT NULL
  - `data_fechamento` TIMESTAMP
  - `status` VARCHAR(30) CHECK (status IN ('ABERTO', 'AGUARDO_PAGAMENTO', 'PAGO', 'CANCELADO'))
  - `valor_total` NUMERIC(10,2) DEFAULT 0 (calculado via trigger)
  - `valor_pago` NUMERIC(10,2)
  - `forma_pagamento` VARCHAR(50)
  - `fechado_por_trabalhador_id` BIGINT FK → trabalhador

#### Tabela: `venda_item_pedido`
- **Finalidade**: Itens de cada pedido
- **Campos principais**:
  - `id` (BIGSERIAL PRIMARY KEY)
  - `pedido_id` BIGINT NOT NULL FK → venda_pedido (CASCADE)
  - `artigo_id` BIGINT NOT NULL FK → venda_artigo
  - `quantidade` INTEGER CHECK (> 0)
  - `valor_unitario` NUMERIC(10,2) NOT NULL
  - `valor_total_item` NUMERIC(10,2) NOT NULL (quantidade × valor_unitario)
  - `data_inclusao` TIMESTAMP

#### Tabela: `venda_pedido_pessoa`
- **Finalidade**: Associação múltipla pessoas ↔ pedido
- **Campos principais**:
  - `id` (BIGSERIAL PRIMARY KEY)
  - `pedido_id` BIGINT NOT NULL FK → venda_pedido
  - `pessoa_id` BIGINT NOT NULL FK → pessoa
  - `tipo_associacao` VARCHAR(30) CHECK (IN ('CONSUMIDOR', 'PAGADOR', 'COMPARTILHADO'))
  - `percentual_rateio` NUMERIC(5,2) CHECK (0-100)
  - `data_associacao` TIMESTAMP
  - UNIQUE (pedido_id, pessoa_id)

### 2.2 Triggers e Funções Automáticas

#### Geração de número do pedido
```sql
CREATE OR REPLACE FUNCTION fn_gerar_numero_pedido() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.numero_pedido IS NULL THEN
        NEW.numero_pedido := CONCAT('PED', LPAD(NEXTVAL('seq_numero_pedido')::TEXT, 8, '0'));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_gerar_numero_pedido
BEFORE INSERT ON venda_pedido
FOR EACH ROW EXECUTE FUNCTION fn_gerar_numero_pedido();
```

#### Recálculo automático do valor total do pedido
Os triggers `trg_calcular_total_insert/update/delete` recalculam `venda_pedido.valor_total` automaticamente sempre que um item é inserido, atualizado ou removido.

### 2.3 Views de Consulta

- **vw_venda_pedidos_completo**: Pedidos com informações de encontro, trabalhador, totalizadores
- **vw_venda_por_artigo**: Agregação de vendas por artigo (quantidade, valores)
- **vw_contas_abertas**: Listagem de contas atualmente abertas

### 2.4 Índices para Performance
Criados índices em:
- `venda_artigo`: ativo, categoria, nome
- `venda_pedido`: encontro_id, trabalhador_id, status, datas, (encontro_id + status)
- `venda_item_pedido`: pedido_id, artigo_id
- `venda_pedido_pessoa`: pedido_id, pessoa_id

---

## 3. REGRAS DE NEGÓCIO DETALHADAS

### 3.1 Encontro Ativo Obrigatório

**REGRA CRÍTICA**: Só é permitido criar ou movimentar vendas se existir pelo menos um encontro ativo (`encontro.ativo = TRUE`).

**Implementação**:
- Método `VendaPedidoService.iniciarNovaVenda()` valida:
  ```java
  if (encontro.getAtivo() == null || !encontro.getAtivo()) {
      throw new IllegalStateException("Não há encontro ativo. Ative um encontro para iniciar vendas.");
  }
  ```
- Mensagem exibida: _"Não há encontro ativo. Ative um encontro para iniciar vendas."_

### 3.2 Estados (Status) do Pedido

Enum `StatusPedido` com 4 estados:

| Status | Descrição | Permite Alteração | Finalizado |
|--------|-----------|-------------------|------------|
| **ABERTO** | Conta em andamento, pode adicionar/remover itens | ✅ Sim | ❌ Não |
| **AGUARDO_PAGAMENTO** | Fechado para itens, mas não pago | ❌ Não | ❌ Não |
| **PAGO** | Quitado, não pode mais ser alterado | ❌ Não | ✅ Sim |
| **CANCELADO** | Cancelado, não contabilizado | ❌ Não | ✅ Sim |

**Métodos do enum**:
- `permiteAlteracao()`: retorna `true` apenas para ABERTO
- `isFinalizado()`: retorna `true` para PAGO e CANCELADO
- `contabilizarRelatorios()`: retorna `true` para PAGO e AGUARDO_PAGAMENTO

### 3.3 Conta Aberta e Registro Contínuo

**Fluxo**:
1. Trabalhador inicia uma conta ABERTA via `iniciarNovaVenda()`
2. Ao longo do encontro, pode chamar `adicionarItem()` quantas vezes precisar
3. Itens podem ser removidos com `removerItem()` enquanto status = ABERTO
4. Não há limite de tempo: conta pode ficar aberta por horas/dias

**Validação em `adicionarItem()`**:
```java
if (!pedido.getStatus().permiteAlteracao()) {
    throw new IllegalStateException("Não é possível adicionar itens...");
}
if (quantidade == null || quantidade < 1) {
    throw new IllegalArgumentException("Quantidade do item deve ser maior que zero");
}
```

### 3.4 Fechamento da Conta

**Método**: `VendaPedidoService.fecharPedido(Long pedidoId, Long fechadoPorTrabalhadorId, boolean pago, String formaPagamento)`

**Regras**:
1. Pedido deve estar com status ABERTO
2. Deve ter pelo menos 1 item: `if (pedido.getItens().isEmpty()) throw ...`
3. Sistema calcula automaticamente o `valor_total` (via trigger do banco)
4. Se `pago = true`:
   - Status → PAGO
   - `valor_pago` = `valor_total`
   - `forma_pagamento` preenchida
5. Se `pago = false`:
   - Status → AGUARDO_PAGAMENTO
   - `valor_pago` = null
6. Registra `data_fechamento` e `fechado_por_trabalhador_id`

**Exemplo de chamada**:
```java
pedidoService.fecharPedido(pedidoId, trabalhadorLogadoId, true, "PIX");
// Resultado: Status = PAGO, valor pago registrado
```

### 3.5 Marcar como Pago (Pagamento Posterior)

**Método**: `marcarComoPago(Long pedidoId, BigDecimal valorPago, String formaPagamento)`

**Regras**:
- Apenas pedidos com status AGUARDO_PAGAMENTO podem ser marcados como pagos
- Atualiza status → PAGO
- Registra valor pago e forma de pagamento
- Útil para contas que foram fechadas mas não pagas imediatamente

### 3.6 Associação de Múltiplas Pessoas/Trabalhadores

**Cenários de uso**:
1. **Conta individual**: Um trabalhador consome, uma pessoa associada (ele mesmo)
2. **Conta compartilhada**: Dois ou mais trabalhadores dividem a conta final
3. **Unir contas**: Trabalhadores A e B tinham contas separadas, ao final associam ambos à mesma conta

**Implementação**:
- Tabela `venda_pedido_pessoa` permite N associações pessoa ↔ pedido
- Tipos: CONSUMIDOR, PAGADOR, COMPARTILHADO
- Opcional: `percentual_rateio` para divisão proporcional

**Fluxo recomendado para "unir contas"**:
1. Trabalhador A tem pedido 001 aberto
2. Trabalhador B tem pedido 002 aberto
3. Opção 1 (mesclagem): 
   - Transferir itens de 002 para 001
   - Cancelar pedido 002
   - Associar Trabalhador B ao pedido 001 via `venda_pedido_pessoa`
4. Opção 2 (conta nova):
   - Criar pedido 003
   - Copiar itens de 001 e 002
   - Cancelar 001 e 002
   - Associar A e B ao pedido 003

### 3.7 Consulta e Consolidação de Consumo por Trabalhador

**Método**: `VendaPedidoService.consultarConsumoPorTrabalhador(Long trabalhadorId, Long encontroId)`

**Retorna**: Lista de todos os pedidos do trabalhador filtrados por encontro (opcional)

**Exibição na tela**:
- DataTable com colunas: Número Pedido, Data, Status, Valor Total, Pago
- Filtros por: Encontro, Status (Aberto/Pago/Aguardando)
- Totalizadores: Soma de contas pagas, soma aguardando pagamento

### 3.8 Relação com Módulo Financeiro (movimento_financeiro)

**Abordagem recomendada**: Integração no fechamento/pagamento de pedidos

**Opção 1 - Registro individual**:
- Ao marcar pedido como PAGO, criar automaticamente um `MovimentoFinanceiro` tipo ENTRADA
- Vantagens: Rastreabilidade completa, um movimento = uma venda paga
- Desvantagens: Muitos registros se houver muitas vendas pequenas

**Opção 2 - Consolidação por encontro**:
- Ao final do encontro, gerar um único `MovimentoFinanceiro` consolidado com a soma de todas as vendas pagas
- Vantagens: Menos registros, visão macro
- Desvantagens: Perde granularidade individual

**Recomendação implementada**: Opção 1 (individual), pois:
- Permite auditoria detalhada
- Facilita estornos/correções
- Pode-se consolidar via relatórios posteriormente

---

## 4. INTEGRAÇÃO COM O SISTEMA EXISTENTE

### 4.1 Obtenção do Encontro Ativo

**Service utilizado**: `EncontroService.buscarAtivos()`

```java
List<Encontro> encontrosAtivos = encontroService.buscarAtivos();
if (encontrosAtivos.isEmpty()) {
    // Bloquear vendas
} else if (encontrosAtivos.size() == 1) {
    // Selecionar automaticamente
    encontroSelecionadoId = encontrosAtivos.get(0).getId();
}
```

### 4.2 Busca de Trabalhadores e Pessoas

**Services utilizados**:
- `TrabalhadorService.buscarTodosAtivos()` → Lista para seleção de responsável
- `TrabalhadorService.buscarPorEncontro(encontroId)` → Trabalhadores de um encontro específico
- `PessoaService.buscarTodos()` → Para associar pessoas via `venda_pedido_pessoa`

**Exibição de nome completo**:
```java
// Em Trabalhador, a associação ManyToOne com Pessoa permite:
trabalhador.getPessoa().getNome()
```

###  4.3 Navegação e Pontos de Integração

**Adição ao menu principal** (`templates/menu.xhtml`):
```xml
<li>
    <div class="menu-section">
        <span class="submenu-label">
            <i class="pi pi-shopping-cart section-icon"></i>
            Vendas
        </span>
        <ul class="submenu">
            <li>
                <h:link outcome="/pages/venda/gestao-vendas.xhtml" styleClass="menu-link">
                    <i class="pi pi-shopping-bag"></i>
                    <span>Gestão de Vendas</span>
                </h:link>
            </li>
            <li>
                <h:link outcome="/pages/venda/artigos.xhtml" styleClass="menu-link">
                    <i class="pi pi-box"></i>
                    <span>Artigos</span>
                </h:link>
            </li>
            <li>
                <h:link outcome="/pages/venda/consulta-vendas.xhtml" styleClass="menu-link">
                    <i class="pi pi-chart-line"></i>
                    <span>Consultar Vendas</span>
                </h:link>
            </li>
        </ul>
    </div>
</li>
```

**Botão em Encontro Ativo**:
Na página de visualização do encontro ativo, adicionar:
```xml
<p:commandButton value="Módulo de Vendas"
                 icon="pi pi-shopping-cart"
                 outcome="/pages/venda/gestao-vendas.xhtml"
                 styleClass="action-button-primary" />
```

### 4.4 Manter Layout e Tema Visual

Todas as páginas criadas utilizam:
- Template padrão: `<ui:composition template="/templates/template.xhtml">`
- Componentes PrimeFaces: p:dataTable, p:commandButton, p:fieldset, etc.
- CSS classes: `.modern-form`, `.modern-fieldset`, `.action-button-primary`
- Mensagens: `<p:growl>` com `<p:autoUpdate />`

---

## 5. ARQUITETURA EM CAMADAS (Java)

### 5.1 Estrutura de Pacotes

```
br.com.segueme
├── entity/
│   ├── VendaArtigo.java
│   ├── VendaPedido.java
│   ├── VendaItemPedido.java
│   ├── VendaPedidoPessoa.java
│   └── enums/
│       ├── StatusPedido.java
│       └── TipoAssociacaoPedido.java
├── repository/
│   ├── VendaArtigoRepository.java (interface)
│   ├── VendaPedidoRepository.java (interface)
│   └── impl/
│       ├── VendaArtigoRepositoryImpl.java (@Stateless)
│       └── VendaPedidoRepositoryImpl.java (@Stateless)
├── service/
│   ├── VendaArtigoService.java (@ApplicationScoped)
│   └── VendaPedidoService.java (@ApplicationScoped)
└── controller/
    ├── VendaArtigoController.java (@Named @ViewScoped)
    └── VendaPedidoController.java (@Named @ViewScoped)
```

### 5.2 Entidades JPA

**VendaArtigo**:
```java
@Entity
@Table(name = "venda_artigo", schema = "public")
public class VendaArtigo implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Size(max = 50)
    @Column(name = "codigo", unique = true)
    private String codigo;
    
    @NotBlank @Size(min = 3, max = 100)
    @Column(name = "nome", nullable = false)
    private String nome;
    
    @NotNull @DecimalMin("0.0")
    @Column(name = "preco_base", precision = 10, scale = 2)
    private BigDecimal precoBase;
    
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;
    
    // + categoria, estoque, observacoes, datas, etc.
    // Métodos: isDisponivel(), isEstoqueBaixo(), debitarEstoque(), creditarEstoque()
}
```

**VendaPedido**:
```java
@Entity
@Table(name = "venda_pedido", schema = "public")
public class VendaPedido implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_pedido", unique = true)
    private String numeroPedido; // Gerado por trigger
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id")
    private Encontro encontro;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabalhador_responsavel_id")
    private Trabalhador trabalhadorResponsavel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusPedido status = StatusPedido.ABERTO;
    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VendaItemPedido> itens = new HashSet<>();
    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VendaPedidoPessoa> pessoasAssociadas = new HashSet<>();
    
    // Métodos: adicionarItem(), removerItem(), fechar(), marcarComoPago(), cancelar(), getQuantidadeTotalItens(), getSaldoPendente()
}
```

### 5.3 Repositories

**Interface VendaArtigoRepository**:
```java
public interface VendaArtigoRepository {
    VendaArtigo save(VendaArtigo artigo);
    VendaArtigo update(VendaArtigo artigo);
    Optional<VendaArtigo> findById(Long id);
    Optional<VendaArtigo> findByCodigo(String codigo);
    List<VendaArtigo> findAll();
    List<VendaArtigo> findAtivos();
    List<VendaArtigo> findByCategoria(String categoria);
    List<VendaArtigo> findComEstoqueBaixo();
    List<VendaArtigo> findByNome(String nome);
    boolean delete(Long id);
    List<String> findAllCategorias();
}
```

**Implementação VendaArtigoRepositoryImpl**:
```java
@Stateless
public class VendaArtigoRepositoryImpl implements VendaArtigoRepository {
    @PersistenceContext(unitName = "seguemePU")
    private EntityManager entityManager;
    
    @Override
    public VendaArtigo save(VendaArtigo artigo) {
        entityManager.persist(artigo);
        return artigo;
    }
    
    @Override
    public List<VendaArtigo> findAtivos() {
        return entityManager.createQuery(
            "SELECT a FROM VendaArtigo a WHERE a.ativo = TRUE ORDER BY a.nome",
            VendaArtigo.class).getResultList();
    }
    // ... demais métodos
}
```

### 5.4 Services

**VendaArtigoService**:
- Validações: nome obrigatório, preço >= 0, código único
- Métodos: salvar(), atualizar(), buscarPorId(), buscarAtivos(), buscarPorCategoria(), etc.
- Lógica: ativar(), inativar(), ajustarEstoque()

**VendaPedidoService** (o mais importante):
- **Regras de negócio concentradas aqui**
- Métodos principais:
  - `iniciarNovaVenda(encontroId, trabalhadorId)` → valida encontro ativo
  - `adicionarItem(pedidoId, artigoId, quantidade)` → valida status ABERTO, quantidade > 0
  - `removerItem(pedidoId, itemId)`
  - `fecharPedido(pedidoId, fechadoPorId, pago, formaPagamento)` → valida mínimo 1 item
  - `marcarComoPago(pedidoId, valorPago, formaPagamento)` → valida status AGUARDO_PAGAMENTO
  - `cancelarPedido(pedidoId)`, `reabrirPedido(pedidoId)`
  - Consultas: `buscarPorEncontro()`, `buscarAbertosPorEncontro()`, `consultarConsumoPorTrabalhador()`

### 5.5 Controllers (Managed Beans)

**VendaArtigoController**:
```java
@Named
@ViewScoped
public class VendaArtigoController implements Serializable {
    @Inject private VendaArtigoService artigoService;
    
    private List<VendaArtigo> artigos;
    private VendaArtigo artigo;
    private VendaArtigo artigoSelecionado;
    
    @PostConstruct
    public void init() { carregarArtigos(); limpar(); }
    
    public String salvar() { /* validações + artigoService.salvar() */ }
    public void excluir() { /* artigoService.remover() */ }
    public void ativar(VendaArtigo artigo) { /* artigoService.ativar() */ }
    // ...
}
```

**VendaPedidoController** (main controller):
```java
@Named
@ViewScoped
public class VendaPedidoController implements Serializable {
    @Inject private VendaPedidoService pedidoService;
    @Inject private EncontroService encontroService;
    @Inject private TrabalhadorService trabalhadorService;
    @Inject private VendaArtigoService artigoService;
    
    private VendaPedido pedidoAtual;
    private Long encontroSelecionadoId;
    private Long trabalhadorSelecionadoId;
    private Long artigoSelecionadoId;
    private Integer quantidadeItem;
    
    @PostConstruct
    public void init() { verificarEncontroAtivo(); }
    
    // Métodos principais (chamados pelos botões com atalhos):
    public void iniciarNovaVenda() { /* pedidoService.iniciarNovaVenda() */ }
    public void adicionarItem() { /* pedidoService.adicionarItem() */ }
    public void fecharPedido() { /* pedidoService.fecharPedido() */ }
    public void marcarComoPago() { /* pedidoService.marcarComoPago() */ }
}
```

---

## 6. PÁGINAS JSF/PRIMEFACES

Continua no próximo arquivo...
