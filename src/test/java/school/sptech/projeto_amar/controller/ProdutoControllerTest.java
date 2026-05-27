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
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.dto.produto.ProdutoRequestDTO;
import school.sptech.projeto_amar.exception.categoriaExceptions.CategoriaNaoEncontradaException;
import school.sptech.projeto_amar.exception.produtoExceptions.ExisteProdutoByNomeException;
import school.sptech.projeto_amar.exception.produtoExceptions.ProdutoNaoEncontradoException;
import school.sptech.projeto_amar.exception.produtoExceptions.SemProdutosCadastradosException;
import school.sptech.projeto_amar.service.AutenticacaoService;
import school.sptech.projeto_amar.service.ProdutoService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
@Import(ProdutoControllerTest.TestSecurityConfig.class)
class ProdutoControllerTest {

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
  private ProdutoService service;

  @MockitoBean
  private AutenticacaoService autenticacaoService;

  @MockitoBean
  private GerenciadorTokenJwt gerenciadorTokenJwt;

  private Produto produto;
  private Categoria categoria;
  private ProdutoRequestDTO requestDTO;

  @BeforeEach
  void setUp() {
    categoria = new Categoria();
    categoria.setId(1);
    categoria.setNome("Alimentos");
    categoria.setDescricao("Produtos alimentícios");
    categoria.setStatus("ATIVO");

    produto = new Produto();
    produto.setId(1);
    produto.setNome("Arroz");
    produto.setUnidadeMedida("kg");
    produto.setStatus("ATIVO");
    produto.setCategoria(categoria);

    requestDTO = new ProdutoRequestDTO("Arroz", "kg", 1);
  }

  @Nested
  @DisplayName("POST /produtos")
  class Cadastrar {

    @Test
    @DisplayName("201 - Admin autenticado, dados válidos")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar201() throws Exception {
      when(service.cadastrar(any(Produto.class))).thenReturn(produto);

      mockMvc.perform(post("/produtos").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("409 - Nome duplicado (ATIVO)")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar409NomeDuplicado() throws Exception {
      when(service.cadastrar(any(Produto.class))).thenThrow(new ExisteProdutoByNomeException());

      mockMvc.perform(post("/produtos").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("404 - Categoria inexistente")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar404CategoriaInexistente() throws Exception {
      when(service.cadastrar(any(Produto.class))).thenThrow(new CategoriaNaoEncontradaException());

      mockMvc.perform(post("/produtos").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 - Body inválido")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar400BodyInvalido() throws Exception {
      ProdutoRequestDTO invalido = new ProdutoRequestDTO(null, null, null);

      mockMvc.perform(post("/produtos").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalido)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("401 - Sem autenticação")
    void deveRetornar401SemAuth() throws Exception {
      mockMvc.perform(post("/produtos").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("403 - Autenticado como VOLUNTARIO")
    @WithMockUser(roles = "VOLUNTARIO")
    void deveRetornar403Voluntario() throws Exception {
      mockMvc.perform(post("/produtos").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("GET /produtos")
  class Listar {

    @Test
    @DisplayName("200 - Sem filtro, existem produtos")
    @WithMockUser
    void deveRetornar200SemFiltro() throws Exception {
      when(service.listar()).thenReturn(List.of(produto));

      mockMvc.perform(get("/produtos"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("404 - Sem filtro, nenhum produto")
    @WithMockUser
    void deveRetornar404SemProdutos() throws Exception {
      when(service.listar()).thenThrow(new SemProdutosCadastradosException());

      mockMvc.perform(get("/produtos"))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("200 - Categoria existente com produtos")
    @WithMockUser
    void deveRetornar200ComCategoria() throws Exception {
      when(service.listarPorCategoria("Alimentos")).thenReturn(List.of(produto));

      mockMvc.perform(get("/produtos").param("categoria", "Alimentos"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("404 - Categoria inexistente")
    @WithMockUser
    void deveRetornar404CategoriaInexistente() throws Exception {
      when(service.listarPorCategoria("Inexistente"))
          .thenThrow(new CategoriaNaoEncontradaException());

      mockMvc.perform(get("/produtos").param("categoria", "Inexistente"))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 - Categoria existente sem produtos")
    @WithMockUser
    void deveRetornar404CategoriaSemProdutos() throws Exception {
      when(service.listarPorCategoria("Alimentos"))
          .thenThrow(new SemProdutosCadastradosException());

      mockMvc.perform(get("/produtos").param("categoria", "Alimentos"))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("PUT /produtos/{id}")
  class Atualizar {

    @Test
    @DisplayName("200 - Admin autenticado, ID existente, dados válidos")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar200() throws Exception {
      when(service.atualizar(eq(1), any(Produto.class))).thenReturn(produto);

      mockMvc.perform(put("/produtos/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("404 - ID inexistente")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar404IdInexistente() throws Exception {
      when(service.atualizar(eq(99), any(Produto.class)))
          .thenThrow(new ProdutoNaoEncontradoException());

      mockMvc.perform(put("/produtos/99").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 - Produto INATIVO")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar404ProdutoInativo() throws Exception {
      when(service.atualizar(eq(1), any(Produto.class)))
          .thenThrow(new ProdutoNaoEncontradoException());

      mockMvc.perform(put("/produtos/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 - Categoria inexistente")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar404CategoriaInexistente() throws Exception {
      when(service.atualizar(eq(1), any(Produto.class)))
          .thenThrow(new CategoriaNaoEncontradaException());

      mockMvc.perform(put("/produtos/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 - Body inválido")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar400BodyInvalido() throws Exception {
      ProdutoRequestDTO invalido = new ProdutoRequestDTO(null, null, null);

      mockMvc.perform(put("/produtos/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalido)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("403 - Autenticado como VOLUNTARIO")
    @WithMockUser(roles = "VOLUNTARIO")
    void deveRetornar403Voluntario() throws Exception {
      mockMvc.perform(put("/produtos/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestDTO)))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("DELETE /produtos/{id}")
  class Remover {

    @Test
    @DisplayName("204 - Admin autenticado, produto ATIVO")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar204() throws Exception {
      doNothing().when(service).removerPorId(1);

      mockMvc.perform(delete("/produtos/1").with(csrf()))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("404 - ID inexistente")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar404IdInexistente() throws Exception {
      doThrow(new ProdutoNaoEncontradoException()).when(service).removerPorId(99);

      mockMvc.perform(delete("/produtos/99").with(csrf()))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 - Produto INATIVO")
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveRetornar404ProdutoInativo() throws Exception {
      doThrow(new ProdutoNaoEncontradoException()).when(service).removerPorId(1);

      mockMvc.perform(delete("/produtos/1").with(csrf()))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("403 - Autenticado como VOLUNTARIO")
    @WithMockUser(roles = "VOLUNTARIO")
    void deveRetornar403Voluntario() throws Exception {
      mockMvc.perform(delete("/produtos/1").with(csrf()))
          .andExpect(status().isForbidden());
    }
  }
}
