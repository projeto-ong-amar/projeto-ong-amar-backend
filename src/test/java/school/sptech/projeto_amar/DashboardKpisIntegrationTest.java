package school.sptech.projeto_amar;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import school.sptech.projeto_amar.config.GerenciadorTokenJwt;
import school.sptech.projeto_amar.dto.usuario.UsuarioDetalhesDTO;
import school.sptech.projeto_amar.domain.enums.Categoria;
import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.ProdutoLote;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;
import school.sptech.projeto_amar.repository.UsuarioRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.ai.google.genai.api-key=test-key",
        "spring.datasource.url=jdbc:h2:mem:dashboard-kpis"
})
@AutoConfigureMockMvc
@Transactional
class DashboardKpisIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private GerenciadorTokenJwt gerenciadorTokenJwt;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private ProdutoLoteRepository loteRepository;
    @Autowired private MovimentacaoRepository movimentacaoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Categoria categoria;
    private Usuario usuario;
    private LocalDate hoje;
    private Cookie authCookie;

    @BeforeEach
    void setUp() {
        hoje = LocalDate.now();
        categoria = Categoria.ALIMENTOS;

        usuario = new Usuario();
        usuario.setNome("Voluntario");
        usuario.setEmail("dashboard@example.com");
        usuario.setSenha("senha");
        usuario.setStatus("ATIVO");
        usuario.setTipoUsuario(TipoUsuario.VOLUNTARIO);
        usuario = usuarioRepository.save(usuario);
        UsuarioDetalhesDTO detalhes = new UsuarioDetalhesDTO(usuario);
        String token = gerenciadorTokenJwt.generateToken(
                new UsernamePasswordAuthenticationToken(detalhes, null, detalhes.getAuthorities()));
        authCookie = new Cookie("authToken", token);
    }

    @Test
    void vencimentoSomaUnidadesNosLimitesSemContarLotes() throws Exception {
        Produto produto = produto("Arroz", "ATIVO");
        lote(produto, 2, hoje);
        lote(produto, 3, hoje.plusDays(7));
        lote(produto, 11, hoje.minusDays(1));
        lote(produto, 13, hoje.plusDays(8));
        lote(produto, 0, hoje.plusDays(1));
        lote(produto, 17, null);
        lote(produto("Inativo", "INATIVO"), 19, hoje);

        indicador("/kpi-vencimento-7-dias", 5);
    }

    @Test
    void estoqueBaixoComparaSomaPorProdutoComLimiteExclusivo() throws Exception {
        Produto baixo = produto("Baixo", "ATIVO");
        lote(baixo, 2, null);
        lote(baixo, 3, null);
        Produto noLimite = produto("No limite", "ATIVO");
        lote(noLimite, 4, null);
        lote(noLimite, 6, null);
        produto("Sem lotes", "ATIVO");
        lote(produto("Zerado", "ATIVO"), 0, null);
        lote(produto("Inativo", "INATIVO"), 1, null);

        indicador("/kpi-estoque-baixo?limite=10", 5);
        indicador("/kpi-estoque-baixo?limite=5", 0);
        for (String parametro : new String[]{"", "?limite=0", "?limite=-1", "?limite=abc"}) {
            mockMvc.perform(get("/dashboard/kpi-estoque-baixo" + parametro).cookie(authCookie))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void doacoesContaRegistrosDeEntradaDeSegundaAHoje() throws Exception {
        LocalDate segunda = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Produto produto = produto("Arroz", "ATIVO");
        movimentacao(produto, TipoMovimentacao.ENTRADA, segunda);
        movimentacao(produto, TipoMovimentacao.ENTRADA, hoje);
        movimentacao(produto, TipoMovimentacao.ENTRADA, segunda.minusDays(1));
        movimentacao(produto, TipoMovimentacao.ENTRADA, hoje.plusDays(1));
        movimentacao(produto, TipoMovimentacao.SAIDA, hoje);
        movimentacao(produto, TipoMovimentacao.PERDA, hoje);

        indicador("/kpi-doacoes-semana", 2);
    }

    @Test
    void semMovimentacaoSomaEstoqueExcluindoQualquerTipoNosUltimos60Dias() throws Exception {
        Produto antigo = produto("Antigo", "ATIVO");
        lote(antigo, 2, null);
        lote(antigo, 3, null);
        movimentacao(antigo, TipoMovimentacao.ENTRADA, hoje.minusDays(60));
        lote(produto("Nunca movimentado", "ATIVO"), 7, null);
        for (TipoMovimentacao tipo : TipoMovimentacao.values()) {
            Produto recente = produto(tipo.name(), "ATIVO");
            lote(recente, 11, null);
            movimentacao(recente, tipo, hoje.minusDays(59));
        }
        Produto atual = produto("Atual", "ATIVO");
        lote(atual, 13, null);
        movimentacao(atual, TipoMovimentacao.ENTRADA, hoje);
        lote(produto("Inativo", "INATIVO"), 17, null);
        produto("Sem estoque", "ATIVO");

        indicador("/kpi-sem-movimentacao-60-dias", 12);
    }

    @Test
    void indicadoresSemDadosRetornamZero() throws Exception {
        indicador("/kpi-vencimento-7-dias", 0);
        indicador("/kpi-estoque-baixo?limite=10", 0);
        indicador("/kpi-doacoes-semana", 0);
        indicador("/kpi-sem-movimentacao-60-dias", 0);
    }

    private void indicador(String rota, long esperado) throws Exception {
        mockMvc.perform(get("/dashboard" + rota).cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(Long.toString(esperado)));
    }

    private Produto produto(String nome, String status) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setCategoria(categoria);
        produto.setStatus(status);
        return produtoRepository.save(produto);
    }

    private void lote(Produto produto, int quantidade, LocalDate validade) {
        ProdutoLote lote = new ProdutoLote();
        lote.setProduto(produto);
        lote.setQuantidade(quantidade);
        lote.setValidade(validade);
        lote.setDataEntrada(hoje.minusDays(100));
        loteRepository.save(lote);
    }

    private void movimentacao(Produto produto, TipoMovimentacao tipo, LocalDate data) {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setProduto(produto);
        movimentacao.setUsuario(usuario);
        movimentacao.setTipo(tipo);
        movimentacao.setData(data);
        movimentacao.setQuantidade(10);
        movimentacaoRepository.save(movimentacao);
    }
}
