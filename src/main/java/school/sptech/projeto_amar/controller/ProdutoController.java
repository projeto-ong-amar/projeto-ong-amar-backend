package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.dto.produto.ProdutoRequestDTO;
import school.sptech.projeto_amar.dto.produto.ProdutoResponseDTO;
import school.sptech.projeto_amar.mapper.ProdutoMapping;
import school.sptech.projeto_amar.service.ProdutoService;

import java.util.List;

@RestController
@RequestMapping("/produtos")
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos")
public class ProdutoController {

    private ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar produto", description = "Cria um novo produto no sistema. Requer perfil ADMINISTRADOR")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ProdutoResponseDTO> cadastrar(@Valid @RequestBody ProdutoRequestDTO produto) {
        Produto cadastro = service.cadastrar(ProdutoMapping.toDomain(produto));
        return ResponseEntity.status(201).body(ProdutoMapping.toResponse(cadastro));
    }

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Retorna todos os produtos ou filtra por categoria")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    public ResponseEntity<List<ProdutoResponseDTO>> listar(
            @Parameter(description = "Nome da categoria para filtrar")
            @RequestParam(required = false) String categoria) {

        List<ProdutoResponseDTO> retorno;
        if (categoria != null) {
            retorno = service.listarPorCategoria(categoria)
                    .stream().map(ProdutoMapping::toResponse).toList();
        } else {
            retorno = service.listar().stream().map(ProdutoMapping::toResponse).toList();
        }
        return ResponseEntity.ok(retorno);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto pelo ID. Requer perfil ADMINISTRADOR")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ProdutoResponseDTO> atualizar(
            @Valid @RequestBody ProdutoRequestDTO produto,
            @Parameter(description = "ID do produto") @PathVariable Integer id) {

        Produto atualizado = service.atualizar(id, ProdutoMapping.toDomain(produto));
        return ResponseEntity.ok(ProdutoMapping.toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover produto", description = "Remove um produto pelo ID. Requer perfil VOLUNTARIO ou superior")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> removerPorId(
            @Parameter(description = "ID do produto") @PathVariable Integer id) {

        service.removerPorId(id);
        return ResponseEntity.noContent().build();
    }
}