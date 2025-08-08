-- Modelo Relacional para o Sistema "Segue-me"

-- Tabela de Pessoas (entidade base)
CREATE TABLE pessoa (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) UNIQUE NOT NULL,
    data_nascimento DATE NOT NULL,
    endereco VARCHAR(200),
    telefone VARCHAR(15),
    email VARCHAR(100),
    sexo CHAR(1),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE
);

-- Tabela de Encontros
CREATE TABLE encontro (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tema VARCHAR(200),
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    local VARCHAR(200) NOT NULL,
    descricao TEXT,
    capacidade_maxima INTEGER DEFAULT 60,
    valor_inscricao DECIMAL(10,2),
    valor_contribuicao_trabalhador DECIMAL(10,2),
    ativo BOOLEAN DEFAULT TRUE
);

-- Tabela de Encontristas (participantes do encontro)
CREATE TABLE encontrista (
    id SERIAL PRIMARY KEY,
    pessoa_id INTEGER NOT NULL REFERENCES pessoa(id),
    encontro_id INTEGER NOT NULL REFERENCES encontro(id),
    data_inscricao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valor_pago DECIMAL(10,2),
    forma_pagamento VARCHAR(50),
    observacoes TEXT,
    UNIQUE (pessoa_id, encontro_id)
);

-- Tabela de Casais
CREATE TABLE casal (
    id SERIAL PRIMARY KEY,
    pessoa1_id INTEGER NOT NULL REFERENCES pessoa(id),
    pessoa2_id INTEGER NOT NULL REFERENCES pessoa(id),
    data_casamento DATE,
    observacoes TEXT,
    UNIQUE (pessoa1_id, pessoa2_id)
);

-- Tabela de Tipos de Equipe
CREATE TABLE tipo_equipe (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    eh_dirigente BOOLEAN DEFAULT FALSE
);

-- Tabela de Equipes
CREATE TABLE equipe (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tipo_equipe_id INTEGER NOT NULL REFERENCES tipo_equipe(id),
    encontro_id INTEGER REFERENCES encontro(id),
    data_inicio DATE,
    data_fim DATE,
    descricao TEXT,
    ativo BOOLEAN DEFAULT TRUE
);

-- Tabela de Pastas (para equipe dirigente)
CREATE TABLE pasta (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    equipe_id INTEGER NOT NULL REFERENCES equipe(id),
    descricao TEXT,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    CONSTRAINT check_equipe_dirigente CHECK (
        EXISTS (
            SELECT 1 FROM equipe e
            JOIN tipo_equipe te ON e.tipo_equipe_id = te.id
            WHERE e.id = equipe_id AND te.eh_dirigente = TRUE
        )
    )
);

-- Tabela de Trabalhadores
CREATE TABLE trabalhador (
    id SERIAL PRIMARY KEY,
    pessoa_id INTEGER NOT NULL REFERENCES pessoa(id),
    equipe_id INTEGER NOT NULL REFERENCES equipe(id),
    encontro_id INTEGER REFERENCES encontro(id),
    eh_coordenador BOOLEAN DEFAULT FALSE,
    foi_encontrista BOOLEAN DEFAULT FALSE,
    encontrista_id INTEGER REFERENCES encontrista(id),
    data_inicio DATE NOT NULL,
    data_fim DATE,
    observacoes TEXT,
    UNIQUE (pessoa_id, equipe_id, encontro_id)
);

-- Tabela de Contribuições
CREATE TABLE contribuicao (
    id SERIAL PRIMARY KEY,
    trabalhador_id INTEGER NOT NULL REFERENCES trabalhador(id),
    valor DECIMAL(10,2) NOT NULL,
    data_pagamento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    forma_pagamento VARCHAR(50) NOT NULL,
    comprovante_url VARCHAR(255),
    observacoes TEXT
);

-- Tabela de Dirigentes (com mandato de 2 anos)
CREATE TABLE dirigente (
    id SERIAL PRIMARY KEY,
    trabalhador_id INTEGER NOT NULL REFERENCES trabalhador(id),
    pasta_id INTEGER NOT NULL REFERENCES pasta(id),
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    CONSTRAINT check_mandato_dois_anos CHECK (
        (data_fim - data_inicio) BETWEEN 720 AND 732
    ),
    UNIQUE (trabalhador_id, pasta_id)
);

-- Tabela para histórico de encontristas que se tornaram trabalhadores
CREATE TABLE historico_encontrista_trabalhador (
    id SERIAL PRIMARY KEY,
    encontrista_id INTEGER NOT NULL REFERENCES encontrista(id),
    trabalhador_id INTEGER NOT NULL REFERENCES trabalhador(id),
    encontro_origem_id INTEGER NOT NULL REFERENCES encontro(id),
    encontro_trabalho_id INTEGER NOT NULL REFERENCES encontro(id),
    data_transicao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (encontrista_id, trabalhador_id)
);


-- Índices para melhorar performance
CREATE INDEX idx_pessoa_nome ON pessoa(nome);
CREATE INDEX idx_encontro_data ON encontro(data_inicio, data_fim);
CREATE INDEX idx_encontrista_encontro ON encontrista(encontro_id);
CREATE INDEX idx_trabalhador_equipe ON trabalhador(equipe_id);
CREATE INDEX idx_trabalhador_encontro ON trabalhador(encontro_id);
CREATE INDEX idx_contribuicao_trabalhador ON contribuicao(trabalhador_id);


-- Tabela de Usuários
CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE
);

-- Tabela de Permissões
CREATE TABLE permissao (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) UNIQUE NOT NULL
);

-- Tabela de Relacionamento entre Usuários e Permissões
CREATE TABLE usuario_permissao (
    usuario_id INTEGER NOT NULL REFERENCES usuario(id),
    permissao_id INTEGER NOT NULL REFERENCES permissao(id),
    PRIMARY KEY (usuario_id, permissao_id)
);
