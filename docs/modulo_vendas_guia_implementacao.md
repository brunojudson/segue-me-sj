# MÓDULO DE VENDAS - Guia de Implementação e Deploy
## Checklist Completo para Finalização da Implementação

---

## 1. CHECKLIST DE IMPLEMENTAÇÃO

### 1.1 Banco de Dados ✅
- [x] Criar script DDL completo (`modelo/modelo_venda.sql`)
- [x] Criar tabelas: venda_artigo, venda_pedido, venda_item_pedido, venda_pedido_pessoa
- [x] Criar sequences: seq_numero_pedido
- [x] Criar triggers: fn_gerar_numero_pedido(), fn_calcular_valor_total_pedido()
- [x] Criar views: vw_venda_pedidos_completo, vw_venda_por_artigo, vw_contas_abertas
- [x] Criar índices de performance
- [ ] **EXECUTAR O SCRIPT NO BANCO DE DADOS**

**Comando para executar**:
```bash
psql -U seu_usuario -d segueme_db -f modelo/modelo_venda.sql
```

### 1.2 Camada de Entidade (Entity) ✅
- [x] Criar enum `StatusPedido.java`
- [x] Criar enum `TipoAssociacaoPedido.java`
- [x] Criar entidade `VendaArtigo.java`
- [x] Criar entidade `VendaPedido.java`
- [x] Criar entidade `VendaItemPedido.java`
- [x] Criar entidade `VendaPedidoPessoa.java`
- [ ] Verificar compilação sem erros
- [ ] Testar mapeamento JPA (opcional: criar test unitário)

### 1.3 Camada de Repositório (Repository) ✅
- [x] Criar interface `VendaArtigoRepository.java`
- [x] Criar implementação `VendaArtigoRepositoryImpl.java`
- [x] Criar interface `VendaPedidoRepository.java`
- [x] Criar implementação `VendaPedidoRepositoryImpl.java`
- [ ] Verificar injeção de `EntityManager`
- [ ] Testar queries JPQL (opcional)

### 1.4 Camada de Serviço (Service) ✅
- [x] Criar `VendaArtigoService.java`
- [x] Criar `VendaPedidoService.java`
- [x] Implementar validação de encontro ativo
- [x] Implementar regras de negócio
- [ ] Verificar injeção de dependências (@Inject)
- [ ] Testar lógica de negócio (opcional)

### 1.5 Camada de Controle (Controller) ✅
- [x] Criar `VendaArtigoController.java`
- [x] Criar `VendaPedidoController.java`
- [x] Implementar métodos de ação
- [x] Configurar FacesMessage
- [ ] Verificar escopo @ViewScoped
- [ ] Testar navegação

### 1.6 Camada de Visualização (View - XHTML) ⏳
- [ ] Criar diretório `pages/venda/`
- [ ] Criar `pages/venda/gestao-vendas.xhtml`
- [ ] Criar `pages/venda/artigos.xhtml`
- [ ] Criar `pages/venda/consulta-vendas.xhtml`
- [ ] Testar renderização no navegador
- [ ] Verificar atalhos de teclado (accesskey)
- [ ] Testar responsividade mobile

### 1.7 Integração com Menu ⏳
- [ ] Editar `templates/menu.xhtml`
- [ ] Adicionar seção "Vendas"
- [ ] Adicionar links: Gestão de Vendas, Artigos, Consultar Vendas
- [ ] Testar navegação entre páginas

### 1.8 Testes Funcionais ⏳
- [ ] Testar criação de artigo
- [ ] Testar iniciar nova venda (com encontro ativo)
- [ ] Testar adicionar itens a pedido
- [ ] Testar remover item de pedido
- [ ] Testar fechar pedido sem pagar (status → AGUARDO_PAGAMENTO)
- [ ] Testar fechar pedido e pagar (status → PAGO)
- [ ] Testar marcar como pago posteriormente
- [ ] Testar cancelar pedido
- [ ] Testar filtros de consulta
- [ ] Testar exportação para Excel
- [ ] Testar validação de encontro ativo (mensagem: "Não há encontro ativo...")

---

## 2. INSTRUÇÕES DE DEPLOY

### 2.1 Pré-requisitos
- PostgreSQL 10+ instalado e rodando
- WildFly/JBoss Application Server configurado
- DataSource JNDI configurado: `java:jboss/datasources/seguemePU`
- Java EE 8+ (ou Jakarta EE)

### 2.2 Passo a Passo

#### Passo 1: Deploy do Banco de Dados
```bash
# Conectar ao banco
psql -U postgres -d segueme_db

# Executar o script DDL
\i modelo/modelo_venda.sql

# Verificar tabelas criadas
\dt venda_*

# Verificar views
\dv vw_*

# Verificar triggers
SELECT tgname FROM pg_trigger WHERE tgname LIKE '%venda%';
```

#### Passo 2: Compilar o Projeto
```bash
# Na raiz do projeto
mvn clean install

# Ou se estiver usando Eclipse/IntelliJ:
# Right-click no projeto → Run As → Maven build → Goals: clean install
```

#### Passo 3: Deploy no WildFly
```bash
# Copiar WAR para o diretório de deploy do WildFly
cp target/segue-me-sj.war /caminho/para/wildfly/standalone/deployments/

# Ou usar CLI do WildFly
/opt/wildfly/bin/jboss-cli.sh --connect
deploy target/segue-me-sj.war --force
```

#### Passo 4: Verificar Deploy
- Acessar: `http://localhost:8080/segue-me-sj`
- Fazer login com usuário administrador
- Navegar até menu "Vendas" → Gestão de Vendas
- Verificar se aparecem os componentes de seleção de encontro

#### Passo 5: Popular Dados de Teste (Opcional)
```sql
-- Inserir artigos de teste
INSERT INTO venda_artigo (codigo, nome, descricao, preco_base, categoria, estoque_atual, estoque_minimo, ativo, data_cadastro)
VALUES 
    ('CAM001', 'Camiseta Básica', 'Camiseta 100% algodão', 35.00, 'Camisetas', 50, 10, TRUE, NOW()),
    ('CAM002', 'Camiseta Estampada', 'Camiseta com estampa do encontro', 45.00, 'Camisetas', 30, 5, TRUE, NOW()),
    ('BON001', 'Boné', 'Boné ajustável', 25.00, 'Acessórios', 20, 5, TRUE, NOW()),
    ('CAN001', 'Caneca', 'Caneca personalizada', 18.00, 'Utensílios', 40, 10, TRUE, NOW()),
    ('PUL001', 'Pulseira', 'Pulseira de silicone', 8.00, 'Acessórios', 100, 20, TRUE, NOW());

-- Verificar inserção
SELECT * FROM venda_artigo;
```

---

## 3. CONFIGURAÇÃO DE ENCONTRO ATIVO

**IMPORTANTE**: Para usar o módulo de vendas, é necessário ter um encontro ativo.

### 3.1 Ativar Encontro Manualmente no Banco
```sql
-- Verificar encontros existentes
SELECT id, nome, ativo FROM encontro;

-- Desativar todos primeiro (apenas 1 pode estar ativo por vez)
UPDATE encontro SET ativo = FALSE;

-- Ativar o encontro desejado (substitua 123 pelo ID real)
UPDATE encontro SET ativo = TRUE WHERE id = 123;

-- Verificar
SELECT id, nome, ativo FROM encontro WHERE ativo = TRUE;
```

### 3.2 Ativar via Interface do Sistema
1. Navegar até "Encontros" → "Gerenciar Encontros"
2. Localizar o encontro desejado na lista
3. Clicar no botão "Ativar" ou editar e marcar checkbox "Ativo"
4. Salvar

**Após ativação**: A página de Gestão de Vendas deve detectar automaticamente o encontro ativo e selecioná-lo.

---

## 4. TESTES DE VALIDAÇÃO OBRIGATÓRIOS

### 4.1 Teste: Encontro Ativo Obrigatório

**Cenário**: Tentar iniciar venda sem encontro ativo

**Passos**:
1. Desativar todos os encontros no banco
2. Acessar "Vendas" → "Gestão de Vendas"
3. Tentar selecionar trabalhador e clicar "Nova Venda (Alt+N)"

**Resultado Esperado**:
- Mensagem de erro: ⚠️ _"Não há encontro ativo. Ative um encontro para iniciar vendas."_
- Pedido NÃO é criado

### 4.2 Teste: Fluxo Completo de Venda

**Cenário**: Venda completa com pagamento

**Passos**:
1. Ativar um encontro
2. Acessar "Vendas" → "Gestão de Vendas"
3. Selecionar trabalhador → Clicar "Nova Venda (Alt+N)"
4. Pedido criado com status ABERTO
5. Adicionar artigo (ex: Camiseta) → Quantidade 2 → Clicar "Adicionar Item (Alt+I)"
6. Adicionar outro artigo (ex: Boné) → Quantidade 1 → Clicar "Adicionar Item (Alt+I)"
7. Verificar tabela de itens e valor total calculado
8. Clicar "Fechar Pedido (Alt+F)"
9. Marcar checkbox "Pagar Agora"
10. Selecionar forma de pagamento: PIX
11. Clicar "Confirmar"

**Resultado Esperado**:
- Pedido muda status para PAGO
- Valor pago = Valor total
- Pedido sai da lista "Contas Abertas"
- Mensagem de sucesso: ✅ _"Pedido fechado e marcado como pago com sucesso"_

### 4.3 Teste: Fechar Sem Pagar

**Cenário**: Fechar conta para pagamento posterior

**Passos**:
1. Criar nova venda e adicionar itens
2. Clicar "Fechar Pedido (Alt+F)"
3. **NÃO marcar** "Pagar Agora"
4. Clicar "Confirmar"

**Resultado Esperado**:
- Pedido muda status para AGUARDO_PAGAMENTO
- Pedido sai da lista "Contas Abertas"
- Pedido aparece na lista "Pedidos Aguardando Pagamento"
- Mensagem: ✅ _"Pedido fechado com sucesso. Pagamento pendente."_

### 4.4 Teste: Marcar Como Pago Posteriormente

**Cenário**: Receber pagamento de conta fechada

**Passos**:
1. Localizar pedido na lista "Pedidos Aguardando Pagamento"
2. Clicar "Marcar como Pago (Alt+P)"
3. Selecionar forma de pagamento: DINHEIRO
4. Valor pago: (já preenchido com valor total, pode editar)
5. Clicar "Confirmar Pagamento"

**Resultado Esperado**:
- Pedido muda status para PAGO
- Pedido sai da lista "Aguardando Pagamento"
- Mensagem: ✅ _"Pagamento registrado com sucesso"_

### 4.5 Teste: Consulta e Filtros

**Cenário**: Buscar vendas por período e status

**Passos**:
1. Acessar "Vendas" → "Consultar Vendas"
2. Filtrar por: Encontro = XXII Encontro, Status = PAGO
3. Data Início = 01/01/2024, Data Fim = 31/12/2024
4. Clicar "Consultar (Alt+C)"

**Resultado Esperado**:
- Tabela mostra apenas pedidos pagos do encontro selecionado
- Totalizadores exibem: Total de Pedidos, Total Pago, Total Geral
- Clicar "Exportar Excel (Alt+E)" → arquivo .xlsx é baixado

### 4.6 Teste: Atalhos de Teclado

**Cenário**: Navegação sem mouse

**Passos**:
1. Na página Gestão de Vendas, pressionar **Alt+N**
2. Verificar se focus vai para o botão "Nova Venda"
3. Após adicionar itens, pressionar **Alt+F**
4. Verificar se dialog de fechar pedido abre
5. Na lista de aguardando pagamento, pressionar **Alt+P** no primeiro item
6. Verificar se dialog de marcar como pago abre

**Resultado Esperado**:
- Todos os atalhos funcionam corretamente
- UX fluida para operadores experientes

---

## 5. SOLUÇÃO DE PROBLEMAS COMUNS

### 5.1 Erro: "Não é possível adicionar itens a este pedido (status não permite)"

**Causa**: Tentou adicionar item a pedido com status diferente de ABERTO

**Solução**:
- Apenas pedidos ABERTOS permitem adicionar/remover itens
- Se pedido está FECHADO/PAGO, criar nova venda
- Se precisa corrigir, reabrir o pedido (implementar método `reabrirPedido()`)

### 5.2 Erro: "Valor total do pedido está zerado"

**Causa**: Triggers do banco não foram criados ou não estão funcionando

**Solução**:
```sql
-- Verificar se trigger existe
SELECT tgname FROM pg_trigger WHERE tgrelid = 'venda_item_pedido'::regclass;

-- Recriar triggers
\i modelo/modelo_venda.sql

-- Recalcular manualmente (caso necessário)
UPDATE venda_pedido SET valor_total = (
    SELECT COALESCE(SUM(valor_total_item), 0)
    FROM venda_item_pedido
    WHERE pedido_id = venda_pedido.id
);
```

### 5.3 Erro: "LazyInitializationException" ao exibir encontro/trabalhador

**Causa**: Entidades relacionadas não foram carregadas com JOIN FETCH

**Solução**: Verificar métodos do repository usam:
```java
@Query("SELECT p FROM VendaPedido p LEFT JOIN FETCH p.encontro LEFT JOIN FETCH p.trabalhadorResponsavel WHERE p.id = :id")
```

### 5.4 Erro: "Could not find bean 'vendaPedidoController'"

**Causa**: Controller não foi reconhecido pelo CDI

**Solução**:
- Verificar `@Named` e `@ViewScoped` na classe
- Verificar `beans.xml` existe em `WEB-INF/`
- Limpar e reconstruir o projeto: `mvn clean install`
- Reiniciar o servidor

### 5.5 Mensagens não aparecem na tela

**Causa**: `<p:growl>` não está configurado corretamente

**Solução**:
```xml
<p:growl id="growl" showDetail="true" sticky="false" life="5000">
    <p:autoUpdate />
</p:growl>
```

E nos botões:
```xml
<p:commandButton ... update="@form" />
<!-- Ou especificamente: update="growl formContent" -->
```

---

## 6. MELHORIAS FUTURAS (Roadmap)

### 6.1 Curto Prazo (v1.1)
- [ ] Implementar associação de múltiplas pessoas via UI (compartilhar conta)
- [ ] Implementar "unir contas" (mesclar dois pedidos)
- [ ] Relatório de vendas por artigo (gráfico)
- [ ] Relatório de vendas por categoria
- [ ] Impressão de comprovante de venda
- [ ] QR Code para pagamento PIX

### 6.2 Médio Prazo (v2.0)
- [ ] Integração com módulo financeiro (MovimentoFinanceiro automático)
- [ ] Dashboard de vendas em tempo real
- [ ] Controle de estoque com alertas automáticos
- [ ] Histórico de preços por artigo
- [ ] Descontos e cupons promocionais
- [ ] Vendas parceladas

### 6.3 Longo Prazo (v3.0)
- [ ] App mobile para registro de vendas
- [ ] Leitor de código de barras
- [ ] Integração com gateway de pagamento
- [ ] Sistema de comissões para trabalhadores
- [ ] Analytics avançado com ML para previsão de estoque

---

## 7. DOCUMENTAÇÃO TÉCNICA ADICIONAL

### 7.1 Diagrama Entidade-Relacionamento (ER)

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   ENCONTRO      │       │  TRABALHADOR    │       │    PESSOA       │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │◄──┐   │ id (PK)         │◄──┐   │ id (PK)         │
│ nome            │   │   │ pessoa_id (FK) ─┼───┼──►│ nome            │
│ ativo           │   │   └─────────────────┘   │   │ cpf             │
└─────────────────┘   │                         │   └─────────────────┘
                      │                         │            ▲
                      │                         │            │
                      │                         │            │
┌─────────────────────┴──┐                      │            │
│   VENDA_PEDIDO         │                      │            │
├────────────────────────┤                      │            │
│ id (PK)                │                      │            │
│ numero_pedido (UK)     │                      │            │
│ encontro_id (FK) ──────┘                      │            │
│ trabalhador_responsavel_id (FK) ──────────────┘            │
│ fechado_por_trabalhador_id (FK)                            │
│ data_abertura          │                                   │
│ data_fechamento        │                                   │
│ status (ENUM)          │                                   │
│ valor_total (CALC)     │                                   │
│ valor_pago             │                                   │
│ forma_pagamento        │                                   │
└────────────┬───────────┘                                   │
             │                                               │
             │                                               │
    ┌────────┴──────────────────┐            ┌──────────────┴─────────┐
    │                           │            │                        │
┌───▼───────────────┐   ┌───────▼────────┐  │  ┌────────────────────┐│
│ VENDA_ITEM_PEDIDO │   │ VENDA_PEDIDO_  │  │  │   VENDA_ARTIGO     ││
├───────────────────┤   │    PESSOA      │  │  ├────────────────────┤│
│ id (PK)           │   ├────────────────┤  │  │ id (PK)            ││
│ pedido_id (FK)    │   │ id (PK)        │  │  │ codigo (UK)        ││
│ artigo_id (FK) ───┼───┼─────────────── │  └──┤ nome               ││
│ quantidade        │   │ pedido_id (FK) │     │ descricao          ││
│ valor_unitario    │   │ pessoa_id (FK) ├─────►│ preco_base         ││
│ valor_total_item  │   │ tipo_associacao│     │ categoria          ││
│ data_inclusao     │   │ percentual_rat.│     │ estoque_atual      ││
└───────────────────┘   └────────────────┘     │ estoque_minimo     ││
                                                │ ativo              ││
                                                └────────────────────┘│
                                                                      │
                                                                      │
```

### 7.2 Fluxo de Estados (Status do Pedido)

```
     ┌──────────┐
     │  ABERTO  │ ◄─── Pode adicionar/remover itens
     └────┬─────┘
          │
          │ fecharPedido(pago=false)
          ▼
  ┌───────────────────┐
  │ AGUARDO_PAGAMENTO │
  └────┬──────────────┘
       │
       │ marcarComoPago()
       ▼
   ┌──────┐
   │ PAGO │ ◄─── Estado final (contabilizado)
   └──────┘

   Qualquer estado ──cancelarPedido()──► CANCELADO (não contabilizado)
```

### 7.3 Sequência de Operações - Fluxo Completo

```
USUÁRIO                 CONTROLLER               SERVICE              REPOSITORY         DATABASE
   │                        │                       │                      │                 │
   │  Nova Venda (Alt+N)    │                       │                      │                 │
   ├───────────────────────►│                       │                      │                 │
   │                        │ iniciarNovaVenda()    │                      │                 │
   │                        ├──────────────────────►│                      │                 │
   │                        │                       │ validar encontro.ativo               │
   │                        │                       │ buscarEncontro()     │                 │
   │                        │                       ├─────────────────────►│ SELECT encontro │
   │                        │                       │                      ├────────────────►│
   │                        │                       │◄─────────────────────┤◄────────────────┤
   │                        │                       │ criar VendaPedido    │                 │
   │                        │                       │ save()               │                 │
   │                        │                       ├─────────────────────►│ INSERT pedido   │
   │                        │                       │                      ├────────────────►│
   │                        │                       │                      │ TRIGGER: gerar  │
   │                        │                       │                      │    numero_pedido│
   │                        │◄──────────────────────┤◄─────────────────────┤◄────────────────┤
   │◄───────────────────────┤ FacesMessage          │                      │                 │
   │ ✅ Venda iniciada      │                       │                      │                 │
   │                        │                       │                      │                 │
   │  Adicionar Item (Alt+I)│                       │                      │                 │
   ├───────────────────────►│ adicionarItem()       │                      │                 │
   │                        ├──────────────────────►│                      │                 │
   │                        │                       │ validar status ABERTO                 │
   │                        │                       │ validar quantidade>0                  │
   │                        │                       │ criar VendaItemPedido                 │
   │                        │                       │ save()               │                 │
   │                        │                       ├─────────────────────►│ INSERT item     │
   │                        │                       │                      ├────────────────►│
   │                        │                       │                      │ TRIGGER: recalc │
   │                        │                       │                      │    valor_total  │
   │                        │◄──────────────────────┤◄─────────────────────┤◄────────────────┤
   │◄───────────────────────┤                       │                      │                 │
   │ ✅ Item adicionado     │                       │                      │                 │
   │                        │                       │                      │                 │
   │  Fechar Pedido (Alt+F) │                       │                      │                 │
   ├───────────────────────►│ fecharPedido()        │                      │                 │
   │                        ├──────────────────────►│                      │                 │
   │                        │                       │ validar itens.isEmpty()               │
   │                        │                       │ pedido.fechar()      │                 │
   │                        │                       │ update()             │                 │
   │                        │                       ├─────────────────────►│ UPDATE pedido   │
   │                        │                       │                      │ SET status=PAGO │
   │                        │                       │                      │ data_fechamento │
   │                        │◄──────────────────────┤◄─────────────────────┤◄────────────────┤
   │◄───────────────────────┤                       │                      │                 │
   │ ✅ Pedido fechado e pago                       │                      │                 │
```

---

## 8. CONTATOS E SUPORTE

**Desenvolvedor**: [Seu Nome]  
**E-mail**: [seu.email@dominio.com]  
**Repositório**: [Link do Git, se aplicável]  
**Versão do Módulo**: 1.0.0  
**Data de Criação**: [Data Atual]

---

**FIM DO GUIA DE IMPLEMENTAÇÃO E DEPLOY**
