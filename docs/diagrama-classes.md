# Diagrama de Classes

## Modelo de dominio

```mermaid
classDiagram
    direction LR

    class Categoria {
        <<enumeration>>
        ALIMENTOS
        PRODUTOS_DE_HIGIENE
    }

    class Produto {
        +Integer id
        +String nome
        +String unidadeMedida
        +String status
    }

    class ProdutoLote {
        +Integer id
        +Integer quantidade
        +LocalDate validade
        +LocalDate dataEntrada
    }

    class Movimentacao {
        +Integer id
        +TipoMovimentacao tipo
        +Integer quantidade
        +LocalDate data
        +String motivo
    }

    class MovimentacaoEstoque {
        +Integer id
        +Integer quantidade
    }

    class Usuario {
        +Integer id
        +String nome
        +String email
        +String senha
        +TipoUsuario tipoUsuario
        +String status
    }

    class TipoMovimentacao {
        <<enumeration>>
    }

    class TipoUsuario {
        <<enumeration>>
    }

    Produto --> Categoria : categoria opcional
    Produto "1" <-- "0..*" ProdutoLote : produto
    Produto "1" <-- "0..*" Movimentacao : produto
    Usuario "1" <-- "0..*" Movimentacao : usuario
    Movimentacao "1" *-- "0..*" MovimentacaoEstoque : itens
    ProdutoLote "1" <-- "0..*" MovimentacaoEstoque : produtoLote
    Movimentacao --> TipoMovimentacao : tipo
    Usuario --> TipoUsuario : tipoUsuario
```

## Camadas da aplicacao

```mermaid
classDiagram
    direction TB

    class CategoriaController {
        <<RestController>>
    }
    class ProdutoController {
        <<RestController>>
    }
    class MovimentacaoController {
        <<RestController>>
    }
    class UsuarioController {
        <<RestController>>
    }
    class DashboardController {
        <<RestController>>
    }
    class AssistenteController {
        <<RestController>>
    }

    class ProdutoService {
        <<Service>>
    }
    class MovimentacaoService {
        <<Service>>
    }
    class UsuarioService {
        <<Service>>
    }
    class DashboardService {
        <<Service>>
    }
    class AssistenteService {
        <<Service>>
    }

    class ProdutoRepository {
        <<Repository>>
    }
    class ProdutoLoteRepository {
        <<Repository>>
    }
    class MovimentacaoRepository {
        <<Repository>>
    }
    class MovimentacaoEstoqueRepository {
        <<Repository>>
    }
    class UsuarioRepository {
        <<Repository>>
    }

    CategoriaController --> Categoria
    ProdutoController --> ProdutoService
    MovimentacaoController --> MovimentacaoService
    UsuarioController --> UsuarioService
    DashboardController --> DashboardService
    AssistenteController --> AssistenteService

    ProdutoService --> ProdutoRepository
    MovimentacaoService --> MovimentacaoRepository
    MovimentacaoService --> ProdutoRepository
    MovimentacaoService --> ProdutoLoteRepository
    MovimentacaoService --> MovimentacaoEstoqueRepository
    MovimentacaoService --> UsuarioRepository
    UsuarioService --> UsuarioRepository
    DashboardService --> ProdutoRepository
    DashboardService --> MovimentacaoRepository
```

As classes de DTO, mappers, configuracao e excecoes foram omitidas para manter o foco no modelo persistido e no fluxo principal da aplicacao.