# 🎯 Sistema Segue-me

> Plataforma completa para gestão de Encontros de Jovens com Cristo

[![Java](https://img.shields.io/badge/Java-11-orange.svg)](https://www.oracle.com/java/)
[![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-8-blue.svg)](https://jakarta.ee/)
[![PrimeFaces](https://img.shields.io/badge/PrimeFaces-12.0-green.svg)](https://www.primefaces.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12+-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

## 📋 Sobre o Projeto

O **Sistema Segue-me** é uma aplicação web desenvolvida para gerenciar de forma completa e integrada os Encontros de Jovens, movimento de evangelização católica que realiza retiros espirituais para jovens.

O sistema oferece funcionalidades abrangentes para:
- ✅ Registro de participantes (encontristas)
- ✅ Organização de equipes e trabalhadores
- ✅ Gestão de dirigentes e pastas organizacionais
- ✅ Controle financeiro (receitas e despesas)
- ✅ Programação de palestras
- ✅ Geração de relatórios gerenciais
- ✅ Emissão de fichas digitais com QR Code
- ✅ Sistema de vendas de artigos religiosos
- ✅ API REST para integrações

## 🚀 Principais Funcionalidades

### 👤 Gestão de Pessoas
- Cadastro completo com dados pessoais, fotos e contatos
- Controle de casais
- Histórico de participação em encontros

### 🎯 Gestão de Encontros
- Criação de eventos com datas, local, tema e valores
- Controle de capacidade máxima
- Status ativo/finalizado

### 📝 Gestão de Encontristas (Seguimistas)
- Inscrição com controle de pagamentos
- Distribuição em círculos (A-H)
- Geração de fichas digitais com QR Code
- Token único para identificação

### 👨‍👩‍👧‍👦 Gestão de Equipes
- Organização de trabalhadores por tipo de equipe
- Equipes permanentes ou específicas de encontro
- Controle de dirigentes e pastas

### 🎤 Gestão de Palestras
- Cadastro de palestras com tema e descrição
- Agendamento (data/hora)
- Vinculação de palestrantes (individual, casal ou grupo)

### 💰 Controle Financeiro
- Registro de contribuições de trabalhadores
- Lançamento de receitas e despesas por categoria
- Anexo de comprovantes
- Relatórios de fechamento

### 📊 Relatórios
- Encontristas por encontro
- Trabalhadores por equipe
- Movimentação financeira
- Dashboard gerencial
- Exportação em PDF

## 🛠️ Tecnologias

### Backend
- **Java 11** - Linguagem de programação
- **Jakarta EE 8** - Plataforma enterprise (JPA, CDI, JAX-RS, Bean Validation)
- **Hibernate 5.6.15** - Implementação JPA (ORM)
- **PostgreSQL 12+** - Banco de dados relacional

### Frontend
- **JSF 2.3** - Framework web MVC
- **PrimeFaces 12.0.0** - Biblioteca de componentes UI
- **PrimeFaces Extensions 12.0.0** - Componentes adicionais
- **OmniFaces 2.7.3** - Utilitários JSF

### Bibliotecas
- **Lombok 1.18.28** - Redução de boilerplate
- **jBCrypt 0.4** - Hash de senhas
- **ZXing 3.5.2** - Geração de QR Codes
- **JasperReports 6.21.0** - Geração de relatórios PDF
- **Logback** - Sistema de logging

## 📁 Estrutura do Projeto

```
segue-me-sj/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/segueme/
│   │   │       ├── api/              # REST Resources
│   │   │       ├── config/           # Filtros e configurações
│   │   │       ├── controller/       # Managed Beans (MVC)
│   │   │       ├── converter/        # Conversores JSF
│   │   │       ├── entity/           # Entidades JPA
│   │   │       ├── repository/       # Camada de persistência (DAO)
│   │   │       ├── service/          # Lógica de negócio
│   │   │       └── util/             # Utilitários
│   │   ├── resources/
│   │   │   ├── logback.xml
│   │   │   └── META-INF/
│   │   │       ├── beans.xml
│   │   │       └── persistence.xml
│   │   └── webapp/
│   │       ├── pages/                # Páginas XHTML
│   │       ├── resources/            # CSS, JS, imagens
│   │       ├── templates/            # Templates JSF
│   │       └── WEB-INF/
│   └── test/                         # Testes unitários
├── modelo/                           # Scripts SQL
├── docs/                             # Documentação
└── pom.xml                           # Configuração Maven
```

## 🏗️ Arquitetura

O sistema segue uma arquitetura em **camadas**:

```
┌─────────────────────────────┐
│   Camada de Apresentação    │  JSF + PrimeFaces (View)
├─────────────────────────────┤
│   Camada de Controle        │  Managed Beans (Controller)
├─────────────────────────────┤
│   Camada de Serviço         │  Regras de Negócio
├─────────────────────────────┤
│   Camada de Persistência    │  Repositórios (DAO)
├─────────────────────────────┤
│   Camada de Dados           │  Entidades JPA (Model)
├─────────────────────────────┤
│   Banco de Dados            │  PostgreSQL
└─────────────────────────────┘
```

### Padrões de Projeto
- **MVC** - Separação Model-View-Controller
- **Service Layer** - Lógica de negócio centralizada
- **Repository (DAO)** - Abstração de persistência
- **Dependency Injection (CDI)** - Inversão de controle
- **Front Controller** - FacesServlet
- **Template Method** - Templates Facelets

## 📦 Instalação e Configuração

### Pré-requisitos
- JDK 11 ou superior
- PostgreSQL 12 ou superior
- Servidor de aplicação Jakarta EE 8 (Wildfly, Payara, TomEE)
- Maven 3.6+

### Passos de Instalação

1. **Clone o repositório**
```bash
git clone <url-do-repositorio>
cd segue-me-sj
```

2. **Configure o banco de dados PostgreSQL**
```sql
CREATE DATABASE segueme_db;
CREATE USER segueme_user WITH PASSWORD 'senha_segura';
GRANT ALL PRIVILEGES ON DATABASE segueme_db TO segueme_user;
```

3. **Configure o DataSource no servidor de aplicação**
   
   Para Wildfly, adicione em `standalone.xml`:
```xml
<datasource jndi-name="java:jboss/datasources/SegueMeDS" 
            pool-name="SegueMeDS">
    <connection-url>jdbc:postgresql://localhost:5432/segueme_db</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>segueme_user</user-name>
        <password>senha_segura</password>
    </security>
</datasource>
```

4. **Configure o caminho de armazenamento de fotos**
   
   Adicione propriedade de sistema:
```bash
-Dcaminho_fotos=/caminho/para/diretorio/fotos
```

5. **Compile o projeto**
```bash
mvn clean package
```

6. **Deploy no servidor**
```bash
# Copie o arquivo WAR gerado para o diretório de deploy
cp target/segue-me-sistema-1.0.0.war /caminho/servidor/deployments/
```

7. **Acesse a aplicação**
```
http://localhost:8080/segue-me-sistema
```

## 📚 Documentação

A documentação completa do projeto está disponível em formato HTML na pasta `docs/`:

| Documento | Descrição | Público-Alvo |
|-----------|-----------|--------------|
| [📖 Índice da Documentação](docs/index.html) | Portal de entrada para toda a documentação | Todos |
| [🎯 Visão Geral do Projeto](docs/visao_geral_projeto.html) | Resumo executivo e benefícios | Coordenadores, Gestores |
| [📋 Especificação de Requisitos](docs/especificacao_requisitos.html) | Requisitos funcionais, não-funcionais, casos de uso | Analistas, Desenvolvedores |
| [📊 Modelo de Dados](docs/modelo_entidade_relacionamento.html) | Modelo ER completo do banco | DBAs, Desenvolvedores |
| [🏗️ Arquitetura do Sistema](docs/arquitetura_sistema.html) | Arquitetura técnica, camadas, padrões | Arquitetos, Desenvolvedores |
| [📖 Manual do Usuário](docs/manual_usuario.html) | Guia passo a passo de uso | Usuários Finais |

Para visualizar a documentação, abra o arquivo `docs/index.html` em um navegador.

## 🔒 Segurança

- ✅ **Senhas criptografadas** com BCrypt
- ✅ **Controle de acesso** baseado em perfis de usuário
- ✅ **Auditoria** de ações críticas
- ✅ **Proteção contra SQL Injection** via JPA
- ✅ **Validações** client-side e server-side
- ✅ **HTTPS** recomendado para produção

### Perfis de Usuário
- **Administrador** - Acesso total ao sistema
- **Coordenador** - Gestão de encontros e equipes
- **Secretário** - Cadastros e inscrições
- **Tesoureiro** - Gestão financeira
- **Consulta** - Visualização apenas

## 🧪 Testes

```bash
# Executar testes unitários
mvn test

# Executar testes com cobertura
mvn test jacoco:report
```

## 📈 Performance

- ✅ **Lazy Loading** em relacionamentos JPA
- ✅ **Paginação** em listas grandes
- ✅ **Índices** em campos de busca frequente
- ✅ **Connection Pooling** configurável
- ✅ **Cache de segundo nível** do Hibernate (opcional)

## 🤝 Contribuindo

Para contribuir com o projeto:

1. Crie um branch para sua feature (`git checkout -b feature/MinhaFeature`)
2. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
3. Push para o branch (`git push origin feature/MinhaFeature`)
4. Abra um Pull Request

## 📝 Licença

Este projeto é proprietário e todos os direitos são reservados.

## 👥 Autores

- **Equipe de Desenvolvimento** - *Sistema Segue-me*

## 📞 Suporte

Para questões técnicas ou suporte:
- **Email**: contato@segueme.com.br
- **Documentação**: `docs/index.html`

## 🎯 Roadmap

### Versão 1.1 (Planejado)
- [ ] Aplicativo mobile Android/iOS
- [ ] Notificações push
- [ ] Integração com plataformas de pagamento
- [ ] Dashboard com gráficos interativos
- [ ] Exportação para Excel

### Versão 2.0 (Futuro)
- [ ] Migração para microserviços
- [ ] PWA (Progressive Web App)
- [ ] Inteligência Artificial para sugestões
- [ ] Multi-tenancy (múltiplas comunidades)
- [ ] Chat integrado

## 🙏 Agradecimentos

Desenvolvido com ❤️ para servir ao Reino de Deus através da evangelização de jovens.

> *"Vem e segue-me" (Mc 10,21)*

---

**Versão**: 1.0.0  
**Última Atualização**: Abril 2026  
**Status**: ✅ Em Produção
