package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.dto.categoria.CategoriaRequestDTO;
import school.sptech.projeto_amar.dto.categoria.CategoriaResponseDTO;
import school.sptech.projeto_amar.mapper.CategoriaMapping;
import school.sptech.projeto_amar.service.CategoriaService;

import java.util.List;

@RestController
@RequestMapping("/categoria")
@Tag(name = "Categorias", description = "Endpoints para gerenciamento de categorias")
public class CategoriaController {

    private CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar categoria", description = "Cria uma nova categoria no sistema. Requer perfil ADMINISTRADOR")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CategoriaResponseDTO> cadastrar(@Valid @RequestBody CategoriaRequestDTO c) {
        Categoria cadastrar = service.cadastrar(CategoriaMapping.toDomain(c));
        return ResponseEntity.status(201).body(CategoriaMapping.toResponse(cadastrar));
    }

    @GetMapping
    @Operation(summary = "Listar categorias", description = "Retorna a lista de todas as categorias cadastradas. Requer perfil ADMINISTRADOR")
    @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso")
    public ResponseEntity<List<CategoriaResponseDTO>> listar() {
        List<CategoriaResponseDTO> retorno = service.listar()
                .stream().map(CategoriaMapping::toResponse).toList();
        return ResponseEntity.ok(retorno);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria", description = "Atualiza os dados de uma categoria existente. Requer perfil ADMINISTRADOR")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CategoriaResponseDTO> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaRequestDTO categoria
    ) {
        return ResponseEntity.ok(
                CategoriaMapping.toResponse(service.atualizar(id, categoria))
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover categoria", description = "Remove uma categoria existente do sistema. Requer perfil ADMINISTRADOR")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria removida com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> remover(@PathVariable Integer id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}