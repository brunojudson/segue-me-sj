-- Script de limpeza do banco de dados para o sistema "Segue-me"
-- Este script remove todos os dados das tabelas mantendo a estrutura do banco

-- Desabilitar verificações de chave estrangeira temporariamente
SET FOREIGN_KEY_CHECKS = 0;

-- Limpar tabelas em ordem para evitar problemas de integridade referencial
TRUNCATE TABLE contribuicao;
TRUNCATE TABLE dirigente;
TRUNCATE TABLE trabalhador;
TRUNCATE TABLE encontrista;
TRUNCATE TABLE casal;
TRUNCATE TABLE equipe;
TRUNCATE TABLE pasta;
TRUNCATE TABLE tipo_equipe;
TRUNCATE TABLE encontro;
TRUNCATE TABLE pessoa;

-- Reiniciar sequências de auto-incremento
ALTER TABLE contribuicao AUTO_INCREMENT = 1;
ALTER TABLE dirigente AUTO_INCREMENT = 1;
ALTER TABLE trabalhador AUTO_INCREMENT = 1;
ALTER TABLE encontrista AUTO_INCREMENT = 1;
ALTER TABLE casal AUTO_INCREMENT = 1;
ALTER TABLE equipe AUTO_INCREMENT = 1;
ALTER TABLE pasta AUTO_INCREMENT = 1;
ALTER TABLE tipo_equipe AUTO_INCREMENT = 1;
ALTER TABLE encontro AUTO_INCREMENT = 1;
ALTER TABLE pessoa AUTO_INCREMENT = 1;

-- Reabilitar verificações de chave estrangeira
SET FOREIGN_KEY_CHECKS = 1;

-- Versão alternativa para PostgreSQL
/*
-- Desabilitar triggers temporariamente
SET session_replication_role = 'replica';

-- Limpar tabelas
TRUNCATE TABLE contribuicao CASCADE;
TRUNCATE TABLE dirigente CASCADE;
TRUNCATE TABLE trabalhador CASCADE;
TRUNCATE TABLE encontrista CASCADE;
TRUNCATE TABLE casal CASCADE;
TRUNCATE TABLE equipe CASCADE;
TRUNCATE TABLE pasta CASCADE;
TRUNCATE TABLE tipo_equipe CASCADE;
TRUNCATE TABLE encontro CASCADE;
TRUNCATE TABLE pessoa CASCADE;

-- Reiniciar sequências
ALTER SEQUENCE contribuicao_id_seq RESTART WITH 1;
ALTER SEQUENCE dirigente_id_seq RESTART WITH 1;
ALTER SEQUENCE trabalhador_id_seq RESTART WITH 1;
ALTER SEQUENCE encontrista_id_seq RESTART WITH 1;
ALTER SEQUENCE casal_id_seq RESTART WITH 1;
ALTER SEQUENCE equipe_id_seq RESTART WITH 1;
ALTER SEQUENCE pasta_id_seq RESTART WITH 1;
ALTER SEQUENCE tipo_equipe_id_seq RESTART WITH 1;
ALTER SEQUENCE encontro_id_seq RESTART WITH 1;
ALTER SEQUENCE pessoa_id_seq RESTART WITH 1;

-- Reabilitar triggers
SET session_replication_role = 'origin';
*/
