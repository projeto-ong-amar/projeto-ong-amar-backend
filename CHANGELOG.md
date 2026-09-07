# Changelog

All notable changes to this project will be documented in this file.

Format: [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)


## [0.3.0](2026-08-17)

### Swagger

#### Documentation

- adiciona documentação do CategoriaController
- adiciona documentação do MovimentacaoController


### Release

#### Chores

- v0.2.0


### README

#### Documentation

- adds README.md


### General

#### Features

- configs application.properties
- pom atualizado com configuracoes do jacoco
- testes faltantes das services
- geração de relatórios com Gemini
- melhora análise da IA e moderniza layout do PDF gerado
- implementa geração de relatórios pdf
- permitindo que a url do front no liveserver se conecte
- add jacoco no pom.xml
- endpoint de historico de saidas/perda
- endpoint de historico de entradas finalizado
- inicio do endpoint de historico
- assistente IA com contexto de estoque e respostas estruturadas
- endpoints restantes de movimentacao
- endpoints de categoria
- integração do assistente ao backend
- pesquisa de produtos por categoria
- verificação de exclusão do admin

#### Bug Fixes

- removendo testes desnecessarios e retirando imports
- ajustando declaração de package
- retornando token para o front no login
- removendo import desnecessario
- limite de tokens de resposta do assistente
- endpoint do assistente autorizado no SecurityConfiguracao
- soft delete nos produtos
- tamanho minimo da senha
- regex de senha
- permitir rota /swagger-ui/** no security

#### Refactoring

- criação de método genérico para geração de relatório

#### Documentation

- adiciona documentação do OllamaResponseDTO
- adiciona documentação do PerguntaRequestDTO
- adiciona documentação do RelatorioAssistenteDTO
- adiciona documentação do AssistenteController
- atualiza swagger no MovimentaçãoController e DTOs de movimentação
- atualiza swagger no ProdutoController e DTOs de produto
- atualiza swagger no CategoriaController e DTOs de categoria


### Contributors

Thank you to 2 community contributors:

@Breno
- feat: verificação de exclusão do admin
- feat: pesquisa de produtos por categoria
- fix: tamanho minimo da senha
- fix: regex de senha
- fix: soft delete nos produtos
- feat: endpoints de categoria
- feat: integração do assistente ao backend
- feat: endpoints restantes de movimentacao
- feat: endpoint de historico de saidas/perda
- feat: endpoint de historico de entradas finalizado
- feat: inicio do endpoint de historico
- fix: retornando token para o front no login
- feat: permitindo que a url do front no liveserver se conecte
- fix: removendo import desnecessario
- feat: add jacoco no pom.xml
- fix: removendo testes desnecessarios e retirando imports
- fix: ajustando declaração de package
- feat: configs application.properties
- feat: pom atualizado com configuracoes do jacoco
- feat: testes faltantes das services
- feat: geração de relatórios com Gemini

@Beno-Goulart
- docs: atualiza swagger no MovimentaçãoController e DTOs de movimentação
- docs: atualiza swagger no ProdutoController e DTOs de produto
- docs: atualiza swagger no CategoriaController e DTOs de categoria
- update: Usuarios Controller e DTOs
- fix: permitir rota /swagger-ui/** no security
- update: atualiza estrutura do endpoint movimentações
- feat: assistente IA com contexto de estoque e respostas estruturadas
- docs: adiciona documentação do AssistenteController
- docs(swagger): adiciona documentação do CategoriaController
- docs(swagger): adiciona documentação do MovimentacaoController
- fix: limite de tokens de resposta do assistente
- fix: endpoint do assistente autorizado no SecurityConfiguracao
- docs: adiciona documentação do OllamaResponseDTO
- docs: adiciona documentação do PerguntaRequestDTO
- docs: adiciona documentação do RelatorioAssistenteDTO
- feat: melhora análise da IA e moderniza layout do PDF gerado
- feat: implementa geração de relatórios pdf
- refactor: criação de método genérico para geração de relatório
- chore(release): v0.2.0
- docs(README): adds README.md

**Contributors:** @Breno, @Beno-Goulart

## [0.2.0](2026-08-17)

### Swagger

#### Documentation

- adiciona documentação do CategoriaController
- adiciona documentação do MovimentacaoController


### README

#### Documentation

- adds README.md


### General

#### Features

- configs application.properties
- pom atualizado com configuracoes do jacoco
- testes faltantes das services
- geração de relatórios com Gemini
- melhora análise da IA e moderniza layout do PDF gerado
- implementa geração de relatórios pdf
- permitindo que a url do front no liveserver se conecte
- add jacoco no pom.xml
- endpoint de historico de saidas/perda
- endpoint de historico de entradas finalizado
- inicio do endpoint de historico
- assistente IA com contexto de estoque e respostas estruturadas
- endpoints restantes de movimentacao
- endpoints de categoria
- integração do assistente ao backend
- pesquisa de produtos por categoria
- verificação de exclusão do admin
- admininitializer
- inicialização de admin no primeiro acesso
- busca de usuarios por tipo
- anotações de permissão
- endpoints de login e logoff
- configuração geral
- componentes de segurança
- service de autenticação
- gerenciador de token e encoder pepper
- busca de movimentacoes com parametro
- registro de perdas
- adicionando campos no usuario
- registro de saidas
- adicionando mensagens em todas as validações
- registro de entradas
- arquivos iniciais e endpoint get
- configuração application properties
- registro de saidas
- adicionando mensagens em todas as validações
- registro de entradas
- arquivos iniciais e endpoint get
- configura swagger e atualiza dependências
- endpoint delete
- endpoint atualizar
- endpoint delete
- endpoint de update
- endpoint de cadastro e listagem
- endpoint categorias
- endpoint de listar todos os usuarios
- arquivos iniciais

#### Bug Fixes

- removendo testes desnecessarios e retirando imports
- ajustando declaração de package
- retornando token para o front no login
- removendo import desnecessario
- limite de tokens de resposta do assistente
- endpoint do assistente autorizado no SecurityConfiguracao
- soft delete nos produtos
- tamanho minimo da senha
- regex de senha
- permitir rota /swagger-ui/** no security
- application.properties
- soft delete de usuarios
- cadastro de usuario apenas por administrador
- anotações e securityconfig
- escondendo url do banco
- arquivos do endpoint categorias
- otimização da função de cadatrar usuario
- mensagem de erro de email já cadastrado
- conexão com o banco de dados

#### Refactoring

- criação de método genérico para geração de relatório
- organizando exceptions
- organizando exceptions

#### Documentation

- adiciona documentação do OllamaResponseDTO
- adiciona documentação do PerguntaRequestDTO
- adiciona documentação do RelatorioAssistenteDTO
- adiciona documentação do AssistenteController
- atualiza swagger no MovimentaçãoController e DTOs de movimentação
- atualiza swagger no ProdutoController e DTOs de produto
- atualiza swagger no CategoriaController e DTOs de categoria
- adiciona tags e schemas no Swagger
- documentando endpoints no Swagger


### Contributors

Thank you to 3 community contributors:

@Breno
- feat: verificação de exclusão do admin
- fix: application.properties
- fix: soft delete de usuarios
- feat: admininitializer
- feat: inicialização de admin no primeiro acesso
- feat: busca de usuarios por tipo
- fix: cadastro de usuario apenas por administrador
- feat: anotações de permissão
- fix: anotações e securityconfig
- fix: escondendo url do banco
- feat: endpoints de login e logoff
- feat: configuração geral
- feat: componentes de segurança
- feat: service de autenticação
- feat: pesquisa de produtos por categoria
- fix: tamanho minimo da senha
- fix: regex de senha
- fix: soft delete nos produtos
- feat: endpoints de categoria
- feat: integração do assistente ao backend
- feat: endpoints restantes de movimentacao
- feat: endpoint de historico de saidas/perda
- feat: endpoint de historico de entradas finalizado
- feat: inicio do endpoint de historico
- fix: retornando token para o front no login
- feat: permitindo que a url do front no liveserver se conecte
- fix: removendo import desnecessario
- feat: add jacoco no pom.xml
- fix: removendo testes desnecessarios e retirando imports
- fix: ajustando declaração de package
- feat: configs application.properties
- feat: pom atualizado com configuracoes do jacoco
- feat: testes faltantes das services
- feat: geração de relatórios com Gemini

@brenokas
- feat: arquivos iniciais
- fix: mensagem de erro de email já cadastrado
- feature: dependencia mysql
- feature: endpoint de cadastro funcionando
- fix: conexão com o banco de dados
- feature: iniciando endpoint de cadastro
- feature: schema.sql
- fix: otimização da função de cadatrar usuario
- feat: endpoint de listar todos os usuarios
- feat: endpoint delete
- feat: endpoint de update
- feat: endpoint de cadastro e listagem
- fix: arquivos do endpoint categorias
- feat: endpoint categorias
- feat: endpoint delete
- feat: endpoint atualizar
- feat: registro de perdas
- feat: adicionando campos no usuario
- feat: registro de saidas
- feat: adicionando mensagens em todas as validações
- feat: registro de entradas
- refactor: organizando exceptions
- feat: arquivos iniciais e endpoint get
- feat: configuração application properties
- feat: registro de saidas
- feat: adicionando mensagens em todas as validações
- feat: registro de entradas
- refactor: organizando exceptions
- feat: arquivos iniciais e endpoint get
- feat: gerenciador de token e encoder pepper
- feat: busca de movimentacoes com parametro

@Beno-Goulart
- docs: documentando endpoints no Swagger
- feat: configura swagger e atualiza dependências
- docs: adiciona tags e schemas no Swagger
- docs: atualiza swagger no MovimentaçãoController e DTOs de movimentação
- docs: atualiza swagger no ProdutoController e DTOs de produto
- docs: atualiza swagger no CategoriaController e DTOs de categoria
- update: Usuarios Controller e DTOs
- fix: permitir rota /swagger-ui/** no security
- update: atualiza estrutura do endpoint movimentações
- feat: assistente IA com contexto de estoque e respostas estruturadas
- docs: adiciona documentação do AssistenteController
- docs(swagger): adiciona documentação do CategoriaController
- docs(swagger): adiciona documentação do MovimentacaoController
- fix: limite de tokens de resposta do assistente
- fix: endpoint do assistente autorizado no SecurityConfiguracao
- docs: adiciona documentação do OllamaResponseDTO
- docs: adiciona documentação do PerguntaRequestDTO
- docs: adiciona documentação do RelatorioAssistenteDTO
- feat: melhora análise da IA e moderniza layout do PDF gerado
- feat: implementa geração de relatórios pdf
- refactor: criação de método genérico para geração de relatório
- docs(README): adds README.md

**Contributors:** @Breno, @brenokas, @Beno-Goulart
