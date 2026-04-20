package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.dto.usuario.*;
import school.sptech.projeto_amar.mapper.UsuarioMapping;
import school.sptech.projeto_amar.service.UsuarioService;

import java.time.Duration;
import java.util.List;

@Tag(name = "Usuário", description = "Operações relacionadas a usuários")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private UsuarioService service;
  @Value("${jwt.validity}")
  private Long jwtValidity;
  public static final String COOKIE_NOME = "authToken";

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/cadastrar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cadastrar um novo usuário")
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso")
  public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO u) {
    Usuario cadastrado = service.cadastrar(UsuarioMapping.toDomain(u));
    return ResponseEntity.status(201).body(UsuarioMapping.toResponse(cadastrado));
  }

  @PostMapping("/login")
  public ResponseEntity<UsuarioSessaoDTO> login (@Valid @RequestBody UsuarioLoginDTO loginDTO,
                                                 HttpServletResponse response) {
    UsuarioTokenDTO tokenDTO = service.login(loginDTO.email(), loginDTO.senha());

    ResponseCookie cookie = ResponseCookie.from(COOKIE_NOME, tokenDTO.token())
            .httpOnly(true)
            .secure(false) // -> true em produção
            .sameSite("Strict")
            .path("/")
            .maxAge(Duration.ofSeconds(jwtValidity))
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    UsuarioSessaoDTO sessao = UsuarioMapping.toSessao(tokenDTO);
    return ResponseEntity.ok(sessao);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from(COOKIE_NOME, "")
            .httpOnly(true)
            .secure(false)
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity.noContent().build();
  }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar todos os usuários")
    @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso")
  public ResponseEntity<List<UsuarioResponseDTO>> listarTodos(
          @RequestParam(required = false) String tipoUsuario
  ) {
      List<UsuarioResponseDTO> usuarios;

      if (tipoUsuario != null) {
          String tipoUsuarioUpper = tipoUsuario.toUpperCase();
          usuarios = service.listarPorTipo(tipoUsuarioUpper).stream().map(UsuarioMapping::toResponse).toList();
      } else {
          usuarios = service.listarTodos().stream().map(UsuarioMapping::toResponse).toList();
      }

      return ResponseEntity.ok(usuarios);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
   @Operation(summary = "Atualizar um usuário por ID")
    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso")
  public ResponseEntity<UsuarioResponseDTO> atualizar(@Valid @RequestBody UsuarioRequestDTO u,
                                                      @PathVariable Integer id) {
    Usuario atualizado = service.atualizar(id, UsuarioMapping.toDomain(u));
    return ResponseEntity.ok(UsuarioMapping.toResponse(atualizado));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
   @Operation(summary = "Remover um usuário por ID")
    @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso")
  public ResponseEntity<Void> removerPorId(@PathVariable Integer id) {
    service.remover(id);
    return ResponseEntity.noContent().build();
  }


}
