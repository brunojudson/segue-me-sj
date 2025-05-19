# Análise de Requisitos - Sistema "Segue-me" para Encontro de Jovens

## Visão Geral
O sistema "Segue-me" é uma aplicação para gerenciar encontros de jovens, incluindo participantes, trabalhadores, equipes e contribuições financeiras.

## Entidades Principais

### 1. Pessoa
- Dados básicos: nome, endereço, telefone, email, data de nascimento, etc.
- Tipos de pessoa: Encontrista, Trabalhador, Casal

### 2. Encontrista
- Jovens que participam do encontro (~60 por evento)
- No ano seguinte, tornam-se trabalhadores

### 3. Trabalhador
- Pessoas que trabalham no encontro
- Podem ser ex-encontristas ou voluntários
- Pagam contribuição financeira

### 4. Casal
- Casais que trabalham no encontro
- Pagam contribuição financeira

### 5. Encontro
- Evento principal
- Data de início e fim
- Local
- Tema

### 6. Equipe
- Grupos de trabalho durante o encontro
- Cada equipe tem componentes e coordenadores
- Tipos especiais: equipe dirigente

### 7. Equipe Dirigente
- 4 pastas (subequipes)
- Mandato de 2 anos

### 8. Contribuição
- Pagamentos realizados pelos trabalhadores
- Valor, data, forma de pagamento

## Regras de Negócio

1. Encontristas de um ano se tornam trabalhadores no ano seguinte
2. Todos os trabalhadores (incluindo casais) pagam contribuição
3. Cada equipe possui componentes e coordenadores
4. A equipe dirigente tem 4 pastas e mandato de 2 anos
5. O sistema deve permitir o cadastro e gerenciamento de todas as entidades
6. O sistema deve controlar as contribuições financeiras

## Requisitos Técnicos

1. Modelo relacional de banco de dados
2. Implementação em Java
3. Interface com páginas JSF
4. Arquitetura em camadas usando repository/service (não DAO)
