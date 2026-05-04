-- =====================================================
-- Migração: Melhorias nas regras de mandato de dirigentes
-- Data: 2026-04-29
-- Descrição:
--   - Adiciona controle de status do mandato (ATIVO, PRORROGADO, RENUNCIADO, etc.)
--   - Adiciona motivo de encerramento antecipado
--   - Adiciona data de encerramento efetivo vs. data de fim planejada
--   - Adiciona controle de prorrogação (flag + data)
--   - Remove constraint UNIQUE(trabalhador_id, pasta_id) para permitir mandatos
--     diferentes do mesmo trabalhador na mesma pasta em períodos distintos
--   - Mandato inicial passa a ser de 1 ano (prorrogável por +1 ano)
-- =====================================================

-- 1. Adicionar novas colunas na tabela dirigente
ALTER TABLE dirigente ADD COLUMN IF NOT EXISTS status_mandato VARCHAR(30);
ALTER TABLE dirigente ADD COLUMN IF NOT EXISTS motivo_encerramento VARCHAR(30);
ALTER TABLE dirigente ADD COLUMN IF NOT EXISTS data_encerramento_efetivo DATE;
ALTER TABLE dirigente ADD COLUMN IF NOT EXISTS prorrogado BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE dirigente ADD COLUMN IF NOT EXISTS data_prorrogacao DATE;

-- 2. Migrar dados existentes: definir status com base nas datas
-- Mandatos com data_fim no passado => ENCERRADO_NATURAL
-- Mandatos com data_fim no futuro e ativo => ATIVO
UPDATE dirigente 
SET status_mandato = 'ENCERRADO_NATURAL',
    motivo_encerramento = 'TERMINO_NATURAL',
    data_encerramento_efetivo = data_fim
WHERE data_fim < CURRENT_DATE AND (status_mandato IS NULL);

UPDATE dirigente 
SET status_mandato = 'ATIVO'
WHERE data_fim >= CURRENT_DATE AND ativo = true AND (status_mandato IS NULL);

UPDATE dirigente 
SET status_mandato = 'ENCERRADO_NATURAL'
WHERE ativo = false AND (status_mandato IS NULL);

-- 3. Tornar status_mandato NOT NULL após migração
ALTER TABLE dirigente ALTER COLUMN status_mandato SET NOT NULL;

-- 4. Remover constraint UNIQUE antiga (trabalhador_id, pasta_id)
-- para permitir mandatos múltiplos do mesmo trabalhador na mesma pasta
-- O nome da constraint pode variar, verificar com:
-- SELECT conname FROM pg_constraint WHERE conrelid = 'dirigente'::regclass AND contype = 'u';
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'dirigente'::regclass
      AND contype = 'u'
      AND array_to_string(conkey, ',') IN (
          SELECT string_agg(attnum::text, ',' ORDER BY attnum)
          FROM pg_attribute
          WHERE attrelid = 'dirigente'::regclass
            AND attname IN ('trabalhador_id', 'pasta_id')
      );
    
    IF constraint_name IS NOT NULL THEN
        EXECUTE 'ALTER TABLE dirigente DROP CONSTRAINT ' || constraint_name;
        RAISE NOTICE 'Constraint % removida com sucesso.', constraint_name;
    ELSE
        RAISE NOTICE 'Nenhuma constraint UNIQUE(trabalhador_id, pasta_id) encontrada.';
    END IF;
END $$;

-- 5. Detectar mandatos que tinham duração de ~2 anos (>365 dias) e marcar como prorrogados
UPDATE dirigente 
SET prorrogado = true,
    data_prorrogacao = data_inicio + INTERVAL '365 days',
    status_mandato = CASE 
        WHEN status_mandato = 'ATIVO' THEN 'PRORROGADO'
        ELSE status_mandato 
    END
WHERE (data_fim - data_inicio) > 366
  AND prorrogado = false;

-- 6. Criar índices para consultas frequentes
CREATE INDEX IF NOT EXISTS idx_dirigente_status_mandato ON dirigente(status_mandato);
CREATE INDEX IF NOT EXISTS idx_dirigente_vigente ON dirigente(trabalhador_id, pasta_id, status_mandato) 
    WHERE status_mandato IN ('ATIVO', 'PRORROGADO');

-- =====================================================
-- RESUMO DAS MUDANÇAS DE REGRAS:
--
-- ANTES:
--   - Mandato fixo de 720-732 dias (2 anos)
--   - UNIQUE(trabalhador_id, pasta_id) impedia re-nomeação
--   - Sem controle de status ou motivo de encerramento
--   - Sem registro de prorrogação
--
-- DEPOIS:
--   - Mandato inicial de 1 ano (~365 dias)
--   - Prorrogável por +1 ano (máximo ~731 dias total)
--   - Status: ATIVO, PRORROGADO, ENCERRADO_NATURAL, 
--     RENUNCIADO, DESTITUIDO, ENCERRADO_OUTROS
--   - Motivo de encerramento: TERMINO_NATURAL, RENUNCIA,
--     DESTITUICAO, FALECIMENTO, MUDANCA_COMUNIDADE,
--     IMPEDIMENTO_SAUDE, OUTRO
--   - Data de encerramento efetivo (vs. data de fim planejada)
--   - Mesmo trabalhador pode ter múltiplos mandatos na mesma pasta
--     (em períodos diferentes)
-- =====================================================
