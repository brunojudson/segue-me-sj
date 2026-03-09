# Módulo de Vendas - Sistema Segue-me-sj

## 📋 Visão Geral

Módulo completo para gerenciamento de vendas de artigos durante Encontros de Jovens, totalmente integrado ao sistema Segue-me-sj.

### Funcionalidades Principais

✅ **Gestão de Artigos**: Cadastro, categorização, controle de estoque  
✅ **Vendas por Encontro**: Vinculação obrigatória a encontros ativos  
✅ **Contas Abertas**: Trabalhadores podem acumular itens ao longo do tempo  
✅ **Fechamento Flexível**: Pagar na hora ou deixar para depois  
✅ **Compartilhamento**: Associar múltiplas pessoas a um pedido  
✅ **Consultas e Relatórios**: Filtros avançados, exportação Excel  
✅ **Atalhos de Teclado**: Navegação rápida via Alt+N, Alt+I, Alt+F, Alt+P

---

## 🚀 Quick Start

### 1. Deploy do Banco de Dados

```bash
psql -U postgres -d segueme_db -f modelo/modelo_venda.sql
```

### 2. Compilar e Deploy

```bash
mvn clean install
cp target/segue-me-sj.war /caminho/wildfly/standalone/deployments/
```

### 3. Ativar um Encontro

```sql
UPDATE encontro SET ativo = TRUE WHERE id = [ID_DO_ENCONTRO];
```

### 4. Popular Artigos de Teste (Opcional)

```sql
INSERT INTO venda_artigo (codigo, nome, preco_base, categoria, estoque_atual, ativo, data_cadastro)
VALUES 
    ('CAM001', 'Camiseta', 35.00, 'Camisetas', 50, TRUE, NOW()),
    ('BON001', 'Boné', 25.00, 'Acessórios', 20, TRUE, NOW());
```

### 5. Acessar o Sistema

1. Navegar para: `http://localhost:8080/segue-me-sj`
2. Login → Menu "Vendas" → "Gestão de Vendas"
3. Criar nova venda (Alt+N) → Adicionar itens (Alt+I) → Fechar (Alt+F)

---

## 📚 Documentação

### Arquivos de Documentação Criados

| Documento | Descrição |
|-----------|-----------|
| **[modulo_vendas_especificacao.md](modulo_vendas_especificacao.md)** | Especificação técnica completa: modelo de dados, regras de negócio, arquitetura em camadas |
| **[modulo_vendas_paginas_xhtml.md](modulo_vendas_paginas_xhtml.md)** | Exemplos completos de páginas JSF/PrimeFaces com código-fonte XHTML e CSS |
| **[modulo_vendas_guia_implementacao.md](modulo_vendas_guia_implementacao.md)** | Checklist de implementação, deploy, testes, troubleshooting, diagramas ER |

### Código-Fonte Implementado

#### 📂 Banco de Dados
- `modelo/modelo_venda.sql` - DDL completo com tabelas, triggers, views, índices

#### 📂 Entidades (`src/main/java/br/com/segueme/entity/`)
- `VendaArtigo.java` - Artigo/produto para venda
- `VendaPedido.java` - Cabeçalho do pedido
- `VendaItemPedido.java` - Itens do pedido
- `VendaPedidoPessoa.java` - Associação pessoa ↔ pedido
- `enums/StatusPedido.java` - ABERTO, AGUARDO_PAGAMENTO, PAGO, CANCELADO
- `enums/TipoAssociacaoPedido.java` - CONSUMIDOR, PAGADOR, COMPARTILHADO

#### 📂 Repositories (`src/main/java/br/com/segueme/repository/`)
- `VendaArtigoRepository.java` + `impl/VendaArtigoRepositoryImpl.java`
- `VendaPedidoRepository.java` + `impl/VendaPedidoRepositoryImpl.java`

#### 📂 Services (`src/main/java/br/com/segueme/service/`)
- `VendaArtigoService.java` - Lógica de negócio de artigos
- `VendaPedidoService.java` - **Regra crítica: validação de encontro ativo**

#### 📂 Controllers (`src/main/java/br/com/segueme/controller/`)
- `VendaArtigoController.java` - Managed bean para artigos
- `VendaPedidoController.java` - Managed bean para vendas

#### 📂 Views (a criar: `src/main/webapp/pages/venda/`)
- `gestao-vendas.xhtml` - Página principal de vendas (com atalhos Alt+N, Alt+I, Alt+F, Alt+P)
- `artigos.xhtml` - Gerenciamento de artigos
- `consulta-vendas.xhtml` - Relatórios e consultas

---

## 🔑 Regras de Negócio Principais

### 1. Encontro Ativo Obrigatório
⚠️ **Vendas só podem ser iniciadas se houver um encontro ativo** (`encontro.ativo = TRUE`)

**Mensagem ao usuário**:  
_"Não há encontro ativo. Ative um encontro para iniciar vendas."_

### 2. Estados do Pedido

```
ABERTO → permite adicionar/remover itens
   ↓
AGUARDO_PAGAMENTO → fechado, aguardando pagamento
   ↓
PAGO → quitado, finalizado

Qualquer estado → CANCELADO (não contabilizado)
```

### 3. Cálculo Automático
- `numero_pedido`: gerado automaticamente (PED00000001)
- `valor_total`: recalculado via trigger ao adicionar/remover itens
- `valor_total_item`: quantidade × valor_unitario

### 4. Conta Aberta
- Trabalhador pode pegar itens ao longo de horas/dias
- Status ABERTO permite adições ilimitadas
- Só pode fechar se tiver pelo menos 1 item

---

## ⌨️ Atalhos de Teclado

| Atalho | Ação | Página |
|--------|------|--------|
| **Alt+N** | Nova Venda / Novo Artigo | Gestão de Vendas / Artigos |
| **Alt+I** | Adicionar Item | Gestão de Vendas |
| **Alt+F** | Fechar Pedido / Filtrar | Gestão de Vendas / Artigos |
| **Alt+P** | Marcar como Pago | Gestão de Vendas |
| **Alt+S** | Salvar | Dialogs de edição |
| **Alt+C** | Consultar | Consulta de Vendas |
| **Alt+E** | Exportar Excel | Consulta de Vendas |
| **Alt+R** | Atualizar Lista | Artigos |

---

## 🧪 Testes Essenciais

### Teste 1: Validação de Encontro Ativo

```
1. Desativar todos os encontros
2. Tentar criar nova venda
3. Verificar mensagem de erro: "Não há encontro ativo..."
```

### Teste 2: Fluxo Completo

```
1. Ativar encontro
2. Nova Venda (Alt+N)
3. Adicionar Item (Alt+I) - Camiseta x 2
4. Adicionar Item (Alt+I) - Boné x 1
5. Fechar Pedido (Alt+F)
6. Marcar "Pagar Agora" → Forma: PIX → Confirmar
7. Verificar status PAGO
```

### Teste 3: Pagamento Posterior

```
1. Criar venda e adicionar itens
2. Fechar sem marcar "Pagar Agora"
3. Verificar status AGUARDO_PAGAMENTO
4. Na lista "Aguardando Pagamento", clicar "Marcar como Pago (Alt+P)"
5. Confirmar → verificar status PAGO
```

---

## 🛠️ Estrutura de Arquivos

```
segue-me-sj/
├── docs/
│   ├── modulo_vendas_especificacao.md       # Especificação técnica
│   ├── modulo_vendas_paginas_xhtml.md       # Código XHTML das páginas
│   ├── modulo_vendas_guia_implementacao.md  # Guia de deploy e testes
│   └── modulo_vendas_README.md              # Este arquivo
├── modelo/
│   └── modelo_venda.sql                     # DDL do banco de dados
├── src/main/java/br/com/segueme/
│   ├── entity/
│   │   ├── VendaArtigo.java
│   │   ├── VendaPedido.java
│   │   ├── VendaItemPedido.java
│   │   ├── VendaPedidoPessoa.java
│   │   └── enums/
│   │       ├── StatusPedido.java
│   │       └── TipoAssociacaoPedido.java
│   ├── repository/
│   │   ├── VendaArtigoRepository.java
│   │   ├── VendaPedidoRepository.java
│   │   └── impl/
│   │       ├── VendaArtigoRepositoryImpl.java
│   │       └── VendaPedidoRepositoryImpl.java
│   ├── service/
│   │   ├── VendaArtigoService.java
│   │   └── VendaPedidoService.java
│   └── controller/
│       ├── VendaArtigoController.java
│       └── VendaPedidoController.java
└── src/main/webapp/pages/venda/             # A CRIAR
    ├── gestao-vendas.xhtml
    ├── artigos.xhtml
    └── consulta-vendas.xhtml
```

---

## 📊 Modelo de Dados Resumido

### Tabelas Principais

**venda_artigo**: Produtos/artigos para venda  
**venda_pedido**: Cabeçalho do pedido (encontro, trabalhador, status)  
**venda_item_pedido**: Itens de cada pedido (artigo, quantidade, valores)  
**venda_pedido_pessoa**: Associação múltipla pessoa ↔ pedido (contas compartilhadas)

### Relacionamentos

```
Encontro 1 ──── N Pedido
Trabalhador 1 ──── N Pedido (responsável + fechado_por)
Pedido 1 ──── N ItemPedido
Artigo 1 ──── N ItemPedido
Pedido N ──── N Pessoa (via venda_pedido_pessoa)
```

---

## 🔧 Tecnologias Utilizadas

- **Backend**: Java EE 8 / Jakarta EE
- **Web Framework**: JSF (JavaServer Faces)
- **UI Components**: PrimeFaces 12+
- **ORM**: JPA (Hibernate)
- **CDI**: Weld (Context and Dependency Injection)
- **EJB**: @Stateless repositories
- **Database**: PostgreSQL 10+
- **Application Server**: WildFly 26+ / JBoss EAP 7+
- **Build**: Maven 3+

---

## 📈 Roadmap de Melhorias

### Versão 1.1 (Próxima)
- [ ] UI para associar múltiplas pessoas (compartilhar conta)
- [ ] Função "unir contas" de diferentes trabalhadores
- [ ] Relatório gráfico de vendas por artigo
- [ ] Impressão de comprovante

### Versão 2.0 (Futuro)
- [ ] Integração automática com módulo financeiro
- [ ] Dashboard de vendas em tempo real
- [ ] Controle de estoque com alertas
- [ ] Descontos e promoções

### Versão 3.0 (Longo prazo)
- [ ] App mobile para vendas
- [ ] Leitor de código de barras
- [ ] Pagamento integrado (PIX, cartão)
- [ ] Analytics com ML

---

## ❓ Troubleshooting Rápido

### ❌ "Não há encontro ativo"
**Solução**: `UPDATE encontro SET ativo = TRUE WHERE id = [ID];`

### ❌ Valor total zerado
**Solução**: Verificar triggers do banco → recriar com `modelo_venda.sql`

### ❌ Controller não encontrado
**Solução**: Verificar `@Named` + `@ViewScoped` → rebuild: `mvn clean install`

### ❌ LazyInitializationException
**Solução**: Usar `LEFT JOIN FETCH` nas queries do repository

---

## 📞 Suporte

**Documentação Completa**: Consulte os arquivos `.md` em `docs/`  
**Logs**: `$WILDFLY_HOME/standalone/log/server.log`  
**Database**: Verificar conectividade com `psql -U postgres -d segueme_db`

---

## 📝 Checklist de Implementação

- [x] ✅ Banco de dados (DDL)
- [x] ✅ Entidades JPA
- [x] ✅ Repositories
- [x] ✅ Services
- [x] ✅ Controllers
- [ ] ⏳ Páginas XHTML
- [ ] ⏳ Integração no menu
- [ ] ⏳ Testes funcionais
- [ ] ⏳ Deploy em produção

**Status Atual**: Backend 100% completo | Frontend pendente (exemplos XHTML prontos na documentação)

---

## 📜 Licença

Este módulo é parte integrante do sistema Segue-me-sj.  
Todos os direitos reservados.

---

**Última Atualização**: 2024  
**Versão do Módulo**: 1.0.0
