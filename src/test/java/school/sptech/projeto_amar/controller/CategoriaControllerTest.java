package school.sptech.projeto_amar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import school.sptech.projeto_amar.config.GerenciadorTokenJwt;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.dto.categoria.CategoriaRequestDTO;
import school.sptech.projeto_amar.exception.categoriaExceptions.CategoriaJaExisteException;
import school.sptech.projeto_amar.exception.categoriaExceptions.CategoriaNaoEncontradaException;
import school.sptech.projeto_amar.exception.categoriaExceptions.SemCategoriasCadastradasException;
import school.sptech.projeto_amar.service.AutenticacaoService;
import school.sptech.projeto_amar.service.CategoriaService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
@Import(CategoriaControllerTest.TestSecurityConfig.class)
class CategoriaControllerTest {

  @Configuration
  @EnableMethodSecurity
  static class TestSecurityConfig {

    @ControllerAdvice
    static class TestSecurityExceptionHandler {
      @ExceptionHandler(AccessDeniedException.class)
      void handleAccessDenied(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.sendError(HttpStatus.FORBIDDEN.value());
      }

      @ExceptionHandler(AuthenticationException.class)
      void handleAuthentication(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.sendError(HttpStatus.UNAUTHORIZED.value());
      }
    }
  }

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private CategoriaService service;

  @MockitoBean
  private AutenticacaoService autenticacaoService;

  @MockitoBean
  private GerenciadorTokenJwt gerenciadorTokenJwt;

  private Categoria categoria;
  private CategoriaRequestDTO requestDTO;

  @BeforeEach
  void setUp() {
    categoria = new Categoria();
    categoria.setId(1);
    categoria.setNome("Alimentos");
    categoria.setDescricao("Produtos alimentícios");
    categoria.setStatus("ATIVO");

    requestDTO = new CategoriaRequestDTO("Alimentos", "Produtos alimentícios");
  }

  @Nested
  @DisplayName("POST /categoria")
  class Cadastrar {

    @Test
    @DisplayName("201 - Admin autenticado, dados válidos")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar201() throws Exception {
      when(service.cadastrar(any(Categoria.class))).thenReturn(categoria);

      mockMvc.perform(post("/categoria").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("409 - Nome duplicado")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar409NomeDuplicado() throws Exception {
      when(service.cadastrar(any(Categoria.class))).thenThrow(new CategoriaJaExisteException());

      mockMvc.perform(post("/categoria").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("400 - Body inválido")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar400BodyInvalido() throws Exception {
      CategoriaRequestDTO invalido = new CategoriaRequestDTO(null, null);

      mockMvc.perform(post("/categoria").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalido)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("401 - Sem autenticação")
    void deveRetornar401SemAuth() throws Exception {
      mockMvc.perform(post("/categoria").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("403 - Autenticado como VOLUNTARIO")
    @WithMockUser(roles = "VOLUNTARIO")
    void deveRetornar403Voluntario() throws Exception {
      mockMvc.perform(post("/categoria").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("GET /categoria")
  class Listar {

    @Test
    @DisplayName("200 - Existem categorias ativas")
    @WithMockUser
    void deveRetornar200() throws Exception {
      when(service.listar()).thenReturn(List.of(categoria));

      mockMvc.perform(get("/categoria"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("404 - Nenhuma categoria ativa")
    @WithMockUser
    void deveRetornar404SemCategorias() throws Exception {
      when(service.listar()).thenThrow(new SemCategoriasCadastradasException());

      mockMvc.perform(get("/categoria"))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("PUT /categoria/{id}")
  class Atualizar {

    @Test
    @DisplayName("200 - Admin autenticado, ID existente, dados válidos")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar200() throws Exception {
      when(service.atualizar(eq(1), any(CategoriaRequestDTO.class))).thenReturn(categoria);

      mockMvc.perform(put("/categoria/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("404 - ID inexistente")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar404IdInexistente() throws Exception {
      when(service.atualizar(eq(99), any(CategoriaRequestDTO.class)))
          .thenThrow(new CategoriaNaoEncontradaException());

      mockMvc.perform(put("/categoria/99").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 - Categoria INATIVA")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar404CategoriaInativa() throws Exception {
      when(service.atualizar(eq(1), any(CategoriaRequestDTO.class)))
          .thenThrow(new CategoriaNaoEncontradaException());

      mockMvc.perform(put("/categoria/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 - Body inválido")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar400BodyInvalido() throws Exception {
      CategoriaRequestDTO invalido = new CategoriaRequestDTO(null, null);

      mockMvc.perform(put("/categoria/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalido)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("403 - Autenticado como VOLUNTARIO")
    @WithMockUser(roles = "VOLUNTARIO")
    void deveRetornar403Voluntario() throws Exception {
      mockMvc.perform(put("/categoria/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("DELETE /categoria/{id}")
  class Remover {

    @Test
    @DisplayName("204 - Admin autenticado, ID existente e ativa")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar204() throws Exception {
      doNothing().when(service).remover(1);

      mockMvc.perform(delete("/categoria/1").with(csrf()))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("404 - ID inexistente")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar404IdInexistente() throws Exception {
      doThrow(new CategoriaNaoEncontradaException()).when(service).remover(99);

      mockMvc.perform(delete("/categoria/99").with(csrf()))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 - Categoria INATIVA")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar404CategoriaInativa() throws Exception {
      doThrow(new CategoriaNaoEncontradaException()).when(service).remover(1);

      mockMvc.perform(delete("/categoria/1").with(csrf()))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("403 - Autenticado como VOLUNTARIO")
    @WithMockUser(roles = "VOLUNTARIO")
    void deveRetornar403Voluntario() throws Exception {
      mockMvc.perform(delete("/categoria/1").with(csrf()))
          .andExpect(status().isForbidden());
    }
  }
}
