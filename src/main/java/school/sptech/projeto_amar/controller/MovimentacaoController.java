package school.sptech.projeto_amar.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.dto.request.EntradaRequestDTO;
import school.sptech.projeto_amar.dto.response.MovimentacaoResponseDTO;
import school.sptech.projeto_amar.mapper.MovimentacaoMapping;
import school.sptech.projeto_amar.service.MovimentacaoService;

import java.util.List;

@RestController
@RequestMapping("/movimentacao")
public class MovimentacaoController {

    private MovimentacaoService service;

    public MovimentacaoController(MovimentacaoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<MovimentacaoResponseDTO>> listar() {
        List<MovimentacaoResponseDTO> retorno =
                service.listar().stream().map(MovimentacaoMapping::toResponse).toList();
        return ResponseEntity.ok(retorno);
    }

    @PostMapping("/entrada")
    public ResponseEntity<MovimentacaoResponseDTO> entrada(@Valid @RequestBody EntradaRequestDTO entrada) {
        Movimentacao movimentacao = service.entrada(entrada);
        return ResponseEntity.status(201).body(MovimentacaoMapping.toResponse(movimentacao));
    }
}
