package school.sptech.projeto_amar.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.movimentacao.entrada.EntradaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.perda.PerdaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.saida.SaidaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.MovimentacaoResponseDTO;
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
    public ResponseEntity<List<MovimentacaoResponseDTO>> listar(
            @RequestParam(required = false) Integer produtoId,
            @RequestParam(required = false) String tipo
    ) {
        if (produtoId != null && tipo != null) {
            TipoMovimentacao tipoEnum = TipoMovimentacao.valueOf(tipo.toUpperCase());
            return ResponseEntity.ok(service.listarPorTipoEProdutoId(tipoEnum, produtoId).stream().map(MovimentacaoMapping::toResponse).toList());
        } else if (produtoId != null) {
            return ResponseEntity.ok(service.listarPorProdutoId(produtoId).stream().map(MovimentacaoMapping::toResponse).toList());
        } else if (tipo != null) {
            TipoMovimentacao tipoEnum = TipoMovimentacao.valueOf(tipo.toUpperCase());
            return ResponseEntity.ok(service.listarPorTipo(tipoEnum).stream().map(MovimentacaoMapping::toResponse).toList());
        } else {
            return ResponseEntity.ok(service.listar().stream().map(MovimentacaoMapping::toResponse).toList());
        }
    }

    @PostMapping("/entrada")
    public ResponseEntity<MovimentacaoResponseDTO> entrada(@Valid @RequestBody EntradaRequestDTO entrada) {
        Movimentacao movimentacao = service.entrada(entrada);
        return ResponseEntity.status(201).body(MovimentacaoMapping.toResponse(movimentacao));
    }

    @PostMapping("/saida")
    public ResponseEntity<MovimentacaoResponseDTO> saida(@Valid @RequestBody SaidaRequestDTO saida) {
        Movimentacao movimentacao = service.saida(saida);
        return ResponseEntity.status(201).body(MovimentacaoMapping.toResponse(movimentacao));
    }

    @PostMapping("/perda")
    public ResponseEntity<MovimentacaoResponseDTO> perda(@Valid @RequestBody PerdaRequestDTO perda) {
        Movimentacao movimentacao = service.perda(perda);
        return ResponseEntity.status(201).body(MovimentacaoMapping.toResponse(movimentacao));
    }

}
/// /////////////////////////