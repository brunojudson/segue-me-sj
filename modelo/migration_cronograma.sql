-- Migration: Tabela de Atividades do Cronograma do Encontro
-- Data: 2026-04-22

CREATE TABLE public.atividade_encontro (
    id bigserial NOT NULL,
    encontro_id int8 NOT NULL,
    titulo varchar(200) NOT NULL,
    tipo_atividade varchar(30) NOT NULL,
    data_hora_inicio timestamp NOT NULL,
    data_hora_fim timestamp NULL,
    responsavel varchar(100) NULL,
    local_atividade varchar(200) NULL,
    descricao varchar(500) NULL,
    ordem int4 NULL,
    CONSTRAINT atividade_encontro_pkey PRIMARY KEY (id),
    CONSTRAINT fk_atividade_encontro FOREIGN KEY (encontro_id) REFERENCES encontro(id)
);

CREATE INDEX idx_atividade_encontro_encontro ON public.atividade_encontro (encontro_id);
CREATE INDEX idx_atividade_encontro_inicio ON public.atividade_encontro (data_hora_inicio);

COMMENT ON TABLE public.atividade_encontro IS 'Cronograma de atividades de cada encontro';
COMMENT ON COLUMN public.atividade_encontro.tipo_atividade IS 'Enum: PALESTRA, DINAMICA, REFEICAO, ORACAO, SILENCIO, INTERVALO, LOUVOR, TESTEMUNHO, ENCERRAMENTO, OUTRO';
COMMENT ON COLUMN public.atividade_encontro.ordem IS 'Ordem manual para atividades com mesmo horário de início';

-- Vínculo opcional com palestra (quando tipoAtividade = PALESTRA)
ALTER TABLE public.atividade_encontro
    ADD COLUMN IF NOT EXISTS palestra_id bigint NULL
        REFERENCES public.palestra(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_atividade_encontro_palestra ON public.atividade_encontro (palestra_id);

COMMENT ON COLUMN public.atividade_encontro.palestra_id IS 'Vínculo opcional com Palestra cadastrada; preenchido quando tipo_atividade = PALESTRA';
