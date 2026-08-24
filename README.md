<div align="center">

<img src="src/main/resources/static/logo-amar.png" alt="ONG AMAR Logo" width="120">

# Projeto AMAR Backend

**Sistema Inteligente de Gestao de Doacoes para ONGs.**

Gerenciamento de estoque com rotatividade FIFO, relatorios inteligentes via IA e geracao de PDF profissional.

<img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white" alt="Java 21">
<img src="https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=flat&logo=springboot&logoColor=white" alt="Spring Boot">
<img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat&logo=mysql&logoColor=white" alt="MySQL">
<img src="https://img.shields.io/badge/Spring_AI-Gemini-4285F4?style=flat&logo=google&logoColor=white" alt="Spring AI + Gemini">
<img src="https://img.shields.io/badge/License-MIT-yellow?style=flat" alt="MIT">

</div>

---

Gerenciamento de estoque completo para uma ONG, com controle de lotes, rotatividade FIFO, autenticacao JWT por cookies, relatorios inteligentes gerados por IA (Google Gemini) e exportacao em PDF com branding profissional. Projetado para voluntarios e administradores, com fallback deterministico quando a IA nao esta disponivel.

## Features

| Feature | Description |
|---|---|
| **Gestao de Estoque** | CRUD completo de produtos, categorias e lotes com quantidades, validades e datas de entrada |
| **Rotatividade FIFO** | Saidas consomem automaticamente os lotes com menor validade primeiro |
| **Controle de Movimentacoes** | Entrada (doacao recebida), Saida (distribuicao) e Perda (avaria) com registro por lote |
| **Relatorios IA** | Analises inteligentes geradas pelo Google Gemini a partir dos dados reais do banco |
| **Exportacao PDF** | Relatorios formatados com logo da ONG, watermark, cards de prioridade e paginacao |
| **Fallback Inteligente** | Quando a IA falha ou excede quota, um relatorio deterministico e gerado automaticamente |
| **Autenticacao JWT** | Login via HttpOnly cookies com SameSite=Strict, tambem suporta header Bearer |
| **Controle de Acesso** | Roles: ADMINISTRADOR (acesso total) e VOLUNTARIO (acesso limitado) |
| **Soft-Delete** | Usuarios, produtos e categorias nunca sao removidos — apenas desativados |
| **Documentacao Swagger** | API documentada com OpenAPI 3, acessivel em `/swagger-ui` |
| **Validacao Multi-Camada** | Respostas da IA sao validadas contra os dados reais antes de serem retornadas |

## Quick Start

### Pre-requisitos

- JDK 21
- MySQL 8+ rodando localmente
- Chave de API do Google Gemini (opcional — funcionalidades de IA desativadas sem ela)

### Configuracao

```bash
cp .env.example .env      # preencha as variaveis de ambiente
```

### Variaveis de Ambiente

| Variavel | Obrigatoria | Descricao |
|---|---|---|
| `DB_URL` | Sim | URL JDBC do MySQL (ex: `jdbc:mysql://localhost:3306/projeto_amar`) |
| `DB_USER` | Sim | Usuario do MySQL |
| `DB_PASSWORD` | Sim | Senha do MySQL |
| `JWT_SECRET` | Sim | Chave Base64 para assinatura HMAC (>= 32 bytes) |
| `JWT_VALIDITY` | Nao | Validade do token em segundos (padrao: 86400 = 24h) |
| `SECURITY_PEPPER` | Sim | Pepper secreto para hash de senhas |
| `GEMINI_API_KEY` | Nao | Chave de API do Google Gemini |
| `GEMINI_MODEL` | Nao | Modelo de IA (padrao: `gemini-3.6-flash`) |

### Executar

```bash
# Windows (PowerShell — carrega .env automaticamente)
.\run.ps1

# Ou via Maven diretamente
.\mvnw.cmd spring-boot:run

# Build e executar JAR
.\mvnw.cmd clean package
java -jar target\projeto-amar-0.0.1-SNAPSHOT.jar
```

### Testes

```bash
.\mvnw.cmd test                              # executar todos os testes (H2 in-memory)
.\mvnw.cmd test -Pcoverage-80                # forcar cobertura minima de 80% nos services
.\mvnw.cmd jacoco:check -Pcoverage-70        # verificar limiar de cobertura
```

## Endpoints

| Metodo | Rota | Autenticacao | Descricao |
|---|---|---|---|
| POST | `/usuarios/cadastrar` | ADMINISTRADOR | Cadastrar usuario |
| POST | `/usuarios/login` | Publico | Login (retorna JWT em cookie) |
| POST | `/usuarios/logout` | Publico | Limpar cookie de autenticacao |
| GET | `/usuarios` | ADMINISTRADOR | Listar usuarios |
| PUT | `/usuarios/{id}` | ADMINISTRADOR | Atualizar usuario |
| DELETE | `/usuarios/{id}` | ADMINISTRADOR | Desativar usuario |
| POST | `/produtos` | ADMINISTRADOR | Cadastrar produto |
| GET | `/produtos` | Autenticado | Listar produtos |
| PUT | `/produtos/{id}` | ADMINISTRADOR | Atualizar produto |
| DELETE | `/produtos/{id}` | ADMINISTRADOR | Desativar produto |
| POST | `/categoria` | ADMINISTRADOR | Cadastrar categoria |
| GET | `/categoria` | Autenticado | Listar categorias |
| PUT | `/categoria/{id}` | ADMINISTRADOR | Atualizar categoria |
| DELETE | `/categoria/{id}` | ADMINISTRADOR | Desativar categoria |
| POST | `/movimentacao/entrada` | Autenticado | Registrar entrada de estoque |
| PUT | `/movimentacao/entrada/{id}` | Autenticado | Atualizar entrada |
| DELETE | `/movimentacao/entrada/{id}` | Autenticado | Remover entrada |
| GET | `/movimentacao/entrada/historico` | Autenticado | Historico paginado de entradas |
| POST | `/movimentacao/saida` | Autenticado | Registrar saida (FIFO) |
| PUT | `/movimentacao/saida/{id}` | Autenticado | Atualizar saida |
| DELETE | `/movimentacao/saida/{id}` | Autenticado | Remover saida |
| GET | `/movimentacao/saida/historico` | Autenticado | Historico paginado de saidas |
| POST | `/movimentacao/perda` | Autenticado | Registrar perda |
| PUT | `/movimentacao/perda/{id}` | Autenticado | Atualizar perda |
| POST | `/assistente/relatorio/entradas` | Publico | Relatorio IA: historico de entradas |
| POST | `/assistente/relatorio/saidas` | Publico | Relatorio IA: historico de saidas |
| POST | `/assistente/relatorio/desempenho-geral` | Publico | Relatorio IA: desempenho geral |
| POST | `/assistente/relatorio/produtos-cadastrados` | Publico | Relatorio IA: produtos cadastrados |
| POST | `/assistente/relatorio/*/pdf` | Publico | Versao PDF de qualquer relatorio |

## Architecture

```mermaid
graph TD
    Client["Frontend / Consumidor"] --> Controllers

    subgraph Controllers["Controller Layer"]
        UC["UsuarioController"]
        PC["ProdutoController"]
        CC["CategoriaController"]
        MC["MovimentacaoController"]
        AC["AssistenteController"]
    end

    Controllers --> Services

    subgraph Services["Service Layer"]
        US["UsuarioService"]
        PS["ProdutoService"]
        CS["CategoriaService"]
        MS["MovimentacaoService"]
        AS["AssistenteService"]
        FS["AssistenteFallbackService"]
        VS["AssistenteIaResponseValidatorService"]
        PDS["PdfService"]
    end

    Services --> Repos

    subgraph Repos["Repository Layer (Spring Data JPA)"]
        UR["UsuarioRepository"]
        PR["ProdutoRepository"]
        CR["CategoriaRepository"]
        MR["MovimentacaoRepository"]
        PLR["ProdutoLoteRepository"]
    end

    Repos --> DB[("MySQL")]

    AS -->|"Spring AI"| Gemini[("Google Gemini")]
    AS -->|"quota 429"| FS
    AS -->|"resposta invalida"| VS
    VS -->|"fallback"| FS
    PDS -->|"OpenPDF"| PDF["PDF Report"]

    subgraph Security["Security"]
        AF["AutenticacaoFilter"]
        AP["AutenticacaoProvider"]
        JT["GerenciadorTokenJwt"]
        PE["PepperPasswordEncoder"]
    end

    Security --> US
```

### Camadas

| Camada | Responsabilidade |
|---|---|
| **Controller** | Endpoints REST, validacao de entrada, mapeamento HTTP |
| **Service** | Logica de negocio, orquestracao, regras de estoque |
| **Repository** | Consultas JPA, queries customizadas, agregacoes |
| **Config** | Seguranca JWT, CORS, inicializacao do admin padrao |
| **Mapper** | Conversao Entity <-> DTO |
| **Exception** | Excecoes de dominio organizadas por entidade |

### Comportamento do Cache IA

Quando a IA responde corretamente, a resposta e validada multi-camada antes de ser retornada. Quando a IA falha:

| Falha | Comportamento |
|---|---|
| Quota excedida (HTTP 429) | Fallback deterministico com aviso de indisponibilidade |
| Resposta nao aderente | Fallback gera relatorio estruturado a partir dos dados brutos |
| Resposta validada | PDF exibe banner laranja indicando modo fallback |

## Tech Stack

| Componente | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Build | Maven (wrapper) |
| ORM | Spring Data JPA + Hibernate |
| Banco de Dados | MySQL 8 (producao) / H2 (testes) |
| Seguranca | Spring Security + JWT (jjwt 0.12.5) |
| Senhas | Argon2 + HMAC-SHA256 pepper |
| IA | Spring AI 2.0 + Google Gemini (`gemini-3.6-flash`) |
| PDF | OpenPDF (LibrePDF) 1.3.39 |
| Documentacao | SpringDoc OpenAPI 2.5.0 |
| Testes | JUnit 5 + Mockito |
| Cobertura | JaCoCo 0.8.12 |

## Project Structure

```
projeto-ong-amar-backend/
├── src/main/java/school/sptech/projeto_amar/
│   ├── ProjetoAmarApplication.java
│   ├── config/
│   │   ├── SecurityConfiguracao.java         # Spring Security + CORS + endpoints
│   │   ├── AutenticacaoFilter.java           # JWT filter (cookie + header)
│   │   ├── AutenticacaoProvider.java         # Custom authentication provider
│   │   ├── GerenciadorTokenJwt.java          # JWT generation/validation
│   │   ├── PepperPasswordEncoder.java        # Argon2 + HMAC-SHA256 pepper
│   │   └── AdminInitializer.java             # Seeds default admin on first run
│   ├── domain/
│   │   ├── Usuario.java                      # User entity
│   │   ├── Produto.java                      # Product entity
│   │   ├── Categoria.java                    # Category entity
│   │   ├── ProdutoLote.java                  # Batch tracking (qty, expiry)
│   │   ├── Movimentacao.java                 # Stock movement
│   │   ├── MovimentacaoEstoque.java          # Movement-to-batch join
│   │   └── enums/                            # TipoUsuario, TipoMovimentacao
│   ├── repository/                           # Spring Data JPA interfaces
│   ├── service/
│   │   ├── MovimentacaoService.java          # FIFO stock logic
│   │   ├── AssistenteService.java            # AI report generation
│   │   ├── AssistenteFallbackService.java    # Deterministic fallback
│   │   ├── AssistenteIaResponseValidatorService.java  # AI validation
│   │   └── PdfService.java                   # PDF generation
│   ├── controller/                           # REST endpoints
│   ├── dto/                                  # Request/Response records
│   ├── mapper/                               # Entity <-> DTO mapping
│   ├── exception/                            # Domain exceptions by entity
│   └── event/
│       └── PdfRelatorioPageEvent.java        # PDF header/watermark/footer
├── src/main/resources/
│   ├── application.properties                # Config (env var placeholders)
│   ├── schema.sql                            # MySQL DDL (6 tables)
│   └── static/logo-amar.png                  # NGO logo for PDFs
├── src/test/                                 # JUnit 5 + Mockito tests (H2)
├── .env.example                              # Environment template
├── run.ps1                                   # PowerShell launcher
└── pom.xml                                   # Maven config
```

### Schema do Banco

```
categoria ──< produto ──< produto_lote
                              │
usuario ──< movimentacao ──< movimentacao_estoque >──┘
```

6 tabelas: `categoria`, `produto`, `produto_lote`, `usuario`, `movimentacao`, `movimentacao_estoque`

## API Documentation

A documentacao interativa da API esta disponivel via Swagger UI:

```
http://localhost:8080/swagger-ui
http://localhost:8080/v3/api-docs
```

## License

MIT
