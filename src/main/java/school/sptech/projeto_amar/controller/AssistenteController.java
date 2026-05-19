package school.sptech.projeto_amar.controller;

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
    public ResponseEntity<String> chat(@RequestBody String pergunta) {
        return ResponseEntity.ok(service.responder(pergunta));
    }
}
