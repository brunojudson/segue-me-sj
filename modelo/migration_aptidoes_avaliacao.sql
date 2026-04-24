-- ============================================================
-- Migração: Avaliações e Aptidões do Encontrista/Seguimista
-- Data: 2026-04-24
-- ============================================================

-- 1. Coluna de avaliação geral no encontrista
ALTER TABLE public.encontrista
    ADD COLUMN IF NOT EXISTS avaliacao_geral varchar(1000) NULL;

-- 2. Colunas de saúde/emergência (caso não existam ainda)
ALTER TABLE public.encontrista
    ADD COLUMN IF NOT EXISTS alergias varchar(500) NULL;
ALTER TABLE public.encontrista
    ADD COLUMN IF NOT EXISTS restricoes_alimentares varchar(500) NULL;
ALTER TABLE public.encontrista
    ADD COLUMN IF NOT EXISTS medicamentos varchar(500) NULL;
ALTER TABLE public.encontrista
    ADD COLUMN IF NOT EXISTS condicao_medica varchar(500) NULL;
ALTER TABLE public.encontrista
    ADD COLUMN IF NOT EXISTS contato_emergencia_nome varchar(100) NULL;
ALTER TABLE public.encontrista
    ADD COLUMN IF NOT EXISTS contato_emergencia_telefone varchar(20) NULL;
ALTER TABLE public.encontrista
    ADD COLUMN IF NOT EXISTS responsavel_nome varchar(100) NULL;
ALTER TABLE public.encontrista
    ADD COLUMN IF NOT EXISTS responsavel_telefone varchar(20) NULL;
ALTER TABLE public.encontrista
    ADD COLUMN IF NOT EXISTS autorizacao_responsavel bool NULL;

-- 3. Tabela de aptidões (relação N:N entre encontrista e equipe-tipo)
CREATE TABLE IF NOT EXISTS public.encontrista_aptidao (
    encontrista_id int8 NOT NULL,
    aptidao varchar(50) NOT NULL,
    CONSTRAINT encontrista_aptidao_pkey PRIMARY KEY (encontrista_id, aptidao),
    CONSTRAINT fk_encontrista_aptidao FOREIGN KEY (encontrista_id)
        REFERENCES public.encontrista(id) ON DELETE CASCADE
);
