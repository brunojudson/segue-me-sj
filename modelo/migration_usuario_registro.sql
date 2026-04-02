-- ============================================================================
-- MIGRAÇÃO: Adicionar campo usuario_registro_id na tabela venda_pedido
-- Descrição: Registra qual usuário do sistema realizou/registrou a venda
-- Data: 02/04/2026
-- ============================================================================

-- Adicionar coluna
ALTER TABLE public.venda_pedido 
    ADD COLUMN IF NOT EXISTS usuario_registro_id BIGINT NULL;

-- Adicionar FK
ALTER TABLE public.venda_pedido 
    ADD CONSTRAINT fk_venda_pedido_usuario_registro 
    FOREIGN KEY (usuario_registro_id) 
    REFERENCES public.usuario(id) ON DELETE SET NULL;

-- Comentário
COMMENT ON COLUMN public.venda_pedido.usuario_registro_id IS 'Usuário do sistema que registrou/criou a venda';
