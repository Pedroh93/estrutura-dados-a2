-- Exemplos dos testes do enunciado: Caneta (Teste 1) e Computador (Teste 2)

DO $$
DECLARE
    pid INTEGER;
    r   RECORD;
BEGIN
    INSERT INTO produto (nome) VALUES ('Caneta') RETURNING id INTO pid;

    -- os pais vêm antes dos filhos (ordem da coluna ord)
    FOR r IN
        SELECT * FROM (VALUES
            (1, 'Caneta', NULL::text, 1, 2.50),
            (2, 'Tinta', 'Caneta', 1, 0.80),
            (3, 'Pigmento', 'Tinta', 1, 0.30),
            (4, 'Solvente', 'Tinta', 1, 0.50),
            (5, 'Corpo', 'Caneta', 1, 1.00),
            (6, 'Plástico', 'Corpo', 1, 0.70),
            (7, 'Clip', 'Corpo', 1, 0.30),
            (8, 'Tampa', 'Caneta', 1, 0.70),
            (9, 'Plástico', 'Tampa', 1, 0.50),
            (10, 'Vedação', 'Tampa', 1, 0.20)
        ) AS t (ord, nome, pai, qtd, valor)
        ORDER BY ord
    LOOP
        INSERT INTO item_formula (produto_id, pai_id, nome, quantidade, valor)
        VALUES (pid,
                (SELECT i.id FROM item_formula i WHERE i.produto_id = pid AND i.nome = r.pai),
                r.nome, r.qtd, r.valor);
    END LOOP;
END $$;

DO $$
DECLARE
    pid INTEGER;
    r   RECORD;
BEGIN
    INSERT INTO produto (nome) VALUES ('Computador') RETURNING id INTO pid;

    -- os pais vêm antes dos filhos (ordem da coluna ord)
    FOR r IN
        SELECT * FROM (VALUES
            (1, 'Computador', NULL::text, 1, 2500.00),
            (2, 'Gabinete', 'Computador', 1, 800.00),
            (3, 'Placa-Mãe', 'Gabinete', 1, 500.00),
            (4, 'CPU', 'Placa-Mãe', 1, 1000.00),
            (5, 'Memória RAM', 'Placa-Mãe', 2, 200.00),
            (6, 'Chipset', 'Placa-Mãe', 1, 150.00),
            (7, 'Fonte de Alimentação', 'Gabinete', 1, 300.00),
            (8, 'Armazenamento', 'Gabinete', 1, 0.00),
            (9, 'HDD', 'Armazenamento', 1, 250.00),
            (10, 'SSD', 'Armazenamento', 1, 400.00),
            (11, 'Monitor', 'Computador', 1, 900.00),
            (12, 'Tela LCD', 'Monitor', 1, 600.00),
            (13, 'Carcaça', 'Monitor', 1, 100.00),
            (14, 'Periféricos', 'Computador', 1, 0.00),
            (15, 'Teclado', 'Periféricos', 1, 100.00),
            (16, 'Mouse', 'Periféricos', 1, 80.00)
        ) AS t (ord, nome, pai, qtd, valor)
        ORDER BY ord
    LOOP
        INSERT INTO item_formula (produto_id, pai_id, nome, quantidade, valor)
        VALUES (pid,
                (SELECT i.id FROM item_formula i WHERE i.produto_id = pid AND i.nome = r.pai),
                r.nome, r.qtd, r.valor);
    END LOOP;
END $$;
