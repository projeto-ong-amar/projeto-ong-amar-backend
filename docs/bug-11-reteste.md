# BUG-11: complemento ao reteste

## Diagnóstico

O [comentário da issue #33](https://github.com/projeto-ong-amar/projeto-ong-amar-backend/issues/33#issuecomment-7099903114)
relata edição de saída com quantidade 9999, erro 400 e trace no perfil `dev`,
no backend `a0ae1ff`. Esse commit é anterior ao PR #41, que introduziu o
handler estruturado e desabilitou detalhes internos no fallback do servidor.
Não há evidência nesse relato de exposição em produção ou na base atual.

Na base `ddbf226`, faltava publicar o estoque disponível sem usar a mensagem
interna da exceção. O novo handler mantém o contrato anterior e acrescenta
`estoqueDisponivel` somente ao erro de estoque insuficiente, a partir de um
atributo tipado da exceção. Não altera cálculos, regras ou permissões.

## Evidências locais (23/11/2026)

Ambiente: Java 21, Maven 3.8.7, H2 e configurações de teste, sem copiar `.env`.

- Antes da correção, `mvn -q -Dtest=RespostasErroIntegrationTest,ConfiguracaoErroPerfisTest test`:
  18 testes, duas falhas pela ausência de `$.estoqueDisponivel`, uma por perfil
  de usuário; demais verificações aprovadas.
- Depois da correção:
  `mvn -q -Dtest=RespostasErroIntegrationTest,ConfiguracaoErroPerfisTest,MovimentacaoServiceTest test`
  aprovado. Cobre criação e edição de saída para administrador e voluntário,
  erro 400 com saldo público, ausência de trace/classes, estoque e quantidade
  persistidos intactos após rejeição.
- Na edição, a quantidade original é devolvida antes do cálculo: com saldo
  atual 15 e saída original 5, o limite público é 20. O teste verifica que a
  tentativa rejeitada mantém saldo 15 e saída 5 no banco.
- `ConfiguracaoErroPerfisTest` carrega separadamente os arquivos reais `dev`
  e `prod` via ConfigData e verifica as quatro opções de erro. Não inicializa
  infraestrutura externa nem valida um deploy de produção.
- O teste HTTP real de fallback continua verificando que `sendError` não
  revela detalhes, mesmo com `trace=true&message=true`.
- `mvn -q test`: 255 testes, cinco falhas de `DashboardKpisIntegrationTest`
  (403 em vez de 200), nenhum erro ou teste ignorado.
- Comparação em worktree isolado de `origin/main`, SHA
  `ddbf226`, com o mesmo `mvn -q test`: 251 testes, as mesmas cinco falhas.
  São preexistentes e ficaram fora do escopo:
  `doacoesContaRegistrosDeEntradaDeSegundaAHoje`,
  `estoqueBaixoComparaSomaPorProdutoComLimiteExclusivo`,
  `indicadoresSemDadosRetornamZero`,
  `semMovimentacaoSomaEstoqueExcluindoQualquerTipoNosUltimos60Dias` e
  `vencimentoSomaUnidadesNosLimitesSemContarLotes`.

A configuração externa do ambiente implantado e o reteste E2E com MySQL/browser
continuam dependendo de validação no ambiente correspondente. Não se afirma que
produção foi testada nem que a suíte completa ficou verde.
