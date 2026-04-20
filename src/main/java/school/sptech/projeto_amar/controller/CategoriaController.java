package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.dto.categoria.CategoriaRequestDTO;
import school.sptech.projeto_amar.dto.categoria.CategoriaResponseDTO;
import school.sptech.projeto_amar.mapper.CategoriaMapping;
import school.sptech.projeto_amar.service.CategoriaService;

import java.util.List;

@Tag(name = "Categoria", description = "Operações relacionadas a categorias")
@RestController
@RequestMapping("/categoria")
public class CategoriaController {
    private CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar uma nova categoria")
    @ApiResponse(responseCode = "201", description = "Categoria cadastrada com sucesso")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CategoriaResponseDTO> cadastrar(@RequestBody CategoriaRequestDTO c) {
        Categoria cadastrar = service.cadastrar(CategoriaMapping.toDomain(c));
        return ResponseEntity.status(201).body(CategoriaMapping.toResponse(cadastrar));
    }

    @GetMapping
    @Operation(summary = "Listar todas as categorias")
    @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso")
    ResponseEntity<List<CategoriaResponseDTO>> listar() {
        List<CategoriaResponseDTO> retorno = service.listar().stream().map(CategoriaMapping::toResponse).toList();
        return ResponseEntity.ok(retorno);
    }
}
