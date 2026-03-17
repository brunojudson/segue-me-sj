-- ============================================================================
-- MÓDULO DE VENDAS - Sistema Segue-me
-- ============================================================================
-- Script DDL para criação das tabelas do módulo de vendas de artigos
-- Banco de dados: PostgreSQL
-- Data de criação: 09/03/2026
-- ============================================================================

-- ============================================================================
-- TABELA: venda_artigo
-- Descrição: Cadastro de artigos/produtos disponíveis para venda
-- ============================================================================
CREATE TABLE public.venda_artigo (
    id BIGSERIAL NOT NULL,
    codigo VARCHAR(50) NULL,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500) NULL,
    preco_base NUMERIC(10, 2) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    categoria VARCHAR(50) NULL,
    estoque_atual INTEGER NULL DEFAULT 0,
    estoque_minimo INTEGER NULL DEFAULT 0,
    foto_url VARCHAR(255) NULL,
    observacoes VARCHAR(500) NULL,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NULL,
    
    CONSTRAINT venda_artigo_pkey PRIMARY KEY (id),
    CONSTRAINT uk_venda_artigo_codigo UNIQUE (codigo),
    CONSTRAINT ck_venda_artigo_preco_base CHECK (preco_base >= 0),
    CONSTRAINT ck_venda_artigo_estoque_atual CHECK (estoque_atual >= 0),
    CONSTRAINT ck_venda_artigo_estoque_minimo CHECK (estoque_minimo >= 0)
);

COMMENT ON TABLE public.venda_artigo IS 'Cadastro de artigos/produtos para venda durante os encontros';
COMMENT ON COLUMN public.venda_artigo.codigo IS 'Código único do artigo (opcional)';
COMMENT ON COLUMN public.venda_artigo.nome IS 'Nome/descrição do artigo';
COMMENT ON COLUMN public.venda_artigo.preco_base IS 'Preço base do artigo (pode variar por pedido)';
COMMENT ON COLUMN public.venda_artigo.ativo IS 'Indica se o artigo está disponível para venda';
COMMENT ON COLUMN public.venda_artigo.categoria IS 'Categoria do artigo (ex: Camiseta, Livro, Acessório)';
COMMENT ON COLUMN public.venda_artigo.estoque_atual IS 'Quantidade atual em estoque';
COMMENT ON COLUMN public.venda_artigo.estoque_minimo IS 'Quantidade mínima desejada em estoque';

-- ============================================================================
-- TABELA: venda_pedido
-- Descrição: Cabeçalho da venda/conta. Uma conta pode ficar aberta e ir 
--            recebendo itens ao longo do tempo
-- ============================================================================
CREATE TABLE public.venda_pedido (
    id BIGSERIAL NOT NULL,
    numero_pedido VARCHAR(20) NULL,
    encontro_id BIGINT NOT NULL,
    trabalhador_responsavel_id BIGINT NOT NULL,
    data_abertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTO',
    valor_total NUMERIC(10, 2) NOT NULL DEFAULT 0,
    valor_pago NUMERIC(10, 2) NULL,
    forma_pagamento VARCHAR(50) NULL,
    fechado_por_trabalhador_id BIGINT NULL,
    observacoes VARCHAR(1000) NULL,
    
    CONSTRAINT venda_pedido_pkey PRIMARY KEY (id),
    CONSTRAINT uk_venda_pedido_numero UNIQUE (numero_pedido),
    CONSTRAINT fk_venda_pedido_encontro FOREIGN KEY (encontro_id) 
        REFERENCES public.encontro(id) ON DELETE RESTRICT,
    CONSTRAINT fk_venda_pedido_trabalhador FOREIGN KEY (trabalhador_responsavel_id) 
        REFERENCES public.trabalhador(id) ON DELETE RESTRICT,
    CONSTRAINT fk_venda_pedido_fechado_por FOREIGN KEY (fechado_por_trabalhador_id) 
        REFERENCES public.trabalhador(id) ON DELETE SET NULL,
    CONSTRAINT ck_venda_pedido_status CHECK (status IN ('ABERTO', 'AGUARDO_PAGAMENTO', 'PAGO', 'CANCELADO')),
    CONSTRAINT ck_venda_pedido_valor_total CHECK (valor_total >= 0),
    CONSTRAINT ck_venda_pedido_valor_pago CHECK (valor_pago IS NULL OR valor_pago >= 0),
    CONSTRAINT ck_venda_pedido_fechamento CHECK (
        (status = 'ABERTO' AND data_fechamento IS NULL) OR
        (status IN ('AGUARDO_PAGAMENTO', 'PAGO', 'CANCELADO') AND data_fechamento IS NOT NULL)
    )
);

COMMENT ON TABLE public.venda_pedido IS 'Cabeçalho do pedido de venda. Pode ficar aberto e receber itens ao longo do tempo';
COMMENT ON COLUMN public.venda_pedido.numero_pedido IS 'Número único do pedido (gerado automaticamente)';
COMMENT ON COLUMN public.venda_pedido.status IS 'Status: ABERTO (pode adicionar itens), AGUARDO_PAGAMENTO (fechado mas não pago), PAGO, CANCELADO';
COMMENT ON COLUMN public.venda_pedido.trabalhador_responsavel_id IS 'Trabalhador que iniciou/é responsável pela conta';
COMMENT ON COLUMN public.venda_pedido.fechado_por_trabalhador_id IS 'Trabalhador que fechou a conta';
COMMENT ON COLUMN public.venda_pedido.valor_total IS 'Valor total calculado pela soma dos itens';
COMMENT ON COLUMN public.venda_pedido.valor_pago IS 'Valor efetivamente pago (quando status = PAGO)';
COMMENT ON COLUMN public.venda_pedido.forma_pagamento IS 'Forma de pagamento (Dinheiro, PIX, Cartão, etc)';

-- ============================================================================
-- TABELA: venda_item_pedido
-- Descrição: Itens de cada pedido. Um pedido pode ter vários itens
-- ============================================================================
CREATE TABLE public.venda_item_pedido (
    id BIGSERIAL NOT NULL,
    pedido_id BIGINT NOT NULL,
    artigo_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL,
    valor_unitario NUMERIC(10, 2) NOT NULL,
    valor_total_item NUMERIC(10, 2) NOT NULL,
    observacoes VARCHAR(255) NULL,
    data_inclusao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT venda_item_pedido_pkey PRIMARY KEY (id),
    CONSTRAINT fk_venda_item_pedido FOREIGN KEY (pedido_id) 
        REFERENCES public.venda_pedido(id) ON DELETE CASCADE,
    CONSTRAINT fk_venda_item_artigo FOREIGN KEY (artigo_id) 
        REFERENCES public.venda_artigo(id) ON DELETE RESTRICT,
    CONSTRAINT ck_venda_item_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_venda_item_valor_unitario CHECK (valor_unitario >= 0),
    CONSTRAINT ck_venda_item_valor_total CHECK (valor_total_item >= 0)
);

COMMENT ON TABLE public.venda_item_pedido IS 'Itens de cada pedido de venda';
COMMENT ON COLUMN public.venda_item_pedido.quantidade IS 'Quantidade do artigo neste item';
COMMENT ON COLUMN public.venda_item_pedido.valor_unitario IS 'Valor unitário do artigo no momento da venda';
COMMENT ON COLUMN public.venda_item_pedido.valor_total_item IS 'Valor total do item (quantidade * valor_unitario)';

-- ============================================================================
-- TABELA: venda_pedido_pessoa
-- Descrição: Tabela de associação entre pedidos e pessoas. Permite que uma
--            conta seja compartilhada/associada a múltiplas pessoas/trabalhadores
-- ============================================================================
CREATE TABLE public.venda_pedido_pessoa (
    id BIGSERIAL NOT NULL,
    pedido_id BIGINT NOT NULL,
    pessoa_id BIGINT NOT NULL,
    tipo_associacao VARCHAR(30) NOT NULL DEFAULT 'CONSUMIDOR',
    percentual_rateio NUMERIC(5, 2) NULL,
    observacoes VARCHAR(255) NULL,
    data_associacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT venda_pedido_pessoa_pkey PRIMARY KEY (id),
    CONSTRAINT uk_venda_pedido_pessoa UNIQUE (pedido_id, pessoa_id),
    CONSTRAINT fk_venda_pedido_pessoa_pedido FOREIGN KEY (pedido_id) 
        REFERENCES public.venda_pedido(id) ON DELETE CASCADE,
    CONSTRAINT fk_venda_pedido_pessoa_pessoa FOREIGN KEY (pessoa_id) 
        REFERENCES public.pessoa(id) ON DELETE RESTRICT,
    CONSTRAINT ck_venda_pedido_pessoa_tipo CHECK (tipo_associacao IN ('CONSUMIDOR', 'PAGADOR', 'COMPARTILHADO')),
    CONSTRAINT ck_venda_pedido_pessoa_percentual CHECK (percentual_rateio IS NULL OR (percentual_rateio >= 0 AND percentual_rateio <= 100))
);

COMMENT ON TABLE public.venda_pedido_pessoa IS 'Associação entre pedidos e pessoas. Permite unir contas ou registrar múltiplos consumidores';
COMMENT ON COLUMN public.venda_pedido_pessoa.tipo_associacao IS 'Tipo: CONSUMIDOR (consumiu itens), PAGADOR (quem pagou), COMPARTILHADO (conta dividida)';
COMMENT ON COLUMN public.venda_pedido_pessoa.percentual_rateio IS 'Percentual de rateio da conta para esta pessoa (opcional)';

-- ============================================================================
-- ÍNDICES para otimizar consultas frequentes
-- ============================================================================

-- Índices para venda_artigo
CREATE INDEX idx_venda_artigo_ativo ON public.venda_artigo(ativo);
CREATE INDEX idx_venda_artigo_categoria ON public.venda_artigo(categoria) WHERE categoria IS NOT NULL;
CREATE INDEX idx_venda_artigo_nome ON public.venda_artigo(nome);

-- Índices para venda_pedido
CREATE INDEX idx_venda_pedido_encontro ON public.venda_pedido(encontro_id);
CREATE INDEX idx_venda_pedido_trabalhador ON public.venda_pedido(trabalhador_responsavel_id);
CREATE INDEX idx_venda_pedido_status ON public.venda_pedido(status);
CREATE INDEX idx_venda_pedido_data_abertura ON public.venda_pedido(data_abertura);
CREATE INDEX idx_venda_pedido_data_fechamento ON public.venda_pedido(data_fechamento) WHERE data_fechamento IS NOT NULL;
CREATE INDEX idx_venda_pedido_encontro_status ON public.venda_pedido(encontro_id, status);

-- Índices para venda_item_pedido
CREATE INDEX idx_venda_item_pedido ON public.venda_item_pedido(pedido_id);
CREATE INDEX idx_venda_item_artigo ON public.venda_item_pedido(artigo_id);
CREATE INDEX idx_venda_item_data_inclusao ON public.venda_item_pedido(data_inclusao);

-- Índices para venda_pedido_pessoa
CREATE INDEX idx_venda_pedido_pessoa_pedido ON public.venda_pedido_pessoa(pedido_id);
CREATE INDEX idx_venda_pedido_pessoa_pessoa ON public.venda_pedido_pessoa(pessoa_id);
CREATE INDEX idx_venda_pedido_pessoa_tipo ON public.venda_pedido_pessoa(tipo_associacao);

-- ============================================================================
-- SEQUENCE para geração de número de pedido
-- ============================================================================
CREATE SEQUENCE public.seq_numero_pedido START WITH 1 INCREMENT BY 1;

COMMENT ON SEQUENCE public.seq_numero_pedido IS 'Sequência para geração de números de pedido únicos';

-- ============================================================================
-- FUNÇÃO para gerar o número do pedido automaticamente
-- ============================================================================
CREATE OR REPLACE FUNCTION public.fn_gerar_numero_pedido()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.numero_pedido IS NULL THEN
        NEW.numero_pedido := CONCAT('PED', LPAD(NEXTVAL('public.seq_numero_pedido')::TEXT, 8, '0'));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION public.fn_gerar_numero_pedido IS 'Gera automaticamente o número do pedido no formato PED00000001';

-- ============================================================================
-- FUNÇÃO para calcular o valor total do pedido baseado nos itens
-- ============================================================================
CREATE OR REPLACE FUNCTION public.fn_calcular_valor_total_pedido()
RETURNS TRIGGER AS $$
DECLARE
    v_total NUMERIC(10, 2);
BEGIN
    -- Calcula o total de todos os itens associados ao pedido
    SELECT COALESCE(SUM(valor_total_item), 0)
    INTO v_total
    FROM public.venda_item_pedido
    WHERE pedido_id = COALESCE(NEW.pedido_id, OLD.pedido_id);
    
    -- Atualiza o valor total do pedido
    UPDATE public.venda_pedido
    SET valor_total = v_total
    WHERE id = COALESCE(NEW.pedido_id, OLD.pedido_id);
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION public.fn_calcular_valor_total_pedido IS 'Recalcula automaticamente o valor total do pedido sempre que um item é inserido, atualizado ou removido';

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Trigger para gerar número do pedido automaticamente
CREATE TRIGGER trg_gerar_numero_pedido
BEFORE INSERT ON public.venda_pedido
FOR EACH ROW
EXECUTE FUNCTION public.fn_gerar_numero_pedido();

-- Trigger para calcular valor total do pedido ao inserir item
CREATE TRIGGER trg_calcular_total_insert
AFTER INSERT ON public.venda_item_pedido
FOR EACH ROW
EXECUTE FUNCTION public.fn_calcular_valor_total_pedido();

-- Trigger para calcular valor total do pedido ao atualizar item
CREATE TRIGGER trg_calcular_total_update
AFTER UPDATE ON public.venda_item_pedido
FOR EACH ROW
EXECUTE FUNCTION public.fn_calcular_valor_total_pedido();

-- Trigger para calcular valor total do pedido ao excluir item
CREATE TRIGGER trg_calcular_total_delete
AFTER DELETE ON public.venda_item_pedido
FOR EACH ROW
EXECUTE FUNCTION public.fn_calcular_valor_total_pedido();

-- ============================================================================
-- VIEWS úteis para consultas
-- ============================================================================

-- View para listar pedidos com informações completas
CREATE OR REPLACE VIEW public.vw_venda_pedidos_completo AS
SELECT 
    p.id,
    p.numero_pedido,
    p.status,
    p.data_abertura,
    p.data_fechamento,
    p.valor_total,
    p.valor_pago,
    p.forma_pagamento,
    p.observacoes,
    e.id AS encontro_id,
    e.nome AS encontro_nome,
    e.ativo AS encontro_ativo,
    t.id AS trabalhador_id,
    pes.nome AS trabalhador_nome,
    pes.telefone AS trabalhador_telefone,
    eq.nome AS equipe_nome,
    ft.id AS fechado_por_id,
    pesf.nome AS fechado_por_nome,
    COUNT(DISTINCT vip.id) AS total_itens,
    COUNT(DISTINCT vpp.id) AS total_pessoas_associadas
FROM public.venda_pedido p
INNER JOIN public.encontro e ON p.encontro_id = e.id
INNER JOIN public.trabalhador t ON p.trabalhador_responsavel_id = t.id
INNER JOIN public.pessoa pes ON t.pessoa_id = pes.id
LEFT JOIN public.equipe eq ON t.equipe_id = eq.id
LEFT JOIN public.trabalhador ft ON p.fechado_por_trabalhador_id = ft.id
LEFT JOIN public.pessoa pesf ON ft.pessoa_id = pesf.id
LEFT JOIN public.venda_item_pedido vip ON p.id = vip.pedido_id
LEFT JOIN public.venda_pedido_pessoa vpp ON p.id = vpp.pedido_id
GROUP BY p.id, e.id, t.id, pes.id, eq.nome, ft.id, pesf.nome;

COMMENT ON VIEW public.vw_venda_pedidos_completo IS 'Visão completa dos pedidos com informações de encontro, trabalhador e totalizadores';

-- View para relatório de vendas por artigo
CREATE OR REPLACE VIEW public.vw_venda_por_artigo AS
SELECT 
    a.id AS artigo_id,
    a.codigo AS artigo_codigo,
    a.nome AS artigo_nome,
    a.categoria AS artigo_categoria,
    COUNT(vip.id) AS total_vendas,
    SUM(vip.quantidade) AS quantidade_total_vendida,
    SUM(vip.valor_total_item) AS valor_total_vendido,
    AVG(vip.valor_unitario) AS valor_medio_unitario
FROM public.venda_artigo a
LEFT JOIN public.venda_item_pedido vip ON a.id = vip.artigo_id
LEFT JOIN public.venda_pedido p ON vip.pedido_id = p.id
WHERE p.status != 'CANCELADO' OR p.status IS NULL
GROUP BY a.id, a.codigo, a.nome, a.categoria;

COMMENT ON VIEW public.vw_venda_por_artigo IS 'Relatório de vendas agregado por artigo';

-- View para contas abertas por encontro
CREATE OR REPLACE VIEW public.vw_contas_abertas AS
SELECT 
    p.id,
    p.numero_pedido,
    p.data_abertura,
    e.nome AS encontro_nome,
    pes.nome AS trabalhador_nome,
    p.valor_total,
    COUNT(vip.id) AS total_itens
FROM public.venda_pedido p
INNER JOIN public.encontro e ON p.encontro_id = e.id
INNER JOIN public.trabalhador t ON p.trabalhador_responsavel_id = t.id
INNER JOIN public.pessoa pes ON t.pessoa_id = pes.id
LEFT JOIN public.venda_item_pedido vip ON p.id = vip.pedido_id
WHERE p.status = 'ABERTO'
GROUP BY p.id, e.nome, pes.nome;

COMMENT ON VIEW public.vw_contas_abertas IS 'Listagem de todas as contas/pedidos atualmente abertos';

-- ============================================================================
-- GRANTS (ajustar conforme políticas de segurança do projeto)
-- ============================================================================
-- Exemplo assuming que existe um role 'app_segueme':
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_segueme;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_segueme;
-- GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO app_segueme;

-- ============================================================================
-- Dados de exemplo / seed (opcional - apenas para desenvolvimento)
-- ============================================================================
/*
-- Artigos de exemplo
INSERT INTO public.venda_artigo (codigo, nome, descricao, preco_base, ativo, categoria, estoque_atual, estoque_minimo) VALUES
('CAM-001', 'Camiseta Segue-me P', 'Camiseta 100% algodão tamanho P', 45.00, TRUE, 'Vestuário', 50, 10),
('CAM-002', 'Camiseta Segue-me M', 'Camiseta 100% algodão tamanho M', 45.00, TRUE, 'Vestuário', 100, 20),
('CAM-003', 'Camiseta Segue-me G', 'Camiseta 100% algodão tamanho G', 45.00, TRUE, 'Vestuário', 80, 15),
('LIV-001', 'Livro Segue-me', 'Livro com ensinamentos da jornada', 25.00, TRUE, 'Livro', 30, 5),
('TER-001', 'Terço Segue-me', 'Terço de madeira com cruz', 15.00, TRUE, 'Acessório', 100, 20),
('CAN-001', 'Caneca Segue-me', 'Caneca de porcelana 300ml', 30.00, TRUE, 'Utensílio', 40, 10);
*/
