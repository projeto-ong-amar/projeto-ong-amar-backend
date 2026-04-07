package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.dto.request.UsuarioRequestDTO;
import school.sptech.projeto_amar.dto.response.UsuarioResponseDTO;
import school.sptech.projeto_amar.mapper.UsuarioMapping;
import school.sptech.projeto_amar.service.UsuarioService;

import java.util.List;

@Tag(name = "Usuário", description = "Operações relacionadas a usuários")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo usuário")
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO u) {
        Usuario cadastrado = service.cadastrar(UsuarioMapping.toDomain(u));
        return ResponseEntity.status(201).body(UsuarioMapping.toResponse(cadastrado));
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários")
    @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        List<UsuarioResponseDTO> usuarios = service.listarTodos().stream().map(UsuarioMapping::toResponse).toList();
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um usuário por ID")
    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@Valid @RequestBody UsuarioRequestDTO u,
                                                        @PathVariable Integer id) {
        Usuario atualizado = service.atualizar(id, UsuarioMapping.toDomain(u));
        return ResponseEntity.ok(UsuarioMapping.toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover um usuário por ID")
    @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso")
    public ResponseEntity<Void> removerPorId(@PathVariable Integer id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}