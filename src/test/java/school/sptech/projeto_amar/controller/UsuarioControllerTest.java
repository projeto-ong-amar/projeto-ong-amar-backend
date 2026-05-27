package school.sptech.projeto_amar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import school.sptech.projeto_amar.config.GerenciadorTokenJwt;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.dto.usuario.UsuarioLoginDTO;
import school.sptech.projeto_amar.dto.usuario.UsuarioRequestDTO;
import school.sptech.projeto_amar.dto.usuario.UsuarioTokenDTO;
import school.sptech.projeto_amar.exception.usuarioExceptions.*;
import school.sptech.projeto_amar.service.AutenticacaoService;
import school.sptech.projeto_amar.service.UsuarioService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import(UsuarioControllerTest.TestSecurityConfig.class)
@TestPropertySource(properties = { "jwt.validity=3600" })
class UsuarioControllerTest {

  @Configuration
  @EnableMethodSecurity
  static class TestSecurityConfig {

    @ControllerAdvice
    static class TestSecurityExceptionHandler {
      @ExceptionHandler(AccessDeniedException.class)
      void handleAccessDenied(HttpServletResponse response) throws Exception {
        response.sendError(HttpStatus.FORBIDDEN.value());
      }

      @ExceptionHandler(AuthenticationException.class)
      void handleAuthentication(HttpServletResponse response) throws Exception {
        response.sendError(HttpStatus.UNAUTHORIZED.value());
      }
    }
  }

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private UsuarioService service;

  @MockitoBean
  private AutenticacaoService autenticacaoService;

  @MockitoBean
  private GerenciadorTokenJwt gerenciadorTokenJwt;

  private Usuario usuario;
  private UsuarioRequestDTO requestDTO;

  @BeforeEach
  void setUp() {
    usuario = new Usuario();
    usuario.setId(1);
    usuario.setNome("João Silva");
    usuario.setEmail("joao@email.com");
    usuario.setSenha("Senha@123");
    usuario.setTipoUsuario(TipoUsuario.VOLUNTARIO);
    usuario.setStatus("ATIVO");

    requestDTO = new UsuarioRequestDTO("João Silva", "joao@email.com", "Senha@123",
        TipoUsuario.VOLUNTARIO, "ATIVO");
  }

  @Nested
  @DisplayName("POST /usuarios/cadastrar")
  class Cadastrar {

    @Test
    @DisplayName("201 - Cadastro com dados válidos e perfil ADMIN")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar201() throws Exception {
      when(service.cadastrar(any(Usuario.class))).thenReturn(usuario);

      mockMvc.perform(post("/usuarios/cadastrar").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("400 - Body inválido (campos obrigatórios nulos)")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar400BodyInvalido() throws Exception {
      UsuarioRequestDTO invalido = new UsuarioRequestDTO(null, null, null, null, null);

      mockMvc.perform(post("/usuarios/cadastrar").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalido)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("409 - Email já cadastrado")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar409EmailDuplicado() throws Exception {
      when(service.cadastrar(any(Usuario.class))).thenThrow(new EmailJaCadastradoException());

      mockMvc.perform(post("/usuarios/cadastrar").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("401 - Sem autenticação")
    void deveRetornar401SemAuth() throws Exception {
      mockMvc.perform(post("/usuarios/cadastrar").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("403 - Autenticado como VOLUNTARIO")
    @WithMockUser(roles = "VOLUNTARIO")
    void deveRetornar403Voluntario() throws Exception {
      mockMvc.perform(post("/usuarios/cadastrar").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("POST /usuarios/login")
  class Login {

    @Test
    @DisplayName("200 - Credenciais válidas")
    @WithMockUser
    void deveRetornar200() throws Exception {
      UsuarioLoginDTO loginDTO = new UsuarioLoginDTO("joao@email.com", "Senha@123");
      UsuarioTokenDTO tokenDTO = new UsuarioTokenDTO(1, "João Silva", "joao@email.com", "token-jwt");
      when(service.login(anyString(), anyString())).thenReturn(tokenDTO);

      mockMvc.perform(post("/usuarios/login").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginDTO)))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("401 - Credenciais inválidas")
    @WithMockUser
    void deveRetornar401CredenciaisInvalidas() throws Exception {
      UsuarioLoginDTO loginDTO = new UsuarioLoginDTO("joao@email.com", "SenhaErrada@1");
      when(service.login(anyString(), anyString()))
          .thenThrow(new BadCredentialsException("Credenciais inválidas"));

      mockMvc.perform(post("/usuarios/login").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginDTO)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("400 - Body inválido")
    @WithMockUser
    void deveRetornar400BodyInvalido() throws Exception {
      UsuarioLoginDTO loginDTO = new UsuarioLoginDTO(null, null);

      mockMvc.perform(post("/usuarios/login").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginDTO)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("POST /usuarios/logout")
  class Logout {

    @Test
    @DisplayName("204 - Logout com sucesso")
    @WithMockUser
    void deveRetornar204() throws Exception {
      mockMvc.perform(post("/usuarios/logout").with(csrf()))
          .andExpect(status().isNoContent());
    }
  }

  @Nested
  @DisplayName("GET /usuarios")
  class Listar {

    @Test
    @DisplayName("200 - Admin autenticado, sem filtro de tipo")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar200SemFiltro() throws Exception {
      when(service.listarTodos()).thenReturn(List.of(usuario));

      mockMvc.perform(get("/usuarios"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("200 - Admin autenticado, filtro ADMINISTRADOR")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar200ComFiltro() throws Exception {
      usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
      when(service.listarPorTipo("ADMINISTRADOR")).thenReturn(List.of(usuario));

      mockMvc.perform(get("/usuarios").param("tipoUsuario", "ADMINISTRADOR"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("400 - Tipo inválido")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar400TipoInvalido() throws Exception {
      when(service.listarPorTipo("INVALIDO")).thenThrow(new TipoInvalidoException());

      mockMvc.perform(get("/usuarios").param("tipoUsuario", "INVALIDO"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("401 - Sem autenticação")
    void deveRetornar401SemAuth() throws Exception {
      mockMvc.perform(get("/usuarios"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("403 - Autenticado como VOLUNTARIO")
    @WithMockUser(roles = "VOLUNTARIO")
    void deveRetornar403Voluntario() throws Exception {
      mockMvc.perform(get("/usuarios"))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("PUT /usuarios/{id}")
  class Atualizar {

    @Test
    @DisplayName("200 - Admin autenticado, ID existente, dados válidos")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar200() throws Exception {
      when(service.atualizar(eq(1), any(Usuario.class))).thenReturn(usuario);

      mockMvc.perform(put("/usuarios/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("404 - ID inexistente")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar404IdInexistente() throws Exception {
      when(service.atualizar(eq(99), any(Usuario.class)))
          .thenThrow(new UsuarioNaoEncontradoException());

      mockMvc.perform(put("/usuarios/99").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 - Body inválido")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar400BodyInvalido() throws Exception {
      UsuarioRequestDTO invalido = new UsuarioRequestDTO(null, null, null, null, null);

      mockMvc.perform(put("/usuarios/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalido)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("403 - Autenticado como VOLUNTARIO")
    @WithMockUser(roles = "VOLUNTARIO")
    void deveRetornar403Voluntario() throws Exception {
      mockMvc.perform(put("/usuarios/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("DELETE /usuarios/{id}")
  class Remover {

    @Test
    @DisplayName("204 - Admin autenticado, ID existente (voluntário)")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar204() throws Exception {
      doNothing().when(service).remover(1);

      mockMvc.perform(delete("/usuarios/1").with(csrf()))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("404 - ID inexistente")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar404IdInexistente() throws Exception {
      doThrow(new UsuarioNaoEncontradoException()).when(service).remover(99);

      mockMvc.perform(delete("/usuarios/99").with(csrf()))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("409 - Último admin")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar409UltimoAdmin() throws Exception {
      doThrow(new ExcluirAdminException()).when(service).remover(1);

      mockMvc.perform(delete("/usuarios/1").with(csrf()))
          .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("403 - Autenticado como VOLUNTARIO")
    @WithMockUser(roles = "VOLUNTARIO")
    void deveRetornar403Voluntario() throws Exception {
      mockMvc.perform(delete("/usuarios/1").with(csrf()))
          .andExpect(status().isForbidden());
    }
  }
}
