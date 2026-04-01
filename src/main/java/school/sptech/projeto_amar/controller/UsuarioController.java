package school.sptech.projeto_amar.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.dto.request.UsuarioRequestDTO;
import school.sptech.projeto_amar.dto.response.UsuarioResponseDTO;
import school.sptech.projeto_amar.mapper.UsuarioMapping;
import school.sptech.projeto_amar.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
  private UsuarioService service;

  public UsuarioController(UsuarioService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO u) {
    Usuario user = UsuarioMapping.toDomain(u);
    return ResponseEntity.status(201).body(service.cadastrar(user));
  }


}
