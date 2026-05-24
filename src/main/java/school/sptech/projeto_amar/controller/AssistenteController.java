package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.sptech.projeto_amar.service.AssistenteService;

@RestController
@RequestMapping("/assistente")
public class AssistenteController {
    private final AssistenteService service;

    public AssistenteController(AssistenteService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Enviar pergunta ao assistente", description = "Recebe uma pergunta em texto e retorna a resposta gerada pelo assistente virtual")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Resposta gerada com sucesso", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))), @ApiResponse(responseCode = "400", description = "Pergunta inválida")})
    public ResponseEntity<String> chat(@RequestBody String pergunta) {
        return ResponseEntity.ok(service.responder(pergunta));
    }
}
