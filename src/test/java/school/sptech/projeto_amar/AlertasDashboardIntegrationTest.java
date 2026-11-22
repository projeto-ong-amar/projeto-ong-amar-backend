package school.sptech.projeto_amar;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import school.sptech.projeto_amar.config.GerenciadorTokenJwt;
import school.sptech.projeto_amar.domain.*;
import school.sptech.projeto_amar.domain.enums.*;
import school.sptech.projeto_amar.dto.usuario.UsuarioDetalhesDTO;
import school.sptech.projeto_amar.repository.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.ai.google.genai.api-key=test-key",
    "spring.datasource.url=jdbc:h2:mem:alertas-dashboard"
})
@AutoConfigureMockMvc
@Transactional
class AlertasDashboardIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired GerenciadorTokenJwt tokens;
  @Autowired UsuarioRepository usuarios;
  @Autowired ProdutoRepository produtos;
  @Autowired ProdutoLoteRepository lotes;
  @Autowired MovimentacaoRepository movimentacoes;
  @Autowired ConfiguracaoMetricaRepository metricas;
  @Autowired ConfiguracaoAlertaDashboardRepository configuracoes;
  @Autowired EntityManager entityManager;

  private Usuario admin;
  private Cookie cookie;
  private final LocalDate hoje = LocalDate.now();
  private static final String CONFIG = "/configuracoes/alertas-dashboard";
  private static final String DESLIGADOS = """
      {"vencimentoProximo":false,"estoqueBaixo":false,"semDoacoes":false,"semMovimentacao":false}
      """;

  @BeforeEach
  void preparar() {
    admin = usuario("admin", TipoUsuario.ADMINISTRADOR);
    cookie = autenticar(admin);
  }

  @Test
  void preferenciasPersistemESaoGlobaisESemDoacoesDesaparece() throws Exception {
    mvc.perform(get(CONFIG).cookie(cookie)).andExpect(status().isOk())
        .andExpect(jsonPath("$.semDoacoes").value(true));
    mvc.perform(get("/dashboard/alertas").cookie(cookie)).andExpect(status().isOk())
        .andExpect(jsonPath("$[0].texto").value("Nenhuma doação registrada esta semana"));
    mvc.perform(put(CONFIG).cookie(cookie).with(csrf()).contentType(MediaType.APPLICATION_JSON)
        .content(DESLIGADOS)).andExpect(status().isOk()).andExpect(jsonPath("$.semDoacoes").value(false));
    entityManager.flush();
    entityManager.clear();
    assertFalse(configuracoes.findById(1).orElseThrow().isSemDoacoes());

    Cookie outroAdmin = autenticar(usuario("outro", TipoUsuario.ADMINISTRADOR));
    mvc.perform(get(CONFIG).cookie(outroAdmin)).andExpect(status().isOk())
        .andExpect(jsonPath("$.semDoacoes").value(false));
    mvc.perform(get("/dashboard/alertas").cookie(outroAdmin)).andExpect(status().isOk())
        .andExpect(content().json("[]"));

    mvc.perform(put(CONFIG).cookie(cookie).with(csrf()).contentType(MediaType.APPLICATION_JSON)
        .content(DESLIGADOS.replace("\"semDoacoes\":false", "\"semDoacoes\":true")))
        .andExpect(status().isOk());
    mvc.perform(get("/dashboard/alertas").cookie(cookie)).andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
    assertEquals(1, configuracoes.count());
  }

  @Test
  void calcularComMetricasReaisSemDuplicarProdutosNemContarInativosOuLotesVazios() throws Exception {
    ConfiguracaoMetrica metrica = new ConfiguracaoMetrica();
    metrica.setEstoqueBaixo(6);
    metrica.setVencimentoProximoDias(3);
    metrica.setRiscoVencimentoDias(9);
    metrica.setSemMovimentacaoDias(4);
    metrica.setPeriodoIndicadoresDias(30);
    metricas.save(metrica);

    Produto antigo = produto("Antigo", "ATIVO");
    lote(antigo, 2, hoje);
    lote(antigo, 3, hoje.plusDays(3));
    movimento(antigo, TipoMovimentacao.SAIDA, hoje.minusDays(4));
    Produto recente = produto("Recente", "ATIVO");
    lote(recente, 6, hoje.plusDays(4));
    movimento(recente, TipoMovimentacao.PERDA, hoje.minusDays(3));
    lote(produto("Inativo", "INATIVO"), 1, hoje);
    lote(produto("Zerado", "ATIVO"), 0, hoje);
    produto("Sem lotes", "ATIVO");
    Produto vencido = produto("Vencido", "ATIVO");
    lote(vencido, 6, hoje.minusDays(1));
    movimento(vencido, TipoMovimentacao.SAIDA, hoje);

    mvc.perform(get("/dashboard/alertas").cookie(cookie)).andExpect(status().isOk())
        .andExpect(jsonPath("$[0].texto").value("Lotes com vencimento em até 3 dias: 2"))
        .andExpect(jsonPath("$[1].texto").value("Produtos com estoque abaixo de 6 unidades: 3"))
        .andExpect(jsonPath("$[2].texto").value("Nenhuma doação registrada esta semana"))
        .andExpect(jsonPath("$[3].texto").value("Produtos em estoque sem movimentação nos últimos 4 dias: 1"));
    mvc.perform(get("/dashboard/kpi-risco-vencimento").cookie(cookie)).andExpect(status().isOk())
        .andExpect(content().string("3"));
  }

  @Test
  void doacaoNaSegundaRemoveAlertaMasEntradaFuturaOuAnteriorNao() throws Exception {
    Produto produto = produto("Doação", "ATIVO");
    LocalDate segunda = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    movimento(produto, TipoMovimentacao.ENTRADA, segunda.minusDays(1));
    movimento(produto, TipoMovimentacao.ENTRADA, hoje.plusDays(1));
    mvc.perform(get("/dashboard/alertas").cookie(cookie)).andExpect(status().isOk())
        .andExpect(jsonPath("$[1].texto").value("Nenhuma doação registrada esta semana"));
    movimento(produto, TipoMovimentacao.ENTRADA, segunda);
    mvc.perform(get("/dashboard/alertas").cookie(cookie)).andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].tipo").value("aviso"));
  }

  @Test
  void cadaPreferenciaFiltraSomenteSeuAlerta() throws Exception {
    lote(produto("Parado", "ATIVO"), 1, hoje.plusDays(1));
    String[] campos = {"vencimentoProximo", "estoqueBaixo", "semDoacoes", "semMovimentacao"};
    String[] tipos = {"perigo", "aviso", "info", "neutro"};
    String habilitados = DESLIGADOS.replace("false", "true");
    mvc.perform(get("/dashboard/alertas").cookie(cookie)).andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(4));
    for (int i = 0; i < campos.length; i++) {
      mvc.perform(put(CONFIG).cookie(cookie).with(csrf()).contentType(MediaType.APPLICATION_JSON)
          .content(habilitados.replace("\"" + campos[i] + "\":true", "\"" + campos[i] + "\":false")))
          .andExpect(status().isOk());
      mvc.perform(get("/dashboard/alertas").cookie(cookie)).andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(3))
          .andExpect(jsonPath("$[*].tipo", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(tipos[i]))));
    }
  }

  @Test
  void semCondicoesDeAlertaRetornaListaVaziaMesmoComTodosHabilitados() throws Exception {
    Produto produto = produto("Saudável", "ATIVO");
    lote(produto, 20, hoje.plusDays(31));
    movimento(produto, TipoMovimentacao.ENTRADA, hoje);
    mvc.perform(get("/dashboard/alertas").cookie(cookie)).andExpect(status().isOk())
        .andExpect(content().json("[]"));
  }

  @Test
  void impedirAnonimoVoluntarioCsrfAusenteECamposInvalidosSemAlterarConfiguracao() throws Exception {
    for (String rota : new String[]{CONFIG, "/dashboard/alertas"}) {
      mvc.perform(get(rota)).andExpect(status().isUnauthorized());
      mvc.perform(get(rota).cookie(autenticar(usuario("vol" + rota.hashCode(), TipoUsuario.VOLUNTARIO))))
          .andExpect(status().isForbidden());
    }
    mvc.perform(put(CONFIG).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(DESLIGADOS))
        .andExpect(status().isForbidden());
    mvc.perform(put(CONFIG).cookie(autenticar(usuario("volPut", TipoUsuario.VOLUNTARIO))).with(csrf())
        .contentType(MediaType.APPLICATION_JSON).content(DESLIGADOS)).andExpect(status().isForbidden());
    for (String corpo : new String[]{"{}", DESLIGADOS.replace("\"semDoacoes\":false", "\"semDoacoes\":null")}) {
      mvc.perform(put(CONFIG).cookie(cookie).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(corpo))
          .andExpect(status().isBadRequest());
    }
    assertEquals(0, configuracoes.count());
  }

  private Usuario usuario(String nome, TipoUsuario tipo) {
    Usuario usuario = new Usuario();
    usuario.setNome(nome);
    usuario.setEmail(nome + "@example.com");
    usuario.setSenha("senha-fixture");
    usuario.setStatus("ATIVO");
    usuario.setTipoUsuario(tipo);
    return usuarios.save(usuario);
  }

  private Cookie autenticar(Usuario usuario) {
    var detalhes = new UsuarioDetalhesDTO(usuario);
    return new Cookie("authToken", tokens.generateToken(
        new UsernamePasswordAuthenticationToken(detalhes, null, detalhes.getAuthorities())));
  }

  private Produto produto(String nome, String status) {
    Produto produto = new Produto();
    produto.setNome(nome);
    produto.setCategoria(Categoria.ALIMENTOS);
    produto.setStatus(status);
    return produtos.save(produto);
  }

  private void lote(Produto produto, int quantidade, LocalDate validade) {
    ProdutoLote lote = new ProdutoLote();
    lote.setProduto(produto);
    lote.setQuantidade(quantidade);
    lote.setValidade(validade);
    lote.setDataEntrada(hoje.minusDays(100));
    lotes.save(lote);
  }

  private void movimento(Produto produto, TipoMovimentacao tipo, LocalDate data) {
    Movimentacao movimento = new Movimentacao();
    movimento.setProduto(produto);
    movimento.setUsuario(admin);
    movimento.setQuantidade(10);
    movimento.setTipo(tipo);
    movimento.setData(data);
    movimentacoes.save(movimento);
  }
}
