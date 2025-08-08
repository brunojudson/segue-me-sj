-- Queries de exemplo para relatórios do sistema "Segue-me"

-- 1. Relatório de Encontristas por Encontro
-- Esta query lista todos os encontristas de um determinado encontro, com seus dados pessoais
SELECT 
    e.id AS encontrista_id,
    p.nome,
    p.email,
    p.telefone,
    p.data_nascimento,
    e.valor_pago,
    e.forma_pagamento,
    e.data_inscricao
FROM 
    encontrista e
JOIN 
    pessoa p ON e.pessoa_id = p.id
JOIN 
    encontro enc ON e.encontro_id = enc.id
WHERE 
    enc.id = :encontro_id
ORDER BY 
    p.nome;

-- 2. Relatório de Trabalhadores por Equipe
-- Esta query lista todos os trabalhadores de uma determinada equipe, indicando quem é coordenador
SELECT 
    t.id AS trabalhador_id,
    p.nome,
    p.email,
    p.telefone,
    CASE WHEN t.coordenador = true THEN 'Sim' ELSE 'Não' END AS coordenador,
    t.data_inicio,
    CASE WHEN t.foi_encontrista = true THEN 'Sim' ELSE 'Não' END AS foi_encontrista
FROM 
    trabalhador t
JOIN 
    pessoa p ON t.pessoa_id = p.id
JOIN 
    equipe eq ON t.equipe_id = eq.id
WHERE 
    eq.id = :equipe_id
ORDER BY 
    t.coordenador DESC, p.nome;

-- 3. Relatório de Contribuições por Encontro
-- Esta query mostra o total de contribuições recebidas por encontro
SELECT 
    enc.nome AS encontro,
    enc.data_inicio,
    COUNT(c.id) AS total_contribuicoes,
    SUM(c.valor) AS valor_total,
    AVG(c.valor) AS valor_medio
FROM 
    contribuicao c
JOIN 
    trabalhador t ON c.trabalhador_id = t.id
JOIN 
    encontro enc ON t.encontro_id = enc.id
GROUP BY 
    enc.id, enc.nome, enc.data_inicio
ORDER BY 
    enc.data_inicio DESC;

-- 4. Relatório de Casais Trabalhadores
-- Esta query lista todos os casais que trabalham em um determinado encontro
SELECT 
    c.id AS casal_id,
    p_esposo.nome AS esposo,
    p_esposa.nome AS esposa,
    c.data_casamento,
    eq_esposo.nome AS equipe_esposo,
    eq_esposa.nome AS equipe_esposa
FROM 
    casal c
JOIN 
    pessoa p_esposo ON c.esposo_id = p_esposo.id
JOIN 
    pessoa p_esposa ON c.esposa_id = p_esposa.id
JOIN 
    trabalhador t_esposo ON t_esposo.pessoa_id = p_esposo.id
JOIN 
    trabalhador t_esposa ON t_esposa.pessoa_id = p_esposa.id
JOIN 
    equipe eq_esposo ON t_esposo.equipe_id = eq_esposo.id
JOIN 
    equipe eq_esposa ON t_esposa.equipe_id = eq_esposa.id
WHERE 
    t_esposo.encontro_id = :encontro_id
    AND t_esposa.encontro_id = :encontro_id
ORDER BY 
    c.data_casamento;

-- 5. Relatório de Encontristas que se tornaram Trabalhadores
-- Esta query mostra os encontristas de um encontro anterior que se tornaram trabalhadores no encontro atual
SELECT 
    p.nome,
    enc_anterior.nome AS encontro_anterior,
    enc_atual.nome AS encontro_atual,
    eq.nome AS equipe_atual
FROM 
    encontrista e
JOIN 
    pessoa p ON e.pessoa_id = p.id
JOIN 
    encontro enc_anterior ON e.encontro_id = enc_anterior.id
JOIN 
    trabalhador t ON t.pessoa_id = p.id
JOIN 
    encontro enc_atual ON t.encontro_id = enc_atual.id
JOIN 
    equipe eq ON t.equipe_id = eq.id
WHERE 
    enc_anterior.id = :encontro_anterior_id
    AND enc_atual.id = :encontro_atual_id
ORDER BY 
    p.nome;

-- 6. Relatório de Dirigentes por Pasta
-- Esta query lista todos os dirigentes de cada pasta, com período de mandato
SELECT 
    pasta.nome AS pasta,
    p.nome AS dirigente,
    d.data_inicio,
    d.data_fim,
    (d.data_fim - d.data_inicio) AS dias_mandato,
    CASE WHEN d.ativo = true THEN 'Sim' ELSE 'Não' END AS ativo
FROM 
    dirigente d
JOIN 
    trabalhador t ON d.trabalhador_id = t.id
JOIN 
    pessoa p ON t.pessoa_id = p.id
JOIN 
    pasta ON d.pasta_id = pasta.id
ORDER BY 
    pasta.nome, d.data_inicio DESC;

-- 7. Relatório de Distribuição de Trabalhadores por Equipe
-- Esta query mostra quantos trabalhadores existem em cada equipe de um determinado encontro
SELECT 
    eq.nome AS equipe,
    tipo.nome AS tipo_equipe,
    COUNT(t.id) AS total_trabalhadores,
    SUM(CASE WHEN t.coordenador = true THEN 1 ELSE 0 END) AS total_coordenadores,
    SUM(CASE WHEN t.foi_encontrista = true THEN 1 ELSE 0 END) AS total_ex_encontristas
FROM 
    trabalhador t
JOIN 
    equipe eq ON t.equipe_id = eq.id
JOIN 
    tipo_equipe tipo ON eq.tipo_equipe_id = tipo.id
WHERE 
    t.encontro_id = :encontro_id
GROUP BY 
    eq.id, eq.nome, tipo.nome
ORDER BY 
    tipo.nome, eq.nome;

-- 8. Relatório Financeiro de Contribuições
-- Esta query mostra um resumo financeiro das contribuições por forma de pagamento
SELECT 
    c.forma_pagamento,
    COUNT(c.id) AS quantidade,
    SUM(c.valor) AS valor_total,
    MIN(c.valor) AS menor_valor,
    MAX(c.valor) AS maior_valor,
    AVG(c.valor) AS valor_medio
FROM 
    contribuicao c
JOIN 
    trabalhador t ON c.trabalhador_id = t.id
WHERE 
    t.encontro_id = :encontro_id
GROUP BY 
    c.forma_pagamento
ORDER BY 
    valor_total DESC;

-- 9. Relatório de Aniversariantes do Mês
-- Esta query lista todas as pessoas (encontristas e trabalhadores) que fazem aniversário em um determinado mês
SELECT 
    p.nome,
    p.data_nascimento,
    EXTRACT(DAY FROM p.data_nascimento) AS dia_aniversario,
    CASE 
        WHEN e.id IS NOT NULL THEN 'Encontrista' 
        WHEN t.id IS NOT NULL THEN 'Trabalhador'
        ELSE 'Outro'
    END AS tipo,
    COALESCE(eq.nome, 'N/A') AS equipe
FROM 
    pessoa p
LEFT JOIN 
    encontrista e ON e.pessoa_id = p.id AND e.encontro_id = :encontro_id
LEFT JOIN 
    trabalhador t ON t.pessoa_id = p.id AND t.encontro_id = :encontro_id
LEFT JOIN 
    equipe eq ON t.equipe_id = eq.id
WHERE 
    EXTRACT(MONTH FROM p.data_nascimento) = :mes
ORDER BY 
    EXTRACT(DAY FROM p.data_nascimento);

-- 10. Relatório de Histórico de Participação
-- Esta query mostra o histórico de participação de uma pessoa em todos os encontros
SELECT 
    p.nome,
    enc.nome AS encontro,
    enc.data_inicio,
    CASE 
        WHEN e.id IS NOT NULL THEN 'Encontrista' 
        WHEN t.id IS NOT NULL THEN 'Trabalhador'
        ELSE 'Não participou'
    END AS tipo_participacao,
    COALESCE(eq.nome, 'N/A') AS equipe,
    CASE WHEN t.coordenador = true THEN 'Sim' ELSE 'Não' END AS coordenador
FROM 
    pessoa p
CROSS JOIN 
    encontro enc
LEFT JOIN 
    encontrista e ON e.pessoa_id = p.id AND e.encontro_id = enc.id
LEFT JOIN 
    trabalhador t ON t.pessoa_id = p.id AND t.encontro_id = enc.id
LEFT JOIN 
    equipe eq ON t.equipe_id = eq.id
WHERE 
    p.id = :pessoa_id
ORDER BY 
    enc.data_inicio DESC;
