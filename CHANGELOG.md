# Changelog

All notable changes to this project will be documented in this file.

Format: [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)


## [0.3.0](2026-10-27)

### Release

#### Chores

- v0.4.0


### Security

#### Features

- fortalecer autenticação e sessões


### Src

#### Features

- adds org, school, java import and adds options and adds RelatorioToolCallbackConfig, RelatorioMcpToolsService, RelatorioMcpToolsServiceTest class


### Swagger

#### Documentation

- documenta endpoints de dashboard


### Auth

#### Features

- adiciona recuperacao de senha por email


### General

#### Features

- fixa categorias e adiciona KPIs ao dashboard
- adiciona endpoints de visao atual do estoque e ajusta risco de vencimento
- integração back-end com front-end
- adiciona tabelas visuais nos PDFs dos relatorios com IA
- adiciona indicadores e graficos de estoque
- endpoint grafico movimentacoes por mes
- endpoint das kpis de itens recebidos e distribuidos
- endpoint que mostra os itens que estao perto de vencer
- endpoints de estoque total e itens perto de vencer

#### Bug Fixes

- entrada e saida agora registram datas corretas
- descomentando pre authorize
- habilita CSRF para requisicoes SPA
- ajustando script sql

#### Documentation

- atualiza versao do jar no README
- descreve IDs de movimentacao nos endpoints de atualizar e remover
- documenta endpoints e DTOs que estavam sem descricao no Swagger

#### Style

- padroniza formatação da autenticação
- ajusta formatacao do dashboard mensal


### Contributors

Thank you to 4 community contributors:

@Breno
- feat: endpoint das kpis de itens recebidos e distribuidos
- feat: endpoint que mostra os itens que estao perto de vencer
- feat: endpoints de estoque total e itens perto de vencer
- feat: endpoint grafico movimentacoes por mes
- feat(auth): adiciona recuperacao de senha por email
- feat: adiciona indicadores e graficos de estoque
- style: ajusta formatacao do dashboard mensal
- style: padroniza formatação da autenticação
- fix: habilita CSRF para requisicoes SPA
- feat(security): fortalecer autenticação e sessões
- fix: entrada e saida agora registram datas corretas
- feat: fixa categorias e adiciona KPIs ao dashboard
- feat: adiciona endpoints de visao atual do estoque e ajusta risco de vencimento

@benogoulart
- feat(src): adds org, school, java import and adds options and adds RelatorioToolCallbackConfig, RelatorioMcpToolsService, RelatorioMcpToolsServiceTest class
- docs: descreve IDs de movimentacao nos endpoints de atualizar e remover
- docs: atualiza versao do jar no README
- chore(release): v0.4.0

@Beno-Goulart
- docs(swagger): documenta endpoints de dashboard
- feat: adiciona tabelas visuais nos PDFs dos relatorios com IA
- docs: documenta endpoints e DTOs que estavam sem descricao no Swagger

@DudaF3rnandes
- fix: ajustando script sql
- feat: integração back-end com front-end
- fix: descomentando pre authorize

**Contributors:** @Breno, @benogoulart, @Beno-Goulart, @DudaF3rnandes
