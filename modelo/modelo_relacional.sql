-- Drop table

-- DROP TABLE public.auditoria

CREATE TABLE public.auditoria (
	id bigserial NOT NULL,
	acao varchar(255) NULL,
	datahora timestamp NULL,
	detalhes text NULL,
	entidade varchar(255) NULL,
	entidadeid int8 NULL,
	usuario varchar(255) NULL,
	data_hora timestamp NULL,
	entidade_id int8 NULL,
	CONSTRAINT auditoria_pkey PRIMARY KEY (id)
);

-- Drop table

-- DROP TABLE public.casal

CREATE TABLE public.casal (
	id bigserial NOT NULL,
	data_casamento date NULL,
	observacoes varchar(255) NULL,
	pessoa1_id int8 NOT NULL,
	pessoa2_id int8 NOT NULL,
	foto varchar(255) NULL,
	CONSTRAINT casal_pkey PRIMARY KEY (id),
	CONSTRAINT ukgq79718a8064ng0u84hxc5qav UNIQUE (pessoa1_id, pessoa2_id),
	CONSTRAINT fkbargtnoqbhtcfso6j10odlfgt FOREIGN KEY (pessoa2_id) REFERENCES pessoa(id),
	CONSTRAINT fkilg0fbidqc2p0d87y51go1apk FOREIGN KEY (pessoa1_id) REFERENCES pessoa(id)
);

-- Drop table

-- DROP TABLE public.contribuicao

CREATE TABLE public.contribuicao (
	id bigserial NOT NULL,
	comprovante_url varchar(255) NULL,
	data_pagamento timestamp NULL,
	forma_pagamento varchar(50) NOT NULL,
	observacoes varchar(255) NULL,
	valor numeric(10,2) NOT NULL,
	trabalhador_id int8 NOT NULL,
	CONSTRAINT contribuicao_pkey PRIMARY KEY (id),
	CONSTRAINT fkqa6nct4dfci9d1umxtfq0msef FOREIGN KEY (trabalhador_id) REFERENCES trabalhador(id)
);

-- Drop table

-- DROP TABLE public.dirigente

CREATE TABLE public.dirigente (
	id bigserial NOT NULL,
	ativo bool NOT NULL,
	data_fim date NOT NULL,
	data_inicio date NOT NULL,
	pasta_id int8 NOT NULL,
	trabalhador_id int8 NOT NULL,
	data_cadastro timestamp NULL,
	observacoes varchar(255) NULL,
	CONSTRAINT dirigente_pkey PRIMARY KEY (id),
	CONSTRAINT uk77yxs8j17nw7ax4kxk8apt52x UNIQUE (trabalhador_id, pasta_id),
	CONSTRAINT fk47up079r92kjyhi9qx4dkbj8d FOREIGN KEY (trabalhador_id) REFERENCES trabalhador(id),
	CONSTRAINT fkh5v1h17i5u4njnrum543gugnm FOREIGN KEY (pasta_id) REFERENCES pasta(id)
);

-- Drop table

-- DROP TABLE public.encontrista

CREATE TABLE public.encontrista (
	id bigserial NOT NULL,
	data_inscricao timestamp NULL,
	forma_pagamento varchar(50) NULL,
	observacoes varchar(255) NULL,
	valor_pago numeric(10,2) NULL,
	encontro_id int8 NOT NULL,
	pessoa_id int8 NOT NULL,
	trabalhador_id int8 NULL,
	ativo bool NULL,
	idade int4 NULL,
	circulo varchar(10) NULL,
	token_ficha varchar(36) NULL,
	CONSTRAINT encontrista_pkey PRIMARY KEY (id),
	CONSTRAINT uk8h7xvn19m3b8cl0khignn8oim UNIQUE (pessoa_id, encontro_id),
	CONSTRAINT uk_s1t42vchoeij0aafddggt32wi UNIQUE (token_ficha),
	CONSTRAINT fk3g7p2u91wldgfk5c0j1naky5a FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
	CONSTRAINT fk43olqjjv227wspf3ht11jr5ml FOREIGN KEY (encontro_id) REFERENCES encontro(id),
	CONSTRAINT fkivwsoitv4qmnwi7bkyh0fl9jh FOREIGN KEY (trabalhador_id) REFERENCES trabalhador(id)
);

-- Drop table

-- DROP TABLE public.encontro

CREATE TABLE public.encontro (
	id bigserial NOT NULL,
	ativo bool NULL,
	capacidade_maxima int4 NULL,
	data_fim date NOT NULL,
	data_inicio date NOT NULL,
	descricao varchar(255) NULL,
	"local" varchar(200) NOT NULL,
	nome varchar(100) NOT NULL,
	tema varchar(200) NULL,
	valor_contribuicao_trabalhador numeric(10,2) NULL,
	valor_inscricao numeric(10,2) NULL,
	CONSTRAINT encontro_pkey PRIMARY KEY (id)
);

-- Drop table

-- DROP TABLE public.equipe

CREATE TABLE public.equipe (
	id bigserial NOT NULL,
	ativo bool NULL,
	data_fim date NULL,
	data_inicio date NULL,
	descricao varchar(255) NULL,
	nome varchar(100) NOT NULL,
	encontro_id int8 NULL,
	tipo_equipe_id int8 NOT NULL,
	CONSTRAINT equipe_pkey PRIMARY KEY (id),
	CONSTRAINT fkonnntytlyav47wtl3lige3snt FOREIGN KEY (tipo_equipe_id) REFERENCES tipo_equipe(id),
	CONSTRAINT fkpg94v4bejwi9humsvxbi8hibs FOREIGN KEY (encontro_id) REFERENCES encontro(id)
);

-- Drop table

-- DROP TABLE public.movimento_financeiro

CREATE TABLE public.movimento_financeiro (
	id bigserial NOT NULL,
	categoria varchar(20) NOT NULL,
	comprovante_url varchar(255) NULL,
	data_criacao timestamp NULL,
	data_movimento date NOT NULL,
	descricao varchar(255) NOT NULL,
	observacoes varchar(500) NULL,
	responsavel varchar(100) NULL,
	tipo varchar(10) NOT NULL,
	valor numeric(10,2) NOT NULL,
	encontro_id int8 NOT NULL,
	CONSTRAINT movimento_financeiro_pkey PRIMARY KEY (id),
	CONSTRAINT fkkklpipeajhcips3t32qdvf6u1 FOREIGN KEY (encontro_id) REFERENCES encontro(id)
);

-- Drop table

-- DROP TABLE public.palestra

CREATE TABLE public.palestra (
	id bigserial NOT NULL,
	descricao varchar(500) NULL,
	tema varchar(255) NOT NULL,
	titulo varchar(200) NOT NULL,
	encontro_id int8 NOT NULL,
	data_hora timestamp NULL,
	CONSTRAINT palestra_pkey PRIMARY KEY (id),
	CONSTRAINT fkhm549es7260p1m9213xnlcqqr FOREIGN KEY (encontro_id) REFERENCES encontro(id)
);

-- Drop table

-- DROP TABLE public.palestra_palestrante

CREATE TABLE public.palestra_palestrante (
	palestra_id int8 NOT NULL,
	palestrante_id int8 NOT NULL,
	CONSTRAINT palestra_palestrante_pkey PRIMARY KEY (palestra_id, palestrante_id),
	CONSTRAINT fk713f5jegtqqip13lyu42dw0ot FOREIGN KEY (palestrante_id) REFERENCES palestrante(id),
	CONSTRAINT fkfagf0qbratlgu3e3pje833des FOREIGN KEY (palestra_id) REFERENCES palestra(id)
);

-- Drop table

-- DROP TABLE public.palestrante

CREATE TABLE public.palestrante (
	id bigserial NOT NULL,
	tipo_palestrante varchar(255) NOT NULL,
	casal_id int8 NULL,
	pessoa_id int8 NULL,
	CONSTRAINT palestrante_pkey PRIMARY KEY (id),
	CONSTRAINT fk2wcs4g1tjl2kmodh6p3f04u65 FOREIGN KEY (casal_id) REFERENCES casal(id),
	CONSTRAINT fkb7sduqwoyn76d8akh0micbwfp FOREIGN KEY (pessoa_id) REFERENCES pessoa(id)
);

-- Drop table

-- DROP TABLE public.palestrante_grupo_pessoa

CREATE TABLE public.palestrante_grupo_pessoa (
	palestrante_id int8 NOT NULL,
	pessoa_id int8 NOT NULL,
	CONSTRAINT palestrante_grupo_pessoa_pkey PRIMARY KEY (palestrante_id, pessoa_id),
	CONSTRAINT fkjcwqj6glmqnry43ay0hlhq6qd FOREIGN KEY (palestrante_id) REFERENCES palestrante(id),
	CONSTRAINT fkpbn6ogue0d9r8mayoxn1pb13w FOREIGN KEY (pessoa_id) REFERENCES pessoa(id)
);

-- Drop table

-- DROP TABLE public.pasta

CREATE TABLE public.pasta (
	id bigserial NOT NULL,
	ativo bool NOT NULL,
	data_fim date NOT NULL,
	data_inicio date NOT NULL,
	descricao varchar(255) NULL,
	nome varchar(100) NOT NULL,
	equipe_id int8 NOT NULL,
	CONSTRAINT pasta_pkey PRIMARY KEY (id),
	CONSTRAINT fkt1lv6djnfvtpv4isdi19g8yyw FOREIGN KEY (equipe_id) REFERENCES equipe(id)
);

-- Drop table

-- DROP TABLE public.permissao

CREATE TABLE public.permissao (
	id serial NOT NULL,
	nome varchar(50) NOT NULL,
	CONSTRAINT permissao_nome_key UNIQUE (nome),
	CONSTRAINT permissao_pkey PRIMARY KEY (id)
);

-- Drop table

-- DROP TABLE public.pessoa

CREATE TABLE public.pessoa (
	id bigserial NOT NULL,
	ativo bool NULL,
	cpf varchar(14) NULL,
	data_cadastro timestamp NULL,
	data_nascimento date NOT NULL,
	email varchar(100) NULL,
	endereco varchar(200) NULL,
	nome varchar(100) NOT NULL,
	sexo bpchar(1) NULL,
	telefone varchar(15) NOT NULL,
	foto varchar(255) NULL,
	idade int4 NULL,
	curso varchar(100) NULL,
	escolaridade varchar(50) NULL,
	filiacao_mae varchar(100) NULL,
	filiacao_pai varchar(100) NULL,
	igreja_frequenta varchar(100) NULL,
	instituicao_ensino varchar(100) NULL,
	movimento_participou varchar(200) NULL,
	naturalidade varchar(100) NULL,
	religiao varchar(100) NULL,
	CONSTRAINT pessoa_pkey PRIMARY KEY (id),
	CONSTRAINT pessoa_un_tel UNIQUE (telefone)
);

-- Drop table

-- DROP TABLE public.pessoa_sacramento

CREATE TABLE public.pessoa_sacramento (
	pessoa_id int8 NOT NULL,
	sacramento varchar(255) NULL,
	CONSTRAINT fkcbny1mgusyyk5e3x4cvnuxrbp FOREIGN KEY (pessoa_id) REFERENCES pessoa(id)
);

-- Drop table

-- DROP TABLE public.tipo_equipe

CREATE TABLE public.tipo_equipe (
	id bigserial NOT NULL,
	ativo bool NOT NULL,
	descricao varchar(255) NULL,
	eh_dirigente bool NULL,
	nome varchar(100) NOT NULL,
	CONSTRAINT tipo_equipe_pkey PRIMARY KEY (id)
);

-- Drop table

-- DROP TABLE public.trabalhador

CREATE TABLE public.trabalhador (
	id bigserial NOT NULL,
	data_fim date NULL,
	data_inicio date NOT NULL,
	eh_coordenador bool NULL,
	foi_encontrista bool NULL,
	observacoes varchar(255) NULL,
	encontro_id int8 NULL,
	equipe_id int8 NOT NULL,
	pessoa_id int8 NOT NULL,
	ativo bool NULL,
	encontrista_id int8 NULL,
	idade int4 NULL,
	CONSTRAINT trabalhador_pkey PRIMARY KEY (id),
	CONSTRAINT ukp1ojykc18frdxfib8kqbu5f53 UNIQUE (pessoa_id, equipe_id, encontro_id),
	CONSTRAINT fk3oddq8lah5dyfkndr1u2g1f8 FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
	CONSTRAINT fkivwsoitv4qmnwi7bkyh0fl9jh FOREIGN KEY (encontrista_id) REFERENCES encontrista(id) ON DELETE SET NULL,
	CONSTRAINT fkl6qcjk49w4afuiby1uthf3dgr FOREIGN KEY (encontro_id) REFERENCES encontro(id),
	CONSTRAINT fkmfkvt04rps47tl67vac285s1g FOREIGN KEY (equipe_id) REFERENCES equipe(id),
	CONSTRAINT fksmmng7c9ido0t8vmsjgakappo FOREIGN KEY (encontrista_id) REFERENCES encontrista(id)
);

-- Drop table

-- DROP TABLE public.usuario

CREATE TABLE public.usuario (
	id serial NOT NULL,
	nome varchar(100) NOT NULL,
	email varchar(100) NOT NULL,
	senha varchar(255) NOT NULL,
	ativo bool NULL DEFAULT true,
	CONSTRAINT usuario_email_key UNIQUE (email),
	CONSTRAINT usuario_pkey PRIMARY KEY (id)
);

-- Drop table

-- DROP TABLE public.usuario_permissao

CREATE TABLE public.usuario_permissao (
	usuario_id int4 NOT NULL,
	permissao_id int4 NOT NULL,
	CONSTRAINT usuario_permissao_pkey PRIMARY KEY (usuario_id, permissao_id),
	CONSTRAINT usuario_permissao_permissao_id_fkey FOREIGN KEY (permissao_id) REFERENCES permissao(id),
	CONSTRAINT usuario_permissao_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- Drop table

-- DROP TABLE public.versiculo

CREATE TABLE public.versiculo (
	id bigserial NOT NULL,
	texto varchar(500) NULL,
	referencia varchar(100) NULL,
	CONSTRAINT versiculo_pkey PRIMARY KEY (id)
);
