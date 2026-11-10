-- Dados de demonstração para a dashboard.
--
-- Apaga todas as movimentações e lotes e recria um histórico de cerca de 10 meses,
-- com datas relativas ao dia em que o script é executado. Produtos e usuários
-- existentes são mantidos; produtos de exemplo que ainda não existem são criados.
--
-- Uso (dentro de projeto-ong-amar-backend):
--   set -a; source .env; set +a
--   MYSQL_PWD="$DB_PASSWORD" mysql -u "$DB_USER" -h 127.0.0.1 projeto_amar < docs/seed-dashboard.sql

SET NAMES utf8mb4;

START TRANSACTION;

DELETE FROM movimentacao_estoque;
DELETE FROM movimentacao;
DELETE FROM produto_lote;

INSERT INTO produto (nome, categoria, unidade_medida, status)
SELECT novo.nome, novo.categoria, novo.unidade_medida, 'ATIVO' FROM (
  SELECT 'Arroz' AS nome, 'ALIMENTOS' AS categoria, 'kg' AS unidade_medida
  UNION ALL SELECT 'Pasta de dente', 'PRODUTOS DE HIGIENE', 'un'
  UNION ALL SELECT 'Bolacha', 'ALIMENTOS', 'pacote'
  UNION ALL SELECT 'Feijão', 'ALIMENTOS', 'kg'
  UNION ALL SELECT 'Óleo de soja', 'ALIMENTOS', 'L'
  UNION ALL SELECT 'Macarrão', 'ALIMENTOS', 'pacote'
  UNION ALL SELECT 'Leite', 'ALIMENTOS', 'L'
  UNION ALL SELECT 'Sabonete', 'PRODUTOS DE HIGIENE', 'un'
  UNION ALL SELECT 'Papel higiênico', 'PRODUTOS DE HIGIENE', 'rolo'
  UNION ALL SELECT 'Shampoo', 'PRODUTOS DE HIGIENE', 'un'
) AS novo
WHERE NOT EXISTS (SELECT 1 FROM produto p WHERE p.nome = novo.nome);

SET @arroz = (SELECT id FROM produto WHERE nome = 'Arroz' ORDER BY id LIMIT 1);
SET @pasta = (SELECT id FROM produto WHERE nome = 'Pasta de dente' ORDER BY id LIMIT 1);
SET @bolacha = (SELECT id FROM produto WHERE nome = 'Bolacha' ORDER BY id LIMIT 1);
SET @feijao = (SELECT id FROM produto WHERE nome = 'Feijão' ORDER BY id LIMIT 1);
SET @oleo = (SELECT id FROM produto WHERE nome = 'Óleo de soja' ORDER BY id LIMIT 1);
SET @macarrao = (SELECT id FROM produto WHERE nome = 'Macarrão' ORDER BY id LIMIT 1);
SET @leite = (SELECT id FROM produto WHERE nome = 'Leite' ORDER BY id LIMIT 1);
SET @sabonete = (SELECT id FROM produto WHERE nome = 'Sabonete' ORDER BY id LIMIT 1);
SET @papel = (SELECT id FROM produto WHERE nome = 'Papel higiênico' ORDER BY id LIMIT 1);
SET @shampoo = (SELECT id FROM produto WHERE nome = 'Shampoo' ORDER BY id LIMIT 1);

SET @usuario_admin = (SELECT MIN(id) FROM usuario);
SET @usuario_voluntario = COALESCE((SELECT MAX(id) FROM usuario), @usuario_admin);

SET @lote_base = (SELECT COALESCE(MAX(id), 0) FROM produto_lote);
SET @mov_base = (SELECT COALESCE(MAX(id), 0) FROM movimentacao);

INSERT INTO produto_lote (id, quantidade, validade, data_entrada, fk_produto) VALUES
  (@lote_base + 1, 0, DATE_ADD(CURDATE(), INTERVAL 120 DAY), DATE_SUB(CURDATE(), INTERVAL 300 DAY), @arroz),
  (@lote_base + 2, 0, DATE_ADD(CURDATE(), INTERVAL 150 DAY), DATE_SUB(CURDATE(), INTERVAL 300 DAY), @feijao),
  (@lote_base + 3, 0, DATE_ADD(CURDATE(), INTERVAL 400 DAY), DATE_SUB(CURDATE(), INTERVAL 290 DAY), @sabonete),
  (@lote_base + 4, 0, DATE_ADD(CURDATE(), INTERVAL 200 DAY), DATE_SUB(CURDATE(), INTERVAL 270 DAY), @macarrao),
  (@lote_base + 5, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 260 DAY), @papel),
  (@lote_base + 6, 15, DATE_ADD(CURDATE(), INTERVAL 180 DAY), DATE_SUB(CURDATE(), INTERVAL 250 DAY), @oleo),
  (@lote_base + 7, 0, DATE_ADD(CURDATE(), INTERVAL 160 DAY), DATE_SUB(CURDATE(), INTERVAL 240 DAY), @arroz),
  (@lote_base + 8, 1, DATE_ADD(CURDATE(), INTERVAL 360 DAY), DATE_SUB(CURDATE(), INTERVAL 220 DAY), @pasta),
  (@lote_base + 9, 0, DATE_SUB(CURDATE(), INTERVAL 150 DAY), DATE_SUB(CURDATE(), INTERVAL 210 DAY), @leite),
  (@lote_base + 10, 8, DATE_ADD(CURDATE(), INTERVAL 300 DAY), DATE_SUB(CURDATE(), INTERVAL 200 DAY), @shampoo),
  (@lote_base + 11, 0, DATE_ADD(CURDATE(), INTERVAL 120 DAY), DATE_SUB(CURDATE(), INTERVAL 180 DAY), @feijao),
  (@lote_base + 12, 20, DATE_ADD(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 170 DAY), @bolacha),
  (@lote_base + 13, 0, DATE_ADD(CURDATE(), INTERVAL 200 DAY), DATE_SUB(CURDATE(), INTERVAL 150 DAY), @arroz),
  (@lote_base + 14, 15, DATE_ADD(CURDATE(), INTERVAL 420 DAY), DATE_SUB(CURDATE(), INTERVAL 140 DAY), @sabonete),
  (@lote_base + 15, 20, DATE_ADD(CURDATE(), INTERVAL 210 DAY), DATE_SUB(CURDATE(), INTERVAL 120 DAY), @macarrao),
  (@lote_base + 16, 10, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_SUB(CURDATE(), INTERVAL 110 DAY), @leite),
  (@lote_base + 17, 0, DATE_ADD(CURDATE(), INTERVAL 6 DAY), DATE_SUB(CURDATE(), INTERVAL 100 DAY), @oleo),
  (@lote_base + 18, 38, NULL, DATE_SUB(CURDATE(), INTERVAL 90 DAY), @papel),
  (@lote_base + 19, 0, DATE_ADD(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 80 DAY), @feijao),
  (@lote_base + 20, 5, DATE_ADD(CURDATE(), INTERVAL 4 DAY), DATE_SUB(CURDATE(), INTERVAL 70 DAY), @bolacha),
  (@lote_base + 21, 15, DATE_ADD(CURDATE(), INTERVAL 250 DAY), DATE_SUB(CURDATE(), INTERVAL 60 DAY), @arroz),
  (@lote_base + 22, 30, DATE_ADD(CURDATE(), INTERVAL 380 DAY), DATE_SUB(CURDATE(), INTERVAL 55 DAY), @pasta),
  (@lote_base + 23, 40, DATE_ADD(CURDATE(), INTERVAL 12 DAY), DATE_SUB(CURDATE(), INTERVAL 45 DAY), @leite),
  (@lote_base + 24, 0, DATE_ADD(CURDATE(), INTERVAL 18 DAY), DATE_SUB(CURDATE(), INTERVAL 40 DAY), @macarrao),
  (@lote_base + 25, 25, DATE_ADD(CURDATE(), INTERVAL 450 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY), @sabonete),
  (@lote_base + 26, 30, DATE_ADD(CURDATE(), INTERVAL 280 DAY), DATE_SUB(CURDATE(), INTERVAL 25 DAY), @feijao),
  (@lote_base + 27, 45, DATE_ADD(CURDATE(), INTERVAL 300 DAY), DATE_SUB(CURDATE(), INTERVAL 20 DAY), @arroz),
  (@lote_base + 28, 30, DATE_ADD(CURDATE(), INTERVAL 45 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY), @bolacha),
  (@lote_base + 29, 20, DATE_ADD(CURDATE(), INTERVAL 220 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY), @oleo),
  (@lote_base + 30, 30, DATE_ADD(CURDATE(), INTERVAL 28 DAY), DATE_SUB(CURDATE(), INTERVAL 6 DAY), @leite),
  (@lote_base + 31, 24, NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY), @papel),
  (@lote_base + 32, 30, DATE_ADD(CURDATE(), INTERVAL 320 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), @arroz),
  (@lote_base + 33, 20, DATE_ADD(CURDATE(), INTERVAL 400 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), @pasta);

INSERT INTO movimentacao (id, tipo, quantidade, data, motivo, fk_produto, fk_usuario) VALUES
  (@mov_base + 1, 'ENTRADA', 60, DATE_SUB(CURDATE(), INTERVAL 300 DAY), 'Entrada registrada no sistema', @arroz, @usuario_admin),
  (@mov_base + 2, 'ENTRADA', 50, DATE_SUB(CURDATE(), INTERVAL 300 DAY), 'Entrada registrada no sistema', @feijao, @usuario_voluntario),
  (@mov_base + 3, 'ENTRADA', 40, DATE_SUB(CURDATE(), INTERVAL 290 DAY), 'Entrada registrada no sistema', @sabonete, @usuario_admin),
  (@mov_base + 4, 'SAIDA', 30, DATE_SUB(CURDATE(), INTERVAL 280 DAY), 'Doação de cestas básicas', @arroz, @usuario_voluntario),
  (@mov_base + 5, 'SAIDA', 25, DATE_SUB(CURDATE(), INTERVAL 280 DAY), 'Doação de cestas básicas', @feijao, @usuario_admin),
  (@mov_base + 6, 'ENTRADA', 45, DATE_SUB(CURDATE(), INTERVAL 270 DAY), 'Entrada registrada no sistema', @macarrao, @usuario_voluntario),
  (@mov_base + 7, 'ENTRADA', 48, DATE_SUB(CURDATE(), INTERVAL 260 DAY), 'Entrada registrada no sistema', @papel, @usuario_admin),
  (@mov_base + 8, 'ENTRADA', 30, DATE_SUB(CURDATE(), INTERVAL 250 DAY), 'Entrada registrada no sistema', @oleo, @usuario_voluntario),
  (@mov_base + 9, 'SAIDA', 20, DATE_SUB(CURDATE(), INTERVAL 250 DAY), 'Doação de cestas básicas', @macarrao, @usuario_admin),
  (@mov_base + 10, 'SAIDA', 15, DATE_SUB(CURDATE(), INTERVAL 245 DAY), 'Doação de cestas básicas', @sabonete, @usuario_voluntario),
  (@mov_base + 11, 'ENTRADA', 50, DATE_SUB(CURDATE(), INTERVAL 240 DAY), 'Entrada registrada no sistema', @arroz, @usuario_admin),
  (@mov_base + 12, 'ENTRADA', 36, DATE_SUB(CURDATE(), INTERVAL 220 DAY), 'Entrada registrada no sistema', @pasta, @usuario_voluntario),
  (@mov_base + 13, 'SAIDA', 40, DATE_SUB(CURDATE(), INTERVAL 220 DAY), 'Doação de cestas básicas', @arroz, @usuario_admin),
  (@mov_base + 14, 'SAIDA', 15, DATE_SUB(CURDATE(), INTERVAL 220 DAY), 'Doação de cestas básicas', @oleo, @usuario_voluntario),
  (@mov_base + 15, 'SAIDA', 20, DATE_SUB(CURDATE(), INTERVAL 215 DAY), 'Doação de cestas básicas', @papel, @usuario_admin),
  (@mov_base + 16, 'ENTRADA', 40, DATE_SUB(CURDATE(), INTERVAL 210 DAY), 'Entrada registrada no sistema', @leite, @usuario_voluntario),
  (@mov_base + 17, 'ENTRADA', 8, DATE_SUB(CURDATE(), INTERVAL 200 DAY), 'Entrada registrada no sistema', @shampoo, @usuario_admin),
  (@mov_base + 18, 'SAIDA', 20, DATE_SUB(CURDATE(), INTERVAL 190 DAY), 'Doação de cestas básicas', @leite, @usuario_voluntario),
  (@mov_base + 19, 'ENTRADA', 40, DATE_SUB(CURDATE(), INTERVAL 180 DAY), 'Entrada registrada no sistema', @feijao, @usuario_admin),
  (@mov_base + 20, 'SAIDA', 30, DATE_SUB(CURDATE(), INTERVAL 175 DAY), 'Doação de cestas básicas', @feijao, @usuario_voluntario),
  (@mov_base + 21, 'ENTRADA', 50, DATE_SUB(CURDATE(), INTERVAL 170 DAY), 'Entrada registrada no sistema', @bolacha, @usuario_admin),
  (@mov_base + 22, 'SAIDA', 30, DATE_SUB(CURDATE(), INTERVAL 160 DAY), 'Doação de cestas básicas', @arroz, @usuario_voluntario),
  (@mov_base + 23, 'SAIDA', 20, DATE_SUB(CURDATE(), INTERVAL 160 DAY), 'Doação de cestas básicas', @macarrao, @usuario_admin),
  (@mov_base + 24, 'SAIDA', 15, DATE_SUB(CURDATE(), INTERVAL 155 DAY), 'Doação de cestas básicas', @pasta, @usuario_voluntario),
  (@mov_base + 25, 'ENTRADA', 70, DATE_SUB(CURDATE(), INTERVAL 150 DAY), 'Entrada registrada no sistema', @arroz, @usuario_admin),
  (@mov_base + 26, 'ENTRADA', 30, DATE_SUB(CURDATE(), INTERVAL 140 DAY), 'Entrada registrada no sistema', @sabonete, @usuario_voluntario),
  (@mov_base + 27, 'SAIDA', 30, DATE_SUB(CURDATE(), INTERVAL 130 DAY), 'Doação de cestas básicas', @bolacha, @usuario_admin),
  (@mov_base + 28, 'SAIDA', 25, DATE_SUB(CURDATE(), INTERVAL 130 DAY), 'Doação de cestas básicas', @feijao, @usuario_voluntario),
  (@mov_base + 29, 'SAIDA', 10, DATE_SUB(CURDATE(), INTERVAL 125 DAY), 'Perda: Embalagem danificada', @leite, @usuario_admin),
  (@mov_base + 30, 'ENTRADA', 40, DATE_SUB(CURDATE(), INTERVAL 120 DAY), 'Entrada registrada no sistema', @macarrao, @usuario_voluntario),
  (@mov_base + 31, 'ENTRADA', 45, DATE_SUB(CURDATE(), INTERVAL 110 DAY), 'Entrada registrada no sistema', @leite, @usuario_admin),
  (@mov_base + 32, 'ENTRADA', 25, DATE_SUB(CURDATE(), INTERVAL 100 DAY), 'Entrada registrada no sistema', @oleo, @usuario_voluntario),
  (@mov_base + 33, 'SAIDA', 45, DATE_SUB(CURDATE(), INTERVAL 100 DAY), 'Doação de cestas básicas', @arroz, @usuario_admin),
  (@mov_base + 34, 'SAIDA', 20, DATE_SUB(CURDATE(), INTERVAL 95 DAY), 'Doação de cestas básicas', @sabonete, @usuario_voluntario),
  (@mov_base + 35, 'ENTRADA', 40, DATE_SUB(CURDATE(), INTERVAL 90 DAY), 'Entrada registrada no sistema', @papel, @usuario_admin),
  (@mov_base + 36, 'SAIDA', 25, DATE_SUB(CURDATE(), INTERVAL 90 DAY), 'Doação de cestas básicas', @macarrao, @usuario_voluntario),
  (@mov_base + 37, 'SAIDA', 15, DATE_SUB(CURDATE(), INTERVAL 85 DAY), 'Doação de cestas básicas', @oleo, @usuario_admin),
  (@mov_base + 38, 'ENTRADA', 35, DATE_SUB(CURDATE(), INTERVAL 80 DAY), 'Entrada registrada no sistema', @feijao, @usuario_voluntario),
  (@mov_base + 39, 'SAIDA', 30, DATE_SUB(CURDATE(), INTERVAL 75 DAY), 'Doação de cestas básicas', @papel, @usuario_admin),
  (@mov_base + 40, 'ENTRADA', 40, DATE_SUB(CURDATE(), INTERVAL 70 DAY), 'Entrada registrada no sistema', @bolacha, @usuario_voluntario),
  (@mov_base + 41, 'SAIDA', 20, DATE_SUB(CURDATE(), INTERVAL 65 DAY), 'Doação de cestas básicas', @feijao, @usuario_admin),
  (@mov_base + 42, 'ENTRADA', 55, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'Entrada registrada no sistema', @arroz, @usuario_voluntario),
  (@mov_base + 43, 'ENTRADA', 30, DATE_SUB(CURDATE(), INTERVAL 55 DAY), 'Entrada registrada no sistema', @pasta, @usuario_admin),
  (@mov_base + 44, 'SAIDA', 40, DATE_SUB(CURDATE(), INTERVAL 50 DAY), 'Doação de cestas básicas', @arroz, @usuario_voluntario),
  (@mov_base + 45, 'SAIDA', 25, DATE_SUB(CURDATE(), INTERVAL 50 DAY), 'Doação de cestas básicas', @leite, @usuario_admin),
  (@mov_base + 46, 'ENTRADA', 40, DATE_SUB(CURDATE(), INTERVAL 45 DAY), 'Entrada registrada no sistema', @leite, @usuario_voluntario),
  (@mov_base + 47, 'ENTRADA', 35, DATE_SUB(CURDATE(), INTERVAL 40 DAY), 'Entrada registrada no sistema', @macarrao, @usuario_admin),
  (@mov_base + 48, 'SAIDA', 25, DATE_SUB(CURDATE(), INTERVAL 35 DAY), 'Doação de cestas básicas', @bolacha, @usuario_voluntario),
  (@mov_base + 49, 'SAIDA', 20, DATE_SUB(CURDATE(), INTERVAL 35 DAY), 'Doação de cestas básicas', @macarrao, @usuario_admin),
  (@mov_base + 50, 'ENTRADA', 25, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'Entrada registrada no sistema', @sabonete, @usuario_voluntario),
  (@mov_base + 51, 'SAIDA', 20, DATE_SUB(CURDATE(), INTERVAL 28 DAY), 'Doação de cestas básicas', @pasta, @usuario_admin),
  (@mov_base + 52, 'ENTRADA', 40, DATE_SUB(CURDATE(), INTERVAL 25 DAY), 'Entrada registrada no sistema', @feijao, @usuario_voluntario),
  (@mov_base + 53, 'SAIDA', 25, DATE_SUB(CURDATE(), INTERVAL 22 DAY), 'Doação de cestas básicas', @feijao, @usuario_admin),
  (@mov_base + 54, 'ENTRADA', 45, DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'Entrada registrada no sistema', @arroz, @usuario_voluntario),
  (@mov_base + 55, 'SAIDA', 20, DATE_SUB(CURDATE(), INTERVAL 18 DAY), 'Doação de cestas básicas', @leite, @usuario_admin),
  (@mov_base + 56, 'ENTRADA', 30, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 'Entrada registrada no sistema', @bolacha, @usuario_voluntario),
  (@mov_base + 57, 'SAIDA', 35, DATE_SUB(CURDATE(), INTERVAL 12 DAY), 'Doação de cestas básicas', @arroz, @usuario_admin),
  (@mov_base + 58, 'SAIDA', 20, DATE_SUB(CURDATE(), INTERVAL 12 DAY), 'Doação de cestas básicas', @sabonete, @usuario_voluntario),
  (@mov_base + 59, 'ENTRADA', 20, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'Entrada registrada no sistema', @oleo, @usuario_admin),
  (@mov_base + 60, 'SAIDA', 10, DATE_SUB(CURDATE(), INTERVAL 8 DAY), 'Perda: Validade próxima, doado para cozinha parceira', @oleo, @usuario_voluntario),
  (@mov_base + 61, 'ENTRADA', 30, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'Entrada registrada no sistema', @leite, @usuario_admin),
  (@mov_base + 62, 'SAIDA', 15, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'Doação de cestas básicas', @macarrao, @usuario_voluntario),
  (@mov_base + 63, 'SAIDA', 10, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'Doação de cestas básicas', @bolacha, @usuario_admin),
  (@mov_base + 64, 'ENTRADA', 24, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'Entrada registrada no sistema', @papel, @usuario_voluntario),
  (@mov_base + 65, 'ENTRADA', 30, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Entrada registrada no sistema', @arroz, @usuario_admin),
  (@mov_base + 66, 'SAIDA', 10, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Doação de cestas básicas', @feijao, @usuario_voluntario),
  (@mov_base + 67, 'ENTRADA', 20, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Entrada registrada no sistema', @pasta, @usuario_admin);

INSERT INTO movimentacao_estoque (quantidade, fk_movimentacao, fk_produto_lote) VALUES
  (60, @mov_base + 1, @lote_base + 1),
  (50, @mov_base + 2, @lote_base + 2),
  (40, @mov_base + 3, @lote_base + 3),
  (30, @mov_base + 4, @lote_base + 1),
  (25, @mov_base + 5, @lote_base + 2),
  (45, @mov_base + 6, @lote_base + 4),
  (48, @mov_base + 7, @lote_base + 5),
  (30, @mov_base + 8, @lote_base + 6),
  (20, @mov_base + 9, @lote_base + 4),
  (15, @mov_base + 10, @lote_base + 3),
  (50, @mov_base + 11, @lote_base + 7),
  (36, @mov_base + 12, @lote_base + 8),
  (30, @mov_base + 13, @lote_base + 1),
  (10, @mov_base + 13, @lote_base + 7),
  (15, @mov_base + 14, @lote_base + 6),
  (20, @mov_base + 15, @lote_base + 5),
  (40, @mov_base + 16, @lote_base + 9),
  (8, @mov_base + 17, @lote_base + 10),
  (20, @mov_base + 18, @lote_base + 9),
  (40, @mov_base + 19, @lote_base + 11),
  (30, @mov_base + 20, @lote_base + 11),
  (50, @mov_base + 21, @lote_base + 12),
  (30, @mov_base + 22, @lote_base + 7),
  (20, @mov_base + 23, @lote_base + 4),
  (15, @mov_base + 24, @lote_base + 8),
  (70, @mov_base + 25, @lote_base + 13),
  (30, @mov_base + 26, @lote_base + 14),
  (30, @mov_base + 27, @lote_base + 12),
  (10, @mov_base + 28, @lote_base + 11),
  (15, @mov_base + 28, @lote_base + 2),
  (10, @mov_base + 29, @lote_base + 9),
  (40, @mov_base + 30, @lote_base + 15),
  (45, @mov_base + 31, @lote_base + 16),
  (25, @mov_base + 32, @lote_base + 17),
  (10, @mov_base + 33, @lote_base + 7),
  (35, @mov_base + 33, @lote_base + 13),
  (20, @mov_base + 34, @lote_base + 3),
  (40, @mov_base + 35, @lote_base + 18),
  (5, @mov_base + 36, @lote_base + 4),
  (20, @mov_base + 36, @lote_base + 15),
  (15, @mov_base + 37, @lote_base + 17),
  (35, @mov_base + 38, @lote_base + 19),
  (28, @mov_base + 39, @lote_base + 5),
  (2, @mov_base + 39, @lote_base + 18),
  (40, @mov_base + 40, @lote_base + 20),
  (20, @mov_base + 41, @lote_base + 19),
  (55, @mov_base + 42, @lote_base + 21),
  (30, @mov_base + 43, @lote_base + 22),
  (35, @mov_base + 44, @lote_base + 13),
  (5, @mov_base + 44, @lote_base + 21),
  (10, @mov_base + 45, @lote_base + 9),
  (15, @mov_base + 45, @lote_base + 16),
  (40, @mov_base + 46, @lote_base + 23),
  (35, @mov_base + 47, @lote_base + 24),
  (25, @mov_base + 48, @lote_base + 20),
  (20, @mov_base + 49, @lote_base + 24),
  (25, @mov_base + 50, @lote_base + 25),
  (20, @mov_base + 51, @lote_base + 8),
  (40, @mov_base + 52, @lote_base + 26),
  (15, @mov_base + 53, @lote_base + 19),
  (10, @mov_base + 53, @lote_base + 2),
  (45, @mov_base + 54, @lote_base + 27),
  (20, @mov_base + 55, @lote_base + 16),
  (30, @mov_base + 56, @lote_base + 28),
  (35, @mov_base + 57, @lote_base + 21),
  (5, @mov_base + 58, @lote_base + 3),
  (15, @mov_base + 58, @lote_base + 14),
  (20, @mov_base + 59, @lote_base + 29),
  (10, @mov_base + 60, @lote_base + 17),
  (30, @mov_base + 61, @lote_base + 30),
  (15, @mov_base + 62, @lote_base + 24),
  (10, @mov_base + 63, @lote_base + 20),
  (24, @mov_base + 64, @lote_base + 31),
  (30, @mov_base + 65, @lote_base + 32),
  (10, @mov_base + 66, @lote_base + 26),
  (20, @mov_base + 67, @lote_base + 33);

COMMIT;
