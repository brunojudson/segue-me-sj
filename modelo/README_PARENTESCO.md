# Solução de Vinculação de Parentesco

## Objetivo

Implementar um mecanismo para vincular pais e filhos cadastrados no sistema e evitar que parentes próximos (pais, filhos ou irmãos) sejam alocados na mesma equipe de um encontro.

## Arquivos Modificados

### 1. Entidade Pessoa ([Pessoa.java](../../src/main/java/br/com/segueme/entity/Pessoa.java))

**Novos campos adicionados:**

- `pai` - Relacionamento ManyToOne com a entidade Pessoa (referência ao pai)
- `mae` - Relacionamento ManyToOne com a entidade Pessoa (referência à mãe)
- `filhosDoPai` - Relacionamento OneToMany com filhos onde esta pessoa é o pai
- `filhosDaMae` - Relacionamento OneToMany com filhos onde esta pessoa é a mãe

**Novos métodos:**

- `getTodosFilhos()` - Retorna todos os filhos (união de filhosDoPai e filhosDaMae)
- `ehParenteProximoDe(Pessoa outraPessoa)` - Verifica se há parentesco próximo entre duas pessoas
  - Detecta: pai/mãe, filho(a), irmão(ã)

### 2. Controller Trabalhador ([TrabalhadorController.java](../../src/main/java/br/com/segueme/controller/TrabalhadorController.java))

**Novos métodos adicionados:**

- `validarParentescoNaEquipe(Pessoa pessoa, Long equipeId)` - Valida se há parentes na equipe antes de adicionar um trabalhador
- `determinarTipoParentesco(Pessoa pessoa1, Pessoa pessoa2)` - Retorna uma descrição amigável do tipo de parentesco para mensagens ao usuário

**Modificação no método `salvarInterno()`:**
- Adicionada chamada à validação de parentesco antes de salvar o trabalhador
- Se houver conflito, uma exceção é lançada com mensagem detalhada

### 3. Controller Pessoa ([PessoaController.java](../../src/main/java/br/com/segueme/controller/PessoaController.java))

**Novos métodos adicionados:**

- `getPessoasDisponiveisParaPai()` - Retorna lista de pessoas do sexo masculino para seleção como pai
- `getPessoasDisponiveisParaMae()` - Retorna lista de pessoas do sexo feminino para seleção como mãe
- `getFilhosDaPessoaAtual()` - Retorna todos os filhos da pessoa sendo editada

### 4. Formulários XHTML

#### [cadastro.xhtml](../../src/main/webapp/pages/pessoa/cadastro.xhtml)
- ✅ Adicionados campos `p:selectOneMenu` para vincular pai e mãe
- ✅ Mantidos campos de texto `filiacaoPai` e `filiacaoMae` para compatibilidade
- ✅ Filtros por sexo (M/F) nas listas de seleção

#### [visualizar.xhtml](../../src/main/webapp/pages/pessoa/visualizar.xhtml)
- ✅ Exibe pai/mãe vinculados com ícone de confirmação
- ✅ Fallback para campos de texto quando não há vínculo
- ✅ Exibe tabela com filhos cadastrados (se houver)

#### [migration_parentesco.sql](../migration_parentesco.sql)
Script de migração para adicionar as colunas no banco de dados:
- `pai_id` - Foreign key para a tabela pessoa
- `mae_id` - Foreign key para a tabela pessoa
- Índices para melhor performance
- Constraints de integridade referencial

#### [exemplo_vinculacao_parentesco.sql](../exemplo_vinculacao_parentesco.sql)
Exemplos práticos de como vincular pessoas existentes, incluindo:
- Vincular pais a filhos
- Consultas para verificar vinculações
- Buscar irmãos
- Scripts de atualização em lote

## Como Usar

### Passo 1: Executar a Migração

```sql
-- Execute o script de migração no banco de dados
\i modelo/migration_parentesco.sql
```

### Passo 2: Vincular Pessoas Existentes

Existem duas formas de vincular pais e filhos:

**Opção A: Via SQL (para dados existentes)**

```sql
-- Exemplo: Vincular Pedro Silva aos seus pais João e Maria
UPDATE pessoa 
SET pai_id = (SELECT id FROM pessoa WHERE nome = 'João Silva' LIMIT 1),
    mae_id = (SELECT id FROM pessoa WHERE nome = 'Maria Silva' LIMIT 1)
WHERE nome = 'Pedro Silva';
```

**Opção B: Via Interface de Cadastro de Pessoa (para novos cadastros)**

Ao cadastrar ou editar uma pessoa na interface web, haverá campos para selecionar o pai e a mãe dentre as pessoas já cadastradas no sistema.

### Passo 3: Adicionar Trabalhadores às Equipes

Ao adicionar um trabalhador a uma equipe:

1. O sistema verificará automaticamente se há parentes próximos na equipe
2. Se houver, será exibida uma mensagem de erro impedindo o cadastro
3. Exemplo de mensagem: *"Não é permitido adicionar Pedro Silva na equipe 'Equipe São José' pois o pai 'João Silva' já faz parte dela."*

## Vantagens da Solução

✅ **Validação Automática:** O sistema impede automaticamente conflitos de parentesco  
✅ **Mensagens Claras:** Informa exatamente qual parente está na equipe e o tipo de parentesco  
✅ **Flexível:** Permite vincular apenas pai, apenas mãe, ou ambos  
✅ **Detecta Irmãos:** Identifica irmãos automaticamente (mesmo pai ou mesma mãe)  
✅ **Compatível:** Mantém os campos de texto `filiacao_pai` e `filiacao_mae` para casos onde o parente não está cadastrado  

## Tipos de Parentesco Detectados

- **Pai/Mãe:** Verifica se a pessoa que está sendo adicionada é pai ou mãe de alguém na equipe
- **Filho/Filha:** Verifica se a pessoa que está sendo adicionada é filho(a) de alguém na equipe
- **Irmão/Irmã:** Verifica se têm o mesmo pai ou a mesma mãe

## Observações Importantes

1. **Campos de Texto Preservados:** Os campos `filiacao_pai` e `filiacao_mae` (String) continuam existindo para registrar nomes de pais/mães que não estão cadastrados no sistema

2. **Validação no Salvamento:** A validação ocorre ao tentar salvar um trabalhador, impedindo o cadastro se houver conflito

3. **Performance:** Os índices criados nas colunas `pai_id` e `mae_id` garantem consultas rápidas

4. **Edição de Trabalhadores:** Ao editar um trabalhador existente, o sistema ignora a própria pessoa na validação de parentesco

## Exemplos de Uso

### Cenário 1: Pai e Filho na Mesma Equipe

**Situação:** João Silva já é trabalhador da Equipe São José. Tentativa de adicionar seu filho Pedro Silva à mesma equipe.

**Resultado:** ❌ Sistema impede o cadastro com a mensagem:  
*"Não é permitido adicionar Pedro Silva na equipe 'Equipe São José' pois o pai 'João Silva' já faz parte dela."*

### Cenário 2: Irmãos na Mesma Equipe

**Situação:** Pedro Silva (filho de João e Maria) já está na Equipe São José. Tentativa de adicionar Ana Silva (também filha de João e Maria).

**Resultado:** ❌ Sistema impede o cadastro com a mensagem:  
*"Não é permitido adicionar Ana Silva na equipe 'Equipe São José' pois o irmão 'Pedro Silva' já faz parte dela."*

### Cenário 3: Sem Parentesco

**Situação:** Pedro Silva está na Equipe São José. Tentativa de adicionar Carlos Costa (sem parentesco).

**Resultado:** ✅ Cadastro permitido normalmente.

## Consultas Úteis

### Ver todas as famílias cadastradas:

```sql
SELECT 
    p.nome AS filho,
    pai.nome AS pai,
    mae.nome AS mae
FROM pessoa p
LEFT JOIN pessoa pai ON p.pai_id = pai.id
LEFT JOIN pessoa mae ON p.mae_id = mae.id
WHERE p.pai_id IS NOT NULL OR p.mae_id IS NOT NULL
ORDER BY COALESCE(pai.nome, mae.nome), p.nome;
```

### Ver conflitos potenciais em uma equipe:

```sql
WITH equipe_membros AS (
    SELECT t.equipe_id, t.pessoa_id, e.nome as equipe_nome
    FROM trabalhador t
    JOIN equipe e ON t.equipe_id = e.id
    WHERE t.ativo = true
)
SELECT DISTINCT
    em1.equipe_nome,
    p1.nome AS pessoa1,
    p2.nome AS pessoa2,
    CASE 
        WHEN p1.pai_id = p2.id OR p1.mae_id = p2.id THEN 'Filho/Pai'
        WHEN p2.pai_id = p1.id OR p2.mae_id = p1.id THEN 'Pai/Filho'
        WHEN p1.pai_id = p2.pai_id THEN 'Irmãos (mesmo pai)'
        WHEN p1.mae_id = p2.mae_id THEN 'Irmãos (mesma mãe)'
    END AS parentesco
FROM equipe_membros em1
JOIN equipe_membros em2 ON em1.equipe_id = em2.equipe_id AND em1.pessoa_id < em2.pessoa_id
JOIN pessoa p1 ON em1.pessoa_id = p1.id
JOIN pessoa p2 ON em2.pessoa_id = p2.id
WHERE 
    p1.pai_id = p2.id OR p1.mae_id = p2.id OR
    p2.pai_id = p1.id OR p2.mae_id = p1.id OR
    (p1.pai_id IS NOT NULL AND p1.pai_id = p2.pai_id) OR
    (p1.mae_id IS NOT NULL AND p1.mae_id = p2.mae_id)
ORDER BY em1.equipe_nome, p1.nome;
```

## Próximos Passos (Opcional)

1. **Interface de Vinculação:** Criar tela para facilitar vinculação de pais/filhos via interface web
2. **Relatório de Famílias:** Criar relatório mostrando todas as famílias cadastradas
3. **Sugestão Automática:** Ao cadastrar pessoa, sugerir possíveis pais baseado em sobrenomes similares
4. **Validação ao Transferir:** Estender validação para transferências de trabalhadores entre equipes
