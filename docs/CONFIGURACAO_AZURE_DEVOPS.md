# Configuração de Segurança - Azure DevOps

## Problema Resolvido

O projeto tinha um token do Azure DevOps hardcoded no código, o que representa um risco de segurança. Esse problema foi resolvido implementando um sistema de configuração seguro.

## Como Configurar

### 1. Variável de Ambiente (Recomendado)

Configure a variável de ambiente `AZURE_DEVOPS_TOKEN` com seu token pessoal:

**Windows (PowerShell):**
```powershell
$env:AZURE_DEVOPS_TOKEN="seu_token_aqui"
```

**Windows (CMD):**
```cmd
set AZURE_DEVOPS_TOKEN=seu_token_aqui
```

**Linux/Mac:**
```bash
export AZURE_DEVOPS_TOKEN="seu_token_aqui"
```

### 2. Arquivo de Configuração

O arquivo `src/main/resources/application.properties` contém as configurações do Azure DevOps:

```properties
# Configurações do Azure DevOps
azure.devops.base.url=https://devops.caixa
azure.devops.collection=projetos
azure.devops.projects.collection=Caixa
# Token será lido de variável de ambiente AZURE_DEVOPS_TOKEN
azure.devops.token=${AZURE_DEVOPS_TOKEN:}
```

### 3. Para Desenvolvimento Local

1. Crie sua variável de ambiente com o token
2. Reinicie seu IDE/terminal
3. A aplicação automaticamente pegará o token da variável de ambiente

### 4. Para Deploy em Produção

Configure a variável de ambiente `AZURE_DEVOPS_TOKEN` no servidor/container onde a aplicação será executada.

## Segurança

- ✅ Token não está mais no código fonte
- ✅ Token é lido de variável de ambiente
- ✅ Configuração flexível via properties
- ✅ Logs não expõem o token

## Arquivos Modificados

- `src/main/java/br/com/segueme/service/AzureDevOpsService.java` - Removido token hardcoded
- `src/main/java/br/com/segueme/config/ConfigService.java` - Nova classe para gerenciar configurações
- `src/main/resources/application.properties` - Arquivo de configuração criado

## Como Obter um Token do Azure DevOps

1. Acesse seu Azure DevOps
2. Clique no seu perfil → Personal Access Tokens
3. Crie um novo token com as permissões necessárias
4. Copie o token e configure na variável de ambiente
