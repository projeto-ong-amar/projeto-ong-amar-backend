package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
public class ProdutoController {
    private ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo produto")
    @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ProdutoResponseDTO> cadastrar(@Valid @RequestBody ProdutoRequestDTO produto) {
        Produto cadastro = service.cadastrar(ProdutoMapping.toDomain(produto));
        return ResponseEntity.status(201).body(ProdutoMapping.toResponse(cadastro));
    }

    @GetMapping
    @Operation(summary = "Listar todos os produtos")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    public ResponseEntity<List<ProdutoResponseDTO>> listar(
            @RequestParam(required = false) String categoria
    ) {
        List<ProdutoResponseDTO> retorno;
        if (categoria != null) {
            retorno = service.listarPorCategoria(categoria)
                    .stream().map(ProdutoMapping::toResponse).toList();
            return ResponseEntity.ok(retorno);
        }

        retorno = service.listar().stream().map(ProdutoMapping::toResponse).toList();
        return ResponseEntity.ok(retorno);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um produto por ID")
    @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@Valid @RequestBody ProdutoRequestDTO produto,
                                                        @PathVariable Integer id) {
        Produto atualizado = service.atualizar(id, ProdutoMapping.toDomain(produto));
        return ResponseEntity.ok(ProdutoMapping.toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover um produto por ID")
    @ApiResponse(responseCode = "204", description = "Produto removido com sucesso")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public ResponseEntity<Void> removerPorId(@PathVariable Integer id) {
        service.removerPorId(id);
        return ResponseEntity.noContent().build();
    }
}
