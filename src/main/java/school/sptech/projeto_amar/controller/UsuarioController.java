package school.sptech.projeto_amar.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.dto.request.UsuarioRequestDTO;
import school.sptech.projeto_amar.dto.response.UsuarioResponseDTO;
import school.sptech.projeto_amar.mapper.UsuarioMapping;
import school.sptech.projeto_amar.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
  private UsuarioService service;

  public UsuarioController(UsuarioService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO u) {
    Usuario cadastrado = service.cadastrar(UsuarioMapping.toDomain(u));
    return ResponseEntity.status(201).body(UsuarioMapping.toResponse(cadastrado));
  }

  @GetMapping
  public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
    List<UsuarioResponseDTO> usuarios = service.listarTodos().stream().map(UsuarioMapping::toResponse).toList();
    return ResponseEntity.ok(usuarios);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UsuarioResponseDTO> atualizar(@Valid @RequestBody UsuarioRequestDTO u,
                                                      @PathVariable Integer id) {
    Usuario atualizado = service.atualizar(id, UsuarioMapping.toDomain(u));
    return ResponseEntity.ok(UsuarioMapping.toResponse(atualizado));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> removerPorId(@PathVariable Integer id) {
    service.remover(id);
    return ResponseEntity.noContent().build();
  }


}
