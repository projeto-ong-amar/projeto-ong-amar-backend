package school.sptech.projeto_amar.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ProdutoResponseDTO> cadastrar(@Valid @RequestBody ProdutoRequestDTO produto) {
        Produto cadastro = service.cadastrar(ProdutoMapping.toDomain(produto));
        return ResponseEntity.status(201).body(ProdutoMapping.toResponse(cadastro));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listar() {
        List<ProdutoResponseDTO> retorno = service.listar().stream().map(ProdutoMapping::toResponse).toList();
        return ResponseEntity.ok(retorno);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@Valid @RequestBody ProdutoRequestDTO produto,
                                                        @PathVariable Integer id) {
        Produto atualizado = service.atualizar(id, ProdutoMapping.toDomain(produto));
        return ResponseEntity.ok(ProdutoMapping.toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerPorId(@PathVariable Integer id) {
        service.removerPorId(id);
        return ResponseEntity.noContent().build();
    }
}
