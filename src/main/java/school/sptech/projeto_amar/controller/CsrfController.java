package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Segurança", description = "Endpoints para inicialização da proteção CSRF")
public class CsrfController {

    @GetMapping("/csrf")
    @Operation(
            summary = "Obter token CSRF",
            description = "Gera o cookie XSRF-TOKEN e retorna seu valor. "
                    + "Envie-o no header X-XSRF-TOKEN em requisições POST, PUT, PATCH e DELETE."
    )
    public ResponseEntity<CsrfToken> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok(csrfToken);
    }
}
