package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.movimentacao.entrada.EntradaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.entrada.MovimentacaoHistoricoResponseDTO;
import school.sptech.projeto_amar.dto.movimentacao.perda.PerdaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.saida.SaidaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.MovimentacaoResponseDTO;
import school.sptech.projeto_amar.mapper.MovimentacaoMapping;
import school.sptech.projeto_amar.service.MovimentacaoService;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/movimentacao")
@Tag(name = "Movimentações", description = "Endpoints para registro de movimentações de estoque")
public class MovimentacaoController {

    private MovimentacaoService service;

    public MovimentacaoController(MovimentacaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar movimentações", description = "Lista movimentações com filtros opcionais por produto e/ou tipo")
    @ApiResponse(responseCode = "200", description = "Lista de movimentações retornada com sucesso")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listar(
            @Parameter(description = "ID do produto para filtrar")
            @RequestParam(required = false) Integer produtoId,
            @Parameter(description = "Tipo da movimentação (ENTRADA, SAIDA, PERDA)")
            @RequestParam(required = false) String tipo) {

        if (produtoId != null && tipo != null) {
            TipoMovimentacao tipoEnum = TipoMovimentacao.valueOf(tipo.toUpperCase());
            return ResponseEntity.ok(service.listarPorTipoEProdutoId(tipoEnum, produtoId)
                    .stream().map(MovimentacaoMapping::toResponse).toList());
        } else if (produtoId != null) {
            return ResponseEntity.ok(service.listarPorProdutoId(produtoId)
                    .stream().map(MovimentacaoMapping::toResponse).toList());
        } else if (tipo != null) {
            TipoMovimentacao tipoEnum = TipoMovimentacao.valueOf(tipo.toUpperCase());
            return ResponseEntity.ok(service.listarPorTipo(tipoEnum)
                    .stream().map(MovimentacaoMapping::toResponse).toList());
        } else {
            return ResponseEntity.ok(service.listar()
                    .stream().map(MovimentacaoMapping::toResponse).toList());
        }
    }

    @PostMapping("/entrada")
    @Operation(summary = "Registrar entrada", description = "Registra uma nova entrada de produto no estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entrada registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<MovimentacaoResponseDTO> entrada(@Valid @RequestBody EntradaRequestDTO entrada) {
        Movimentacao movimentacao = service.entrada(entrada);
        return ResponseEntity.status(201).body(MovimentacaoMapping.toResponse(movimentacao));
    }

    @PutMapping("/entrada/{id}")
    public ResponseEntity<MovimentacaoResponseDTO> atualizarEntrada(@Valid @RequestBody EntradaRequestDTO entrada,
                                                                    @PathVariable Integer id) {
        return ResponseEntity.ok(MovimentacaoMapping.toResponse(service.atualizarEntrada(entrada, id)));
    }

    @DeleteMapping("/entrada/{id}")
    public ResponseEntity<Void> removerEntrada(@PathVariable Integer id) {
        service.removerEntrada(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/entrada/historico")
    public ResponseEntity<Page<MovimentacaoHistoricoResponseDTO>> listarHistoricoEntradas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){ 
        Page<MovimentacaoHistoricoResponseDTO> historico = service.listarHistoricoEntrada(PageRequest.of(page, size))
        .map(MovimentacaoMapping::toMovimentacaoHistorico);
        return ResponseEntity.ok(historico);
    }

    @GetMapping("/saida/historico")
    public ResponseEntity<Page<MovimentacaoHistoricoResponseDTO>> listarHistoricoSaida(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        Page<MovimentacaoHistoricoResponseDTO> historico = service.listarHistoricoSaida(PageRequest.of(page, size))
        .map(MovimentacaoMapping::toMovimentacaoHistorico);
        return ResponseEntity.ok(historico);
    }
    

    @PostMapping("/saida")
    @Operation(summary = "Registrar saída", description = "Registra uma saída de produto do estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Saída registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<MovimentacaoResponseDTO> saida(@Valid @RequestBody SaidaRequestDTO saida) {
        Movimentacao movimentacao = service.saida(saida);
        return ResponseEntity.status(201).body(MovimentacaoMapping.toResponse(movimentacao));
    }

    @PutMapping("/saida/{id}")
    public ResponseEntity<MovimentacaoResponseDTO> atualizarSaida(@Valid @RequestBody SaidaRequestDTO saida,
                                                                   @PathVariable Integer id) {
        return ResponseEntity.ok(MovimentacaoMapping.toResponse(service.atualizarSaida(saida, id)));
    }

    @DeleteMapping("/saida/{id}")
    public ResponseEntity<Void> removerSaida(@PathVariable Integer id) {
        service.removerSaida(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/perda")
    @Operation(summary = "Registrar perda", description = "Registra uma perda de produto no estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Perda registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<MovimentacaoResponseDTO> perda(@Valid @RequestBody PerdaRequestDTO perda) {
        Movimentacao movimentacao = service.perda(perda);
        return ResponseEntity.status(201).body(MovimentacaoMapping.toResponse(movimentacao));
    }

    @PutMapping("/perda/{id}")
    public ResponseEntity<MovimentacaoResponseDTO> atualizarPerda(@Valid @RequestBody PerdaRequestDTO perda,
                                                                   @PathVariable Integer id) {
        return ResponseEntity.ok(MovimentacaoMapping.toResponse(service.atualizarPerda(perda, id)));
    }

    @DeleteMapping("/perda/{id}")
    public ResponseEntity<Void> removerPerda(@PathVariable Integer id) {
        service.removerPerda(id);
        return ResponseEntity.noContent().build();
    }
}
