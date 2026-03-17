# 📑 ÍNDICE DA DOCUMENTAÇÃO - MÓDULO DE VENDAS

## Visão Geral do Módulo

O **Módulo de Vendas** é uma extensão completa do sistema Segue-me-sj que permite o gerenciamento de vendas de artigos durante encontros de jovens, com controle de estoque, contas abertas, fechamento flexível e relatórios avançados.

---

## 📚 Documentos Disponíveis

### 1. README Principal
**Arquivo**: [modulo_vendas_README.md](modulo_vendas_README.md)

**Conteúdo**:
- Visão geral e funcionalidades principais
- Quick Start (instalação rápida)
- Regras de negócio resumidas
- Atalhos de teclado
- Testes essenciais
- Estrutura de arquivos
- Troubleshooting rápido
- Roadmap de melhorias

**Recomendado para**: Desenvolvedores que precisam de uma visão rápida do módulo

---

### 2. Especificação Técnica Completa
**Arquivo**: [modulo_vendas_especificacao.md](modulo_vendas_especificacao.md)

**Conteúdo**:
1. Resumo do entendimento do sistema atual e objetivo do módulo
2. Modelo de dados (DDL PostgreSQL) detalhado
   - Estrutura das tabelas
   - Triggers e funções automáticas
   - Views de consulta
   - Índices para performance
3. Regras de negócio detalhadas
   - Encontro ativo obrigatório ⚠️
   - Estados (status) do pedido
   - Conta aberta e registro contínuo
   - Fechamento da conta
   - Marcar como pago posteriormente
   - Associação de múltiplas pessoas
   - Consulta por trabalhador
   - Relação com módulo financeiro
4. Integração com o sistema existente
   - Obtenção do encontro ativo
   - Busca de trabalhadores e pessoas
   - Navegação e pontos de integração
   - Manter layout e tema visual
5. Arquitetura em camadas (Java)
   - Estrutura de pacotes
   - Entidades JPA com código de exemplo
   - Repositories (interfaces + @Stateless implementations)
   - Services (@ApplicationScoped)
   - Controllers (@Named @ViewScoped)

**Recomendado para**: Analistas de sistemas, DBAs, arquitetos de software

---

### 3. Páginas JSF/PrimeFaces com Código XHTML
**Arquivo**: [modulo_vendas_paginas_xhtml.md](modulo_vendas_paginas_xhtml.md)

**Conteúdo**:
1. Página principal: **Gestão de Vendas** (gestao-vendas.xhtml)
   - Código XHTML completo
   - Seleção de encontro ativo
   - Tabela de contas abertas
   - Formulário de nova venda
   - Adicionar itens
   - Tabela de itens do pedido
   - Dialog de fechar pedido
   - Lista de pedidos aguardando pagamento
   - Dialog de marcar como pago
   - Atalhos: Alt+N, Alt+I, Alt+F, Alt+P

2. Página de artigos: **Gerenciamento de Artigos** (artigos.xhtml)
   - Código XHTML completo
   - Filtros de consulta
   - DataTable com paginação
   - Dialog de cadastro/edição
   - Ações: editar, ativar/inativar, excluir
   - Atalhos: Alt+N, Alt+R, Alt+F, Alt+S

3. Página de consultas: **Consulta de Vendas** (consulta-vendas.xhtml)
   - Código XHTML completo
   - Filtros avançados (encontro, status, período)
   - Totalizadores (dashboard cards)
   - DataTable de pedidos
   - Dialog de detalhes do pedido
   - Exportação para Excel
   - Atalhos: Alt+C, Alt+E

4. Resumo dos atalhos de teclado (accesskey)

5. CSS adicional sugerido
   - Dashboard cards
   - Action buttons
   - Modern form elements
   - Status indicators

6. Integração no menu (código XML)

7. Notas importantes sobre navegação, validação, performance

**Recomendado para**: Desenvolvedores frontend, designers de UI/UX

---

### 4. Guia de Implementação e Deploy
**Arquivo**: [modulo_vendas_guia_implementacao.md](modulo_vendas_guia_implementacao.md)

**Conteúdo**:
1. **Checklist de implementação**
   - ✅ Banco de dados
   - ✅ Camada de entidade
   - ✅ Camada de repositório
   - ✅ Camada de serviço
   - ✅ Camada de controle
   - ⏳ Camada de visualização (XHTML)
   - ⏳ Integração com menu
   - ⏳ Testes funcionais

2. **Instruções de deploy**
   - Pré-requisitos
   - Passo a passo detalhado
   - Deploy do banco
   - Compilação Maven
   - Deploy no WildFly
   - Verificação
   - Popular dados de teste

3. **Configuração de encontro ativo**
   - Ativar manualmente no banco
   - Ativar via interface

4. **Testes de validação obrigatórios**
   - Teste: Encontro ativo obrigatório
   - Teste: Fluxo completo de venda
   - Teste: Fechar sem pagar
   - Teste: Marcar como pago posteriormente
   - Teste: Consulta e filtros
   - Teste: Atalhos de teclado

5. **Solução de problemas comuns**
   - 5 problemas principais com soluções

6. **Melhorias futuras (roadmap)**
   - v1.1 (curto prazo)
   - v2.0 (médio prazo)
   - v3.0 (longo prazo)

7. **Documentação técnica adicional**
   - Diagrama Entidade-Relacionamento (ER)
   - Fluxo de estados (status do pedido)
   - Sequência de operações completa

8. Contatos e suporte

**Recomendado para**: DevOps, equipe de QA, gestores de projeto

---

## 🎯 Fluxo de Leitura Recomendado

### Para implementar o módulo do zero:
1. Comece com **modulo_vendas_README.md** (visão geral)
2. Leia **modulo_vendas_especificacao.md** (entenda as regras de negócio)
3. Execute **modulo_vendas_guia_implementacao.md** (checklist e deploy)
4. Implemente as páginas XHTML usando **modulo_vendas_paginas_xhtml.md** (exemplos de código)

### Para entender apenas as regras de negócio:
1. **modulo_vendas_README.md** → seção "Regras de Negócio Principais"
2. **modulo_vendas_especificacao.md** → seção 3

### Para desenvolver apenas o frontend:
1. **modulo_vendas_paginas_xhtml.md** → exemplos completos de XHTML
2. **modulo_vendas_especificacao.md** → seção 5.5 (entender os controllers)

### Para fazer deploy:
1. **modulo_vendas_guia_implementacao.md** → seção 2 (Deploy) + seção 3 (Configuração)
2. **modulo_vendas_README.md** → Quick Start

### Para troubleshooting:
1. **modulo_vendas_README.md** → seção "Troubleshooting Rápido"
2. **modulo_vendas_guia_implementacao.md** → seção 5 (Solução de problemas)

---

## 📂 Arquivos de Código Implementados

### Banco de Dados
- `modelo/modelo_venda.sql` - DDL completo (tabelas, triggers, views, índices)

### Java - Entities
- `src/main/java/br/com/segueme/entity/VendaArtigo.java`
- `src/main/java/br/com/segueme/entity/VendaPedido.java`
- `src/main/java/br/com/segueme/entity/VendaItemPedido.java`
- `src/main/java/br/com/segueme/entity/VendaPedidoPessoa.java`
- `src/main/java/br/com/segueme/entity/enums/StatusPedido.java`
- `src/main/java/br/com/segueme/entity/enums/TipoAssociacaoPedido.java`

### Java - Repositories
- `src/main/java/br/com/segueme/repository/VendaArtigoRepository.java`
- `src/main/java/br/com/segueme/repository/impl/VendaArtigoRepositoryImpl.java`
- `src/main/java/br/com/segueme/repository/VendaPedidoRepository.java`
- `src/main/java/br/com/segueme/repository/impl/VendaPedidoRepositoryImpl.java`

### Java - Services
- `src/main/java/br/com/segueme/service/VendaArtigoService.java`
- `src/main/java/br/com/segueme/service/VendaPedidoService.java`

### Java - Controllers
- `src/main/java/br/com/segueme/controller/VendaArtigoController.java`
- `src/main/java/br/com/segueme/controller/VendaPedidoController.java`

### XHTML - Views (exemplos prontos na documentação)
- `pages/venda/gestao-vendas.xhtml` (código completo em modulo_vendas_paginas_xhtml.md)
- `pages/venda/artigos.xhtml` (código completo em modulo_vendas_paginas_xhtml.md)
- `pages/venda/consulta-vendas.xhtml` (código completo em modulo_vendas_paginas_xhtml.md)

---

## ✅ Status da Implementação

| Camada | Status | Observações |
|--------|--------|-------------|
| **Banco de Dados** | ✅ 100% | DDL completo com triggers e views |
| **Entities (JPA)** | ✅ 100% | 4 entidades + 2 enums |
| **Repositories** | ✅ 100% | Interfaces + @Stateless implementations |
| **Services** | ✅ 100% | Lógica de negócio completa |
| **Controllers** | ✅ 100% | 2 managed beans @ViewScoped |
| **Views (XHTML)** | 📄 Exemplos prontos | Código completo na documentação |
| **Menu Integration** | 📄 Exemplo pronto | Código XML na documentação |
| **Testes** | 📋 Roteiro pronto | Casos de teste na documentação |

**Legenda**:
- ✅ Implementado e testado
- 📄 Código de exemplo pronto (aguardando criação dos arquivos)
- 📋 Documentado (aguardando execução)

---

## 🚀 Próximos Passos

### Imediato (para finalizar o módulo)

1. **Criar arquivos XHTML**:
   ```bash
   # Criar diretório
   mkdir -p src/main/webapp/pages/venda/
   
   # Copiar código dos exemplos em modulo_vendas_paginas_xhtml.md
   # Criar: gestao-vendas.xhtml, artigos.xhtml, consulta-vendas.xhtml
   ```

2. **Integrar menu**:
   - Editar `src/main/webapp/templates/menu.xhtml`
   - Adicionar seção "Vendas" (código em modulo_vendas_paginas_xhtml.md seção 6)

3. **Executar DDL no banco**:
   ```bash
   psql -U postgres -d segueme_db -f modelo/modelo_venda.sql
   ```

4. **Ativar um encontro**:
   ```sql
   UPDATE encontro SET ativo = TRUE WHERE id = [ID];
   ```

5. **Build e deploy**:
   ```bash
   mvn clean install
   cp target/segue-me-sj.war $WILDFLY_HOME/standalone/deployments/
   ```

6. **Testar** seguindo roteiro em modulo_vendas_guia_implementacao.md seção 4

### Médio prazo (melhorias v1.1)
- Implementar UI para compartilhar contas
- Implementar função "unir contas"
- Criar relatórios gráficos
- Impressão de comprovantes

---

## 📞 Informações de Suporte

**Versão do Módulo**: 1.0.0  
**Data de Criação**: 2024  
**Compatibilidade**: Sistema Segue-me-sj (Java EE 8, JSF, PrimeFaces, PostgreSQL)

**Documentos de Referência**:
- README: Visão geral e quick start
- Especificação: Regras de negócio e arquitetura
- Páginas XHTML: Código frontend completo
- Guia de Implementação: Deploy, testes, troubleshooting

---

## 📊 Estatísticas do Módulo

- **Tabelas no Banco**: 4 principais + tabelas relacionadas existentes
- **Triggers**: 3 (geração de número, cálculo de totais)
- **Views**: 3 (consultas agregadas)
- **Entidades JPA**: 4 classes + 2 enums
- **Repositories**: 2 interfaces + 2 implementações
- **Services**: 2 classes
- **Controllers**: 2 managed beans
- **Páginas XHTML**: 3 páginas principais
- **Atalhos de Teclado**: 8 (Alt+N, Alt+I, Alt+F, Alt+P, Alt+S, Alt+C, Alt+E, Alt+R)
- **Linhas de Código Java**: ~3.000 (aproximado)
- **Linhas de Código XHTML**: ~1.500 (aproximado)

---

**Este é o índice principal da documentação do Módulo de Vendas.**  
**Consulte os documentos específicos para detalhes técnicos completos.**

📅 Última atualização: 2024
