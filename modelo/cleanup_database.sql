-- Script de limpeza do banco de dados para o sistema "Segue-me"
-- Este script remove todos os dados das tabelas mantendo a estrutura do banco
-- Script de limpeza do banco de dados para o sistema "Segue-me"
-- Versão otimizada para PostgreSQL.
-- Executar como um superusuário ou um usuário com permissão para truncar tabelas e reiniciar sequences.

-- Observação: usamos RESTART IDENTITY CASCADE para truncar todas as tabelas listadas,
-- limpar dados dependentes e reiniciar automaticamente as sequences relacionadas.

-- Exemplo de uso (psql): \i cleanup_database.sql

-- TRUNCATE com restart automático das sequences e cascading para respeitar FK
TRUNCATE TABLE auditoria,
	casal,
	contribuicao,
	dirigente,
	encontrista,
	encontro,
	equipe,
	movimento_financeiro,
	palestra,
	palestra_palestrante,
	palestrante,
	palestrante_grupo_pessoa,
	pasta,
	permissao,
	pessoa,
	pessoa_sacramento,
	tipo_equipe,
	trabalhador,
	usuario,
	usuario_permissao,
	versiculo
RESTART IDENTITY CASCADE;

-- Se desejar reiniciar manualmente sequences específicas (opcional):
ALTER SEQUENCE auditoria_id_seq RESTART WITH 1;
ALTER SEQUENCE casal_id_seq RESTART WITH 1;
ALTER SEQUENCE contribuicao_id_seq RESTART WITH 1;
ALTER SEQUENCE dirigente_id_seq RESTART WITH 1;
ALTER SEQUENCE encontrista_id_seq RESTART WITH 1;
ALTER SEQUENCE encontro_id_seq RESTART WITH 1;
ALTER SEQUENCE equipe_id_seq RESTART WITH 1;
ALTER SEQUENCE movimento_financeiro_id_seq RESTART WITH 1;
ALTER SEQUENCE palestra_id_seq RESTART WITH 1;
ALTER SEQUENCE palestrante_id_seq RESTART WITH 1;
ALTER SEQUENCE pasta_id_seq RESTART WITH 1;
ALTER SEQUENCE permissao_id_seq RESTART WITH 1;
ALTER SEQUENCE pessoa_id_seq RESTART WITH 1;
ALTER SEQUENCE tipo_equipe_id_seq RESTART WITH 1;
ALTER SEQUENCE trabalhador_id_seq RESTART WITH 1;
ALTER SEQUENCE usuario_id_seq RESTART WITH 1;
ALTER SEQUENCE versiculo_id_seq RESTART WITH 1;
ALTER SEQUENCE usuario_id_seq RESTART WITH 1;


INSERT INTO public.usuario (nome,email,senha,ativo) VALUES 
('Ricardo José','user2','$2a$10$fW9ixG3FX3rHyCXhA4w81.e6d1KbOVm9XUD4U2xhT4RdOAnrcAl56',true)
,('Alice Maria','user3','$2a$10$wgXk7mRVOpRKcvEvgMsREeFFb/rQxVHlO9mrU9MOo1rq6cGGVAh92',true)
,('Bruno Judson de Almeida do Lago','admin','$2a$10$H/IxuX.aOHn4PB3BdS8Ly.3w4koKKNg7f066T5oB2QohUBht1jxGi',true)
,('Marina de Assis Almeida','user','$2a$10$hHhVRW9h.NPF0A8j1qrx4.CB1zywkP5vl1vyttjz2oJ9DXbyME5tm',true)
;


INSERT INTO public.usuario_permissao (usuario_id,permissao_id) VALUES 
(3,2)
,(1,3)
,(2,3)
,(3,3)
;

INSERT INTO public.permissao (nome) VALUES 
('USER')
,('ADMIN')
,('PROVER')
;
