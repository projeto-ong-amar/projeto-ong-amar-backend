package school.sptech.projeto_amar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.movimentacao.entrada.EntradaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.perda.PerdaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.saida.SaidaRequestDTO;
import school.sptech.projeto_amar.exception.movimentacaoExceptions.EstoqueInsuficienteException;
import school.sptech.projeto_amar.exception.movimentacaoExceptions.MovimentacaoNaoEncontradaException;
import school.sptech.projeto_amar.exception.movimentacaoExceptions.SemMovimentacoesException;
import school.sptech.projeto_amar.exception.produtoExceptions.ProdutoNaoEncontradoException;
import school.sptech.projeto_amar.exception.produtoLoteExceptions.LoteNaoEncontradoException;
import school.sptech.projeto_amar.exception.usuarioExceptions.UsuarioNaoEncontradoException;
import school.sptech.projeto_amar.service.AutenticacaoService;
import school.sptech.projeto_amar.service.MovimentacaoService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovimentacaoController.class)
@Import(MovimentacaoControllerTest.TestSecurityConfig.class)
class MovimentacaoControllerTest {

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

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @MockitoBean
  private MovimentacaoService service;

  @MockitoBean
  private AutenticacaoService autenticacaoService;

  @MockitoBean
  private GerenciadorTokenJwt gerenciadorTokenJwt;

  private Movimentacao movimentacao;
  private EntradaRequestDTO entradaDTO;
  private SaidaRequestDTO saidaDTO;
  private PerdaRequestDTO perdaDTO;

  @BeforeEach
  void setUp() {
    Produto produto = new Produto();
    produto.setId(1);
    produto.setNome("Arroz");
    produto.setStatus("ATIVO");

    Usuario usuario = new Usuario();
    usuario.setId(1);
    usuario.setNome("João");

    movimentacao = new Movimentacao();
    movimentacao.setId(1);
    movimentacao.setTipo(TipoMovimentacao.ENTRADA);
    movimentacao.setQuantidade(50);
    movimentacao.setProduto(produto);
    movimentacao.setUsuario(usuario);
    movimentacao.setData(LocalDate.now());
    movimentacao.setMotivo("Doação");

    entradaDTO = new EntradaRequestDTO(50, LocalDate.now(), LocalDate.now().plusMonths(6),
        "Doação recebida", 1, 1);
    saidaDTO = new SaidaRequestDTO(10, LocalDate.now(), "Distribuição para famílias", 1, 1);
    perdaDTO = new PerdaRequestDTO(5, LocalDate.now(), "Produto vencido", 1, 1, 1);
  }

  // ==================== LISTAGEM ====================

  @Nested
  @DisplayName("GET /movimentacao")
  class Listar {

    @Test
    @DisplayName("200 - Sem filtros, existem movimentações")
    @WithMockUser
    void deveRetornar200SemFiltros() throws Exception {
      when(service.listar()).thenReturn(List.of(movimentacao));

      mockMvc.perform(get("/movimentacao"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("404 - Sem filtros, nenhuma movimentação")
    @WithMockUser
    void deveRetornar404SemMovimentacoes() throws Exception {
      when(service.listar()).thenThrow(new SemMovimentacoesException());

      mockMvc.perform(get("/movimentacao"))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("200 - Filtro por produto com resultados")
    @WithMockUser
    void deveRetornar200FiltroProduto() throws Exception {
      when(service.listarPorProdutoId(1)).thenReturn(List.of(movimentacao));

      mockMvc.perform(get("/movimentacao").param("produtoId", "1"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("200 - Filtro por tipo com resultados")
    @WithMockUser
    void deveRetornar200FiltroTipo() throws Exception {
      when(service.listarPorTipo(TipoMovimentacao.ENTRADA)).thenReturn(List.of(movimentacao));

      mockMvc.perform(get("/movimentacao").param("tipo", "ENTRADA"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("200 - Filtro combo com resultados")
    @WithMockUser
    void deveRetornar200FiltroCombo() throws Exception {
      when(service.listarPorTipoEProdutoId(TipoMovimentacao.ENTRADA, 1))
          .thenReturn(List.of(movimentacao));

      mockMvc.perform(get("/movimentacao")
          .param("produtoId", "1")
          .param("tipo", "ENTRADA"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("400 - Tipo inválido")
    @WithMockUser
    void deveRetornar400TipoInvalido() throws Exception {
      org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
          () -> mockMvc.perform(get("/movimentacao").param("tipo", "INVALIDO")));
    }
  }

  // ==================== ENTRADA ====================

  @Nested
  @DisplayName("POST /movimentacao/entrada")
  class EntradaPost {

    @Test
    @DisplayName("201 - Dados válidos")
    @WithMockUser
    void deveRetornar201() throws Exception {
      when(service.entrada(any(EntradaRequestDTO.class))).thenReturn(movimentacao);

      mockMvc.perform(post("/movimentacao/entrada").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(entradaDTO)))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("404 - Produto inexistente")
    @WithMockUser
    void deveRetornar404ProdutoInexistente() throws Exception {
      when(service.entrada(any(EntradaRequestDTO.class)))
          .thenThrow(new ProdutoNaoEncontradoException());

      mockMvc.perform(post("/movimentacao/entrada").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(entradaDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 - Usuário inexistente")
    @WithMockUser
    void deveRetornar404UsuarioInexistente() throws Exception {
      when(service.entrada(any(EntradaRequestDTO.class)))
          .thenThrow(new UsuarioNaoEncontradoException());

      mockMvc.perform(post("/movimentacao/entrada").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(entradaDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 - Body inválido")
    @WithMockUser
    void deveRetornar400BodyInvalido() throws Exception {
      EntradaRequestDTO invalido = new EntradaRequestDTO(null, null, null, null, null, null);

      mockMvc.perform(post("/movimentacao/entrada").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalido)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("PUT /movimentacao/entrada/{id}")
  class EntradaPut {

    @Test
    @DisplayName("200 - Dados válidos, movimentação existente tipo ENTRADA")
    @WithMockUser
    void deveRetornar200() throws Exception {
      when(service.atualizarEntrada(any(EntradaRequestDTO.class), eq(1))).thenReturn(movimentacao);

      mockMvc.perform(put("/movimentacao/entrada/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(entradaDTO)))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("404 - Movimentação inexistente")
    @WithMockUser
    void deveRetornar404MovInexistente() throws Exception {
      when(service.atualizarEntrada(any(EntradaRequestDTO.class), eq(99)))
          .thenThrow(new MovimentacaoNaoEncontradaException("Movimentação não encontrada"));

      mockMvc.perform(put("/movimentacao/entrada/99").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(entradaDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 - Movimentação não é ENTRADA")
    @WithMockUser
    void deveRetornar404NaoEntrada() throws Exception {
      when(service.atualizarEntrada(any(EntradaRequestDTO.class), eq(1)))
          .thenThrow(new MovimentacaoNaoEncontradaException("Movimentação não é do tipo ENTRADA"));

      mockMvc.perform(put("/movimentacao/entrada/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(entradaDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 - Estoque insuficiente")
    @WithMockUser
    void deveRetornar400EstoqueInsuficiente() throws Exception {
      when(service.atualizarEntrada(any(EntradaRequestDTO.class), eq(1)))
          .thenThrow(new EstoqueInsuficienteException(10));

      mockMvc.perform(put("/movimentacao/entrada/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(entradaDTO)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("DELETE /movimentacao/entrada/{id}")
  class EntradaDelete {

    @Test
    @DisplayName("204 - Movimentação existente tipo ENTRADA")
    @WithMockUser
    void deveRetornar204() throws Exception {
      doNothing().when(service).removerEntrada(1);

      mockMvc.perform(delete("/movimentacao/entrada/1").with(csrf()))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("404 - Movimentação inexistente")
    @WithMockUser
    void deveRetornar404MovInexistente() throws Exception {
      doThrow(new MovimentacaoNaoEncontradaException("Movimentação não encontrada")).when(service).removerEntrada(99);

      mockMvc.perform(delete("/movimentacao/entrada/99").with(csrf()))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 - Estoque insuficiente")
    @WithMockUser
    void deveRetornar400EstoqueInsuficiente() throws Exception {
      doThrow(new EstoqueInsuficienteException(10)).when(service).removerEntrada(1);

      mockMvc.perform(delete("/movimentacao/entrada/1").with(csrf()))
          .andExpect(status().isBadRequest());
    }
  }

  // ==================== SAÍDA ====================

  @Nested
  @DisplayName("POST /movimentacao/saida")
  class SaidaPost {

    @Test
    @DisplayName("201 - Dados válidos, estoque suficiente")
    @WithMockUser
    void deveRetornar201() throws Exception {
      movimentacao.setTipo(TipoMovimentacao.SAIDA);
      when(service.saida(any(SaidaRequestDTO.class))).thenReturn(movimentacao);

      mockMvc.perform(post("/movimentacao/saida").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(saidaDTO)))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("404 - Produto inexistente")
    @WithMockUser
    void deveRetornar404ProdutoInexistente() throws Exception {
      when(service.saida(any(SaidaRequestDTO.class)))
          .thenThrow(new ProdutoNaoEncontradoException());

      mockMvc.perform(post("/movimentacao/saida").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(saidaDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 - Usuário inexistente")
    @WithMockUser
    void deveRetornar404UsuarioInexistente() throws Exception {
      when(service.saida(any(SaidaRequestDTO.class)))
          .thenThrow(new UsuarioNaoEncontradoException());

      mockMvc.perform(post("/movimentacao/saida").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(saidaDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 - Estoque insuficiente")
    @WithMockUser
    void deveRetornar400EstoqueInsuficiente() throws Exception {
      when(service.saida(any(SaidaRequestDTO.class)))
          .thenThrow(new EstoqueInsuficienteException(5));

      mockMvc.perform(post("/movimentacao/saida").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(saidaDTO)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 - Body inválido")
    @WithMockUser
    void deveRetornar400BodyInvalido() throws Exception {
      SaidaRequestDTO invalido = new SaidaRequestDTO(null, null, null, null, null);

      mockMvc.perform(post("/movimentacao/saida").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalido)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("PUT /movimentacao/saida/{id}")
  class SaidaPut {

    @Test
    @DisplayName("200 - Dados válidos")
    @WithMockUser
    void deveRetornar200() throws Exception {
      movimentacao.setTipo(TipoMovimentacao.SAIDA);
      when(service.atualizarSaida(any(SaidaRequestDTO.class), eq(1))).thenReturn(movimentacao);

      mockMvc.perform(put("/movimentacao/saida/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(saidaDTO)))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("404 - Movimentação inexistente")
    @WithMockUser
    void deveRetornar404MovInexistente() throws Exception {
      when(service.atualizarSaida(any(SaidaRequestDTO.class), eq(99)))
          .thenThrow(new MovimentacaoNaoEncontradaException("Movimentação não encontrada"));

      mockMvc.perform(put("/movimentacao/saida/99").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(saidaDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 - Movimentação não é SAIDA")
    @WithMockUser
    void deveRetornar404NaoSaida() throws Exception {
      when(service.atualizarSaida(any(SaidaRequestDTO.class), eq(1)))
          .thenThrow(new MovimentacaoNaoEncontradaException("Movimentação não é do tipo SAIDA"));

      mockMvc.perform(put("/movimentacao/saida/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(saidaDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 - Estoque insuficiente")
    @WithMockUser
    void deveRetornar400EstoqueInsuficiente() throws Exception {
      when(service.atualizarSaida(any(SaidaRequestDTO.class), eq(1)))
          .thenThrow(new EstoqueInsuficienteException(10));

      mockMvc.perform(put("/movimentacao/saida/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(saidaDTO)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("DELETE /movimentacao/saida/{id}")
  class SaidaDelete {

    @Test
    @DisplayName("204 - Movimentação existente tipo SAIDA")
    @WithMockUser
    void deveRetornar204() throws Exception {
      doNothing().when(service).removerSaida(1);

      mockMvc.perform(delete("/movimentacao/saida/1").with(csrf()))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("404 - Movimentação inexistente")
    @WithMockUser
    void deveRetornar404MovInexistente() throws Exception {
      doThrow(new MovimentacaoNaoEncontradaException("Movimentação não encontrada")).when(service).removerSaida(99);

      mockMvc.perform(delete("/movimentacao/saida/99").with(csrf()))
          .andExpect(status().isNotFound());
    }
  }

  // ==================== PERDA ====================

  @Nested
  @DisplayName("POST /movimentacao/perda")
  class PerdaPost {

    @Test
    @DisplayName("201 - Dados válidos, estoque suficiente")
    @WithMockUser
    void deveRetornar201() throws Exception {
      movimentacao.setTipo(TipoMovimentacao.PERDA);
      when(service.perda(any(PerdaRequestDTO.class))).thenReturn(movimentacao);

      mockMvc.perform(post("/movimentacao/perda").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(perdaDTO)))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("404 - Produto inexistente")
    @WithMockUser
    void deveRetornar404ProdutoInexistente() throws Exception {
      when(service.perda(any(PerdaRequestDTO.class)))
          .thenThrow(new ProdutoNaoEncontradoException());

      mockMvc.perform(post("/movimentacao/perda").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(perdaDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 - Usuário inexistente")
    @WithMockUser
    void deveRetornar404UsuarioInexistente() throws Exception {
      when(service.perda(any(PerdaRequestDTO.class)))
          .thenThrow(new UsuarioNaoEncontradoException());

      mockMvc.perform(post("/movimentacao/perda").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(perdaDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 - Lote inexistente")
    @WithMockUser
    void deveRetornar404LoteInexistente() throws Exception {
      when(service.perda(any(PerdaRequestDTO.class)))
          .thenThrow(new LoteNaoEncontradoException());

      mockMvc.perform(post("/movimentacao/perda").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(perdaDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 - Estoque total insuficiente")
    @WithMockUser
    void deveRetornar400EstoqueTotalInsuficiente() throws Exception {
      when(service.perda(any(PerdaRequestDTO.class)))
          .thenThrow(new EstoqueInsuficienteException(2));

      mockMvc.perform(post("/movimentacao/perda").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(perdaDTO)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 - Quantidade do lote insuficiente")
    @WithMockUser
    void deveRetornar400QuantidadeLoteInsuficiente() throws Exception {
      when(service.perda(any(PerdaRequestDTO.class)))
          .thenThrow(new EstoqueInsuficienteException(3));

      mockMvc.perform(post("/movimentacao/perda").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(perdaDTO)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 - Body inválido")
    @WithMockUser
    void deveRetornar400BodyInvalido() throws Exception {
      PerdaRequestDTO invalido = new PerdaRequestDTO(null, null, null, null, null, null);

      mockMvc.perform(post("/movimentacao/perda").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalido)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("PUT /movimentacao/perda/{id}")
  class PerdaPut {

    @Test
    @DisplayName("200 - Dados válidos")
    @WithMockUser
    void deveRetornar200() throws Exception {
      movimentacao.setTipo(TipoMovimentacao.PERDA);
      when(service.atualizarPerda(any(PerdaRequestDTO.class), eq(1))).thenReturn(movimentacao);

      mockMvc.perform(put("/movimentacao/perda/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(perdaDTO)))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("404 - Movimentação inexistente")
    @WithMockUser
    void deveRetornar404MovInexistente() throws Exception {
      when(service.atualizarPerda(any(PerdaRequestDTO.class), eq(99)))
          .thenThrow(new MovimentacaoNaoEncontradaException("Movimentação não encontrada"));

      mockMvc.perform(put("/movimentacao/perda/99").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(perdaDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 - Movimentação não é PERDA")
    @WithMockUser
    void deveRetornar404NaoPerda() throws Exception {
      when(service.atualizarPerda(any(PerdaRequestDTO.class), eq(1)))
          .thenThrow(new MovimentacaoNaoEncontradaException("Movimentação não é do tipo PERDA"));

      mockMvc.perform(put("/movimentacao/perda/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(perdaDTO)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 - Estoque insuficiente")
    @WithMockUser
    void deveRetornar400EstoqueInsuficiente() throws Exception {
      when(service.atualizarPerda(any(PerdaRequestDTO.class), eq(1)))
          .thenThrow(new EstoqueInsuficienteException(10));

      mockMvc.perform(put("/movimentacao/perda/1").with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(perdaDTO)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("DELETE /movimentacao/perda/{id}")
  class PerdaDelete {

    @Test
    @DisplayName("204 - Movimentação existente tipo PERDA")
    @WithMockUser
    void deveRetornar204() throws Exception {
      doNothing().when(service).removerPerda(1);

      mockMvc.perform(delete("/movimentacao/perda/1").with(csrf()))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("404 - Movimentação inexistente")
    @WithMockUser
    void deveRetornar404MovInexistente() throws Exception {
      doThrow(new MovimentacaoNaoEncontradaException("Movimentação não encontrada")).when(service).removerPerda(99);

      mockMvc.perform(delete("/movimentacao/perda/99").with(csrf()))
          .andExpect(status().isNotFound());
    }
  }
}
