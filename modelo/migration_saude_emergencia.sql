-- Migration: Campos de Saúde e Emergência na tabela encontrista
-- Data: 2026-04-22

ALTER TABLE public.encontrista
    ADD COLUMN IF NOT EXISTS alergias varchar(500) NULL,
    ADD COLUMN IF NOT EXISTS restricoes_alimentares varchar(500) NULL,
    ADD COLUMN IF NOT EXISTS medicamentos varchar(500) NULL,
    ADD COLUMN IF NOT EXISTS condicao_medica varchar(500) NULL,
    ADD COLUMN IF NOT EXISTS contato_emergencia_nome varchar(100) NULL,
    ADD COLUMN IF NOT EXISTS contato_emergencia_telefone varchar(20) NULL,
    ADD COLUMN IF NOT EXISTS responsavel_nome varchar(100) NULL,
    ADD COLUMN IF NOT EXISTS responsavel_telefone varchar(20) NULL,
    ADD COLUMN IF NOT EXISTS autorizacao_responsavel bool NULL;

COMMENT ON COLUMN public.encontrista.alergias IS 'Alergias conhecidas (medicamentos, alimentos, materiais)';
COMMENT ON COLUMN public.encontrista.restricoes_alimentares IS 'Restrições alimentares (vegetariano, intolerante à lactose, etc.)';
COMMENT ON COLUMN public.encontrista.medicamentos IS 'Medicamentos de uso contínuo e horários';
COMMENT ON COLUMN public.encontrista.condicao_medica IS 'Condições médicas relevantes (epilepsia, diabetes, asma, etc.)';
COMMENT ON COLUMN public.encontrista.contato_emergencia_nome IS 'Nome do contato para emergências';
COMMENT ON COLUMN public.encontrista.contato_emergencia_telefone IS 'Telefone do contato para emergências';
COMMENT ON COLUMN public.encontrista.responsavel_nome IS 'Nome do responsável legal (para menores de 18 anos)';
COMMENT ON COLUMN public.encontrista.responsavel_telefone IS 'Telefone do responsável legal (para menores de 18 anos)';
COMMENT ON COLUMN public.encontrista.autorizacao_responsavel IS 'Indica se o responsável autorizou a participação no encontro';
