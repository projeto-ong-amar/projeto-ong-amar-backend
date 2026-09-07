package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.dto.usuario.*;
import school.sptech.projeto_amar.mapper.UsuarioMapping;
import school.sptech.projeto_amar.service.UsuarioService;
import school.sptech.projeto_amar.service.LoginAttemptService;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UsuarioController {

    private final UsuarioService service;
    private final LoginAttemptService loginAttemptService;

    @Value("${security.cookie.max-age-seconds}")
    private Long cookieMaxAge;
    @Value("${security.cookie.secure}")
    private boolean secureCookie;

    public static final String COOKIE_NOME = "authToken";

    public UsuarioController(UsuarioService service, LoginAttemptService loginAttemptService) {
        this.service = service;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/cadastrar")
    @Operation(summary = "Cadastrar usuário", description = "Cria um novo usuário no sistema. Requer perfil ADMINISTRADOR")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO u) {
        Usuario cadastrado = service.cadastrar(UsuarioMapping.toDomain(u));
        return ResponseEntity.status(201).body(UsuarioMapping.toResponse(cadastrado));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica o usuário e retorna um token via cookie HttpOnly")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso",
                    headers = @Header(
                            name = "Set-Cookie",
                            description = "authToken (HttpOnly, SameSite=Strict)",
                            schema = @Schema(type = "string", example = "authToken=eyJhbGc...; Path=/; HttpOnly; SameSite=Strict")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<?> login(
            @Valid @RequestBody UsuarioLoginDTO loginDTO,
            HttpServletResponse response,
            HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        try {
            loginAttemptService.verificarPermitido(ip, loginDTO.email());
            UsuarioTokenDTO tokenDTO = service.login(loginDTO.email(), loginDTO.senha());

            ResponseCookie cookie = ResponseCookie.from(COOKIE_NOME, tokenDTO.token())
                   .httpOnly(true)
                   .secure(secureCookie)
                   .sameSite("Strict")
                   .path("/")
                   .maxAge(Duration.ofSeconds(cookieMaxAge))
                   .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            loginAttemptService.limpar(ip, loginDTO.email());

            return ResponseEntity.ok(new UsuarioSessaoDTO(tokenDTO.userId(), tokenDTO.nome(), tokenDTO.email()));
        } catch (AuthenticationException e) {
            loginAttemptService.registrarFalha(ip, loginDTO.email());
            return ResponseEntity.status(401).body(Map.of("mensagem", "Credenciais inválidas"));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Remove o cookie de autenticação do cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<Void> logout(HttpServletResponse response,
                                       @AuthenticationPrincipal UsuarioDetalhesDTO usuario) {
        service.invalidarTokens(usuario.getUsername());

        ResponseCookie cookie = ResponseCookie.from(COOKIE_NOME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioSessaoDTO> me(@AuthenticationPrincipal UsuarioDetalhesDTO usuario) {
        return ResponseEntity.ok(new UsuarioSessaoDTO(usuario.getId(), usuario.getNome(), usuario.getUsername()));
    }

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Lista todos os usuários ou filtra por tipo. Requer perfil ADMINISTRADOR")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos(
            @Parameter(description = "Tipo do usuário (ADMINISTRADOR ou VOLUNTARIO)")
            @RequestParam(required = false) String tipoUsuario) {

        List<UsuarioResponseDTO> usuarios;

        if (tipoUsuario != null) {
            usuarios = service.listarPorTipo(tipoUsuario.toUpperCase())
                    .stream().map(UsuarioMapping::toResponse).toList();
        } else {
            usuarios = service.listarTodos()
                    .stream().map(UsuarioMapping::toResponse).toList();
        }

        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário pelo ID. Requer perfil ADMINISTRADOR")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @Valid @RequestBody UsuarioRequestDTO u,
            @Parameter(description = "ID do usuário") @PathVariable Integer id) {

        Usuario atualizado = service.atualizar(id, UsuarioMapping.toDomain(u));
        return ResponseEntity.ok(UsuarioMapping.toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover usuário", description = "Remove um usuário pelo ID. Requer perfil ADMINISTRADOR")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> removerPorId(
            @Parameter(description = "ID do usuário") @PathVariable Integer id) {

        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
