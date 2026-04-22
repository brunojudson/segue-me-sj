-- ============================================================
-- Migration: Suporte a Casal Trabalhador no Encontro
-- Objetivo: Permitir que casais possam trabalhar no encontro,
--           seja como membros de equipe ou como casal coordenador.
-- ============================================================

-- 1. Tornar pessoa_id opcional (era NOT NULL implícito pela entidade)
ALTER TABLE public.trabalhador
    ALTER COLUMN pessoa_id DROP NOT NULL;

-- 2. Adicionar referência ao casal
ALTER TABLE public.trabalhador
    ADD COLUMN IF NOT EXISTS casal_id BIGINT NULL;

ALTER TABLE public.trabalhador
    ADD CONSTRAINT fk_trabalhador_casal
    FOREIGN KEY (casal_id) REFERENCES public.casal(id) ON DELETE SET NULL;

-- 3. Adicionar flag de casal coordenador
ALTER TABLE public.trabalhador
    ADD COLUMN IF NOT EXISTS eh_casal_coordenador BOOLEAN NOT NULL DEFAULT false;

-- 4. Unique constraint para casal + equipe + encontro
ALTER TABLE public.trabalhador
    ADD CONSTRAINT uq_trabalhador_casal_equipe_encontro
    UNIQUE (casal_id, equipe_id, encontro_id);

-- 5. Check constraint: deve ter pessoa OU casal (não ambos, não nenhum)
ALTER TABLE public.trabalhador
    ADD CONSTRAINT chk_trabalhador_pessoa_ou_casal
    CHECK (
        (pessoa_id IS NOT NULL AND casal_id IS NULL)
        OR
        (pessoa_id IS NULL AND casal_id IS NOT NULL)
    );

-- 6. Índice para buscas por casal
CREATE INDEX IF NOT EXISTS idx_trabalhador_casal_id
    ON public.trabalhador(casal_id);
