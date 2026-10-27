## [0.4.0] 2026-10-27

### Dashboard
#### Features
- fixa categorias e adiciona KPIs ao dashboard
- adiciona endpoints de visao atual do estoque e ajusta risco de vencimento
- adiciona indicadores e graficos de estoque
- endpoint grafico movimentacoes por mes
- endpoint das kpis de itens recebidos e distribuidos
- endpoint que mostra os itens que estao perto de vencer

### Movimentacao
#### Features
- adiciona busca de movimentações com filtros por período
- adiciona endpoint para buscar movimentação por ID
- entrada e saida agora registram datas corretas
- busca de movimentacoes com parametro

#### Bug Fixes
- corrige busca de movimentações por periodo
- altera nome do campo no RelatorioMovimentacaoDTO

### Estoque / Produtos
#### Features
- adiciona busca de produtos com filtros

### Autenticacao / Seguranca
#### Features
- adiciona funcionalidade de recuperação de senha por email
- adiciona configuração e serviço de envio de email
- fortalecer autenticação e sessões
- adiciona recuperacao de senha por email

#### Bug Fixes
- habilita CSRF para requisicoes SPA
- descomentando pre authorize
- ajusta fluxo de autenticação
- corrigindo erro ao listar usuarios

#### Style
- padroniza formatação da autenticação
- formatando codigo

### Relatorios / IA
#### Features
- adiciona endpoint para gerar relatório em PDF por período
- adiciona tabelas visuais nos PDFs dos relatorios com IA
- integração com MCP para relatorios IA

#### Bug Fixes
- corrige envio de email e formatação

### Integracao
#### Features
- adiciona integração com front-end

#### Bug Fixes
- ajustando script sql

### Documentacao
#### Documentation
- descreve IDs de movimentacao nos endpoints de atualizar e remover
- documenta endpoints e DTOs que estavam sem descricao no Swagger
- documenta endpoints de dashboard

### Contributors
Thank you to 3 community contributors:

@Breno
- feat: fixa categorias e adiciona KPIs ao dashboard
- feat: adiciona endpoints de visao atual do estoque e ajusta risco de vencimento
- feat: adiciona indicadores e graficos de estoque
- fix: entrada e saida agora registram datas corretas
- fix: descomentando pre authorize
- feat: adiciona integração back-end com front-end
- feat(auth): adiciona recuperacao de senha por email
- feat: fortalecer autenticação e sessões

@Beno-Goulart
- docs: descreve IDs de movimentacao nos endpoints de atualizar e remover
- docs: documenta endpoints e DTOs que estavam sem descricao no Swagger
- docs(swagger): documenta endpoints de dashboard
- style: padroniza formatação da autenticação
- fix: habilita CSRF para requisicoes SPA
- fix: ajustando script sql

@Gui-Bus
- feat(src): adds org, school, java import and adds options and adds RelatorioToolCallbackConfig, RelatorioMcpToolsService, RelatorioMcpToolsServiceTest class
- feat: adiciona tabelas visuais nos PDFs dos relatorios com IA

**Contributors:** @Breno, @Beno-Goulart, @Gui-Bus
