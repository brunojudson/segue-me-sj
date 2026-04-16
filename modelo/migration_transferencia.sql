-- =============================================================================
-- Migração: Suporte a transferência de seguidores entre paróquias
-- =============================================================================

-- 1. Adicionar campos de transferência na tabela pessoa
-- -----------------------------------------------------------------------------
ALTER TABLE public.pessoa
    ADD COLUMN IF NOT EXISTS status_transferencia VARCHAR(30) NOT NULL DEFAULT 'ATIVO_ORIGEM',
    ADD COLUMN IF NOT EXISTS paroquia_origem      VARCHAR(150);

-- Restrição para garantir apenas valores válidos do enum
ALTER TABLE public.pessoa
    ADD CONSTRAINT chk_pessoa_status_transferencia
    CHECK (status_transferencia IN (
        'ATIVO_ORIGEM',
        'TRANSFERIDO_SAIDA',
        'TRANSFERIDO_ENTRADA',
        'RETORNADO'
    ));

-- 2. Criar tabela de histórico de transferências
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.transferencia (
    id                  BIGSERIAL       NOT NULL,
    pessoa_id           BIGINT          NOT NULL,
    paroquia_origem     VARCHAR(150)    NOT NULL,
    paroquia_destino    VARCHAR(150)    NOT NULL,
    tipo_transferencia  VARCHAR(30)     NOT NULL,
    data_solicitacao    DATE,
    data_efetivacao     DATE,
    observacoes         VARCHAR(500),
    registrado_por      VARCHAR(100),
    data_cadastro       TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT transferencia_pkey PRIMARY KEY (id),
    CONSTRAINT fk_transferencia_pessoa FOREIGN KEY (pessoa_id)
        REFERENCES public.pessoa(id) ON DELETE CASCADE,
    CONSTRAINT chk_transferencia_tipo CHECK (tipo_transferencia IN (
        'ATIVO_ORIGEM',
        'TRANSFERIDO_SAIDA',
        'TRANSFERIDO_ENTRADA',
        'RETORNADO'
    ))
);

-- Índice para busca rápida por pessoa
CREATE INDEX IF NOT EXISTS idx_transferencia_pessoa_id
    ON public.transferencia (pessoa_id);

-- Índice para ordenação por data de solicitação
CREATE INDEX IF NOT EXISTS idx_transferencia_data_solicitacao
    ON public.transferencia (data_solicitacao DESC);
