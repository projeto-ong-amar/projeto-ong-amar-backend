package school.sptech.projeto_amar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.dto.request.CategoriaRequestDTO;
import school.sptech.projeto_amar.dto.response.CategoriaResponseDTO;
import school.sptech.projeto_amar.mapper.CategoriaMapping;
import school.sptech.projeto_amar.service.CategoriaService;

import java.util.List;

@RestController
@RequestMapping("/categoria")
public class CategoriaController {
    private CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> cadastrar(@RequestBody CategoriaRequestDTO c) {
        Categoria cadastrar = service.cadastrar(CategoriaMapping.toDomain(c));
        return ResponseEntity.status(201).body(CategoriaMapping.toResponse(cadastrar));
    }

    @GetMapping ResponseEntity<List<CategoriaResponseDTO>> listar() {
        List<CategoriaResponseDTO> retorno = service.listar().stream().map(CategoriaMapping::toResponse).toList();
        return ResponseEntity.ok(retorno);
    }
}
