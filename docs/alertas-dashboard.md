# Alertas do dashboard (BUG-14)

Contrato complementar à [issue do front-end #47](https://github.com/projeto-ong-amar/projeto-ong-amar-frontend/issues/47).

## Endpoints

Todos exigem administrador autenticado; o PUT também exige CSRF.

- `GET /configuracoes/alertas-dashboard`: retorna as preferências globais.
- `PUT /configuracoes/alertas-dashboard`: substitui as quatro preferências.
  Todos os campos são booleanos obrigatórios, não nulos:

  ```json
  {
    "vencimentoProximo": true,
    "estoqueBaixo": true,
    "semDoacoes": false,
    "semMovimentacao": true
  }
  ```

- `GET /dashboard/alertas`: calcula apenas alertas habilitados que possuem
  condição de alerta. Retorna uma lista de `{ "tipo": "aviso", "texto": "..." }`;
  lista vazia significa que não há alertas ativos. Tipos: `perigo`, `aviso`,
  `info`, `neutro`, nessa ordem. Erros não são convertidos em lista vazia.

## Regras

- **Vencimento próximo:** conta lotes de produtos ativos com saldo positivo
  e validade entre hoje e hoje + `vencimentoProximoDias`, inclusive. Mesma
  unidade e regra do KPI de risco, mas com janela própria. Exclui vencidos,
  lotes vazios e produtos inativos. Não soma unidades nem produtos distintos.
- **Estoque baixo:** conta produtos ativos com soma de todos os lotes menor
  que `estoqueBaixo`, incluindo estoque zero e produtos sem lotes.
- **Sem doações:** aparece quando não há registro de ENTRADA da segunda-feira
  desta semana até hoje, inclusive; não soma quantidades.
- **Sem movimentação:** conta produtos ativos distintos com saldo positivo
  sem ENTRADA, SAIDA ou PERDA nos últimos `semMovimentacaoDias` (hoje e os
  N−1 dias anteriores). Inclui produtos que nunca foram movimentados.

Usa a data local do servidor, independentemente da próxima doação.
Limites vêm de `/configuracoes/metricas`; não altera os KPIs antigos.

## Persistência e implantação

Reutiliza a tabela existente `configuracao_alerta_dashboard`, registro global
de ID 1. Sem registro salvo, as quatro opções são habilitadas.
Não adiciona tabela, coluna, permissão ou dado de auditoria.
As preferências de e-mail por usuário continuam independentes.

Publicar este back-end antes do front-end correspondente. Valores antigos
do `localStorage` não são importados automaticamente para a configuração
global: cada navegador podia ter escolhas diferentes. O administrador deve
revisar e salvar as opções após atualizar.

## Validação

`./mvnw -q -Dtest=AlertasDashboardIntegrationTest test`: 6 testes cobrindo
persistência após flush/clear, outro administrador, atualização/reabilitação,
filtros independentes, métricas não padrão, limites de data, produtos sem lotes,
inativos, saldo zero, lotes múltiplos, lista vazia, autenticação, perfis, CSRF
e payloads ausentes/nulos.

`./mvnw -q test`: 218 testes, 213 aprovados e 5 falhas preexistentes em `DashboardKpisIntegrationTest`
(espera 200 com perfil VOLUNTARIO, recebe 403). A mesma suíte, executada em
worktree isolado da base `d2c8faf`, apresentou as mesmas 5 falhas
(212 testes, 207 aprovados). Nenhum teste antigo foi alterado ou excluído.

`./mvnw -q -DskipTests package` valida o empacotamento separadamente; não
representa aprovação da suíte. `git diff --check` sem erros.
