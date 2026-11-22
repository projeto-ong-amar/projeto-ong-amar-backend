package school.sptech.projeto_amar;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import school.sptech.projeto_amar.config.GerenciadorTokenJwt;
import school.sptech.projeto_amar.domain.*;
import school.sptech.projeto_amar.domain.enums.*;
import school.sptech.projeto_amar.dto.usuario.UsuarioDetalhesDTO;
import school.sptech.projeto_amar.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.ai.google.genai.api-key=test-key")
@AutoConfigureMockMvc
class ResponsavelMovimentacaoIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private GerenciadorTokenJwt jwt;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private ProdutoLoteRepository loteRepository;
    @Autowired private MovimentacaoRepository movimentacaoRepository;

    private Produto produto;
    private ProdutoLote lote;
    private Usuario outroUsuario;
    private Cookie csrf;

    static Stream<org.junit.jupiter.params.provider.Arguments> tiposEPerfis() {
        return Stream.of(TipoMovimentacao.ENTRADA, TipoMovimentacao.SAIDA, TipoMovimentacao.PERDA)
                .flatMap(tipo -> Stream.of(TipoUsuario.ADMINISTRADOR, TipoUsuario.VOLUNTARIO)
                        .map(perfil -> org.junit.jupiter.params.provider.Arguments.of(tipo, perfil)));
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> entradasSaidasEPerfis() {
        return Stream.of(TipoMovimentacao.ENTRADA, TipoMovimentacao.SAIDA)
                .flatMap(tipo -> Stream.of(TipoUsuario.ADMINISTRADOR, TipoUsuario.VOLUNTARIO)
                        .map(perfil -> org.junit.jupiter.params.provider.Arguments.of(tipo, perfil)));
    }

    @BeforeEach
    void setUp() throws Exception {
        outroUsuario = criarUsuario(TipoUsuario.ADMINISTRADOR);
        produto = new Produto();
        produto.setNome("Resp " + UUID.randomUUID());
        produto.setCategoria(Categoria.ALIMENTOS);
        produto.setUnidadeMedida("UN");
        produto.setStatus("ATIVO");
        produto = produtoRepository.save(produto);
        lote = new ProdutoLote();
        lote.setProduto(produto);
        lote.setQuantidade(1000);
        lote.setDataEntrada(LocalDate.now());
        lote.setValidade(LocalDate.now().plusMonths(6));
        lote = loteRepository.save(lote);
        csrf = mockMvc.perform(get("/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(csrf);
    }

    @ParameterizedTest
    @MethodSource("tiposEPerfis")
    void criacaoUsaSessaoIgnorandoFkUsuario(TipoMovimentacao tipo, TipoUsuario perfil) throws Exception {
        Usuario autenticado = criarUsuario(perfil);
        // Outro usuário, ID inexistente, campo ausente e nulo não definem o responsável.
        for (String campo : List.of(",\"fkUsuario\":" + outroUsuario.getId(),
                ",\"fkUsuario\":2147483647", "", ",\"fkUsuario\":null")) {
            criarMovimentacao(tipo, autenticado, campo);
        }
        var salvas = movimentacaoRepository.findByTipoAndProdutoId(tipo, produto.getId());
        assertEquals(4, salvas.size());
        assertTrue(salvas.stream().allMatch(m -> autenticado.getId().equals(m.getUsuario().getId())));
    }

    @ParameterizedTest
    @MethodSource("tiposEPerfis")
    @DisplayName("BUG-06: alterar dados com outro usuário preserva o autor na resposta, banco e histórico")
    void edicaoDeDadosPreservaResponsavelOriginalNoHistorico(TipoMovimentacao tipo, TipoUsuario perfil) throws Exception {
        criarMovimentacao(tipo, outroUsuario, "");
        var original = movimentacaoRepository.findByTipoAndProdutoId(tipo, produto.getId()).getFirst();
        Usuario editor = criarUsuario(perfil);
        int quantidade = original.getQuantidade();
        for (String campo : List.of(",\"fkUsuario\":" + editor.getId(),
                ",\"fkUsuario\":2147483647", "", ",\"fkUsuario\":null")) {
            quantidade++;
            String motivo = "Motivo alterado " + quantidade;
            mockMvc.perform(put(rota(tipo) + "/" + original.getId())
                    .cookie(autenticar(editor), csrf).header("X-XSRF-TOKEN", csrf.getValue())
                    .contentType(MediaType.APPLICATION_JSON).content(corpo(tipo, campo, quantidade, motivo)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantidade").value(quantidade))
                    .andExpect(jsonPath("$.motivo").value(motivo))
                    .andExpect(jsonPath("$.usuario.nome").value(outroUsuario.getNome()));
            var salva = movimentacaoRepository.findById(original.getId()).orElseThrow();
            assertEquals(quantidade, salva.getQuantidade());
            assertEquals(motivo, salva.getMotivo());
            assertEquals(outroUsuario.getId(), salva.getUsuario().getId());

            String historico = tipo == TipoMovimentacao.ENTRADA ? "entrada" : "saida";
            String registro = "$.content[?(@.id == " + original.getId() + ")]";
            mockMvc.perform(get("/movimentacao/" + historico + "/historico")
                    .param("size", "1000").cookie(autenticar(editor)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(registro + ".responsavelNome")
                            .value(org.hamcrest.Matchers.contains(outroUsuario.getNome())))
                    .andExpect(jsonPath(registro + ".quantidade")
                            .value(org.hamcrest.Matchers.contains(quantidade)))
                    .andExpect(jsonPath(registro + ".motivo")
                            .value(org.hamcrest.Matchers.contains(motivo)));
        }
    }

    @ParameterizedTest
    @MethodSource("entradasSaidasEPerfis")
    @DisplayName("BUG-08: editar data atualiza resposta, banco, lote e histórico sem trocar o autor")
    void edicaoDeDataPersisteNoHistorico(TipoMovimentacao tipo, TipoUsuario perfil) throws Exception {
        criarMovimentacao(tipo, outroUsuario, "");
        var original = movimentacaoRepository.findByTipoAndProdutoId(tipo, produto.getId()).getFirst();
        Usuario editor = criarUsuario(perfil);
        // Primeiro altera somente a data; depois altera também a quantidade.
        for (int quantidade : List.of(2, 7)) {
            LocalDate novaData = LocalDate.now().minusDays(quantidade);
            String corpo = corpo(tipo, "", quantidade, original.getMotivo(), novaData);
            mockMvc.perform(put(rota(tipo) + "/" + original.getId())
                    .cookie(autenticar(editor), csrf).header("X-XSRF-TOKEN", csrf.getValue())
                    .contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(novaData.toString()))
                    .andExpect(jsonPath("$.quantidade").value(quantidade))
                    .andExpect(jsonPath("$.usuario.nome").value(outroUsuario.getNome()));

            var salva = movimentacaoRepository.findById(original.getId()).orElseThrow();
            assertEquals(novaData, salva.getData());
            assertEquals(quantidade, salva.getQuantidade());
            assertEquals(outroUsuario.getId(), salva.getUsuario().getId());
            var lotes = loteRepository.findByProdutoIdAndQuantidadeGreaterThanOrderByValidadeAsc(produto.getId(), 0);
            int estoqueEsperado = tipo == TipoMovimentacao.ENTRADA ? 1000 + quantidade : 1000 - quantidade;
            assertEquals(estoqueEsperado, lotes.stream().mapToInt(ProdutoLote::getQuantidade).sum());
            if (tipo == TipoMovimentacao.ENTRADA) {
                var loteEntrada = lotes.stream().filter(l -> !l.getId().equals(lote.getId())).findFirst().orElseThrow();
                assertEquals(novaData, loteEntrada.getDataEntrada());
            } else {
                assertEquals(lote.getDataEntrada(), loteRepository.findById(lote.getId()).orElseThrow().getDataEntrada());
            }

            String registro = "$.content[?(@.id == " + original.getId() + ")]";
            mockMvc.perform(get(rota(tipo) + "/historico").param("size", "1000").cookie(autenticar(editor)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(registro + ".dataHora")
                            .value(org.hamcrest.Matchers.contains(novaData.toString())))
                    .andExpect(jsonPath(registro + ".quantidade")
                            .value(org.hamcrest.Matchers.contains(quantidade)))
                    .andExpect(jsonPath(registro + ".responsavelNome")
                            .value(org.hamcrest.Matchers.contains(outroUsuario.getNome())));
        }
    }

    @ParameterizedTest
    @MethodSource("tiposEPerfis")
    void criacaoEEdicaoExigemSessaoECsrf(TipoMovimentacao tipo, TipoUsuario perfil) throws Exception {
        criarMovimentacao(tipo, outroUsuario, "");
        var original = movimentacaoRepository.findByTipoAndProdutoId(tipo, produto.getId()).getFirst();
        for (var request : List.of(post(rota(tipo)), put(rota(tipo) + "/" + original.getId()))) {
            mockMvc.perform(request.cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue())
                    .contentType(MediaType.APPLICATION_JSON).content(corpo(tipo, "")))
                    .andExpect(status().isUnauthorized());
        }
        Usuario autenticado = criarUsuario(perfil);
        for (var request : List.of(post(rota(tipo)), put(rota(tipo) + "/" + original.getId()))) {
            mockMvc.perform(request.cookie(autenticar(autenticado))
                    .contentType(MediaType.APPLICATION_JSON).content(corpo(tipo, "")))
                    .andExpect(status().isForbidden());
        }
    }

    private void criarMovimentacao(TipoMovimentacao tipo, Usuario usuario, String campo) throws Exception {
        mockMvc.perform(post(rota(tipo)).cookie(autenticar(usuario), csrf)
                .header("X-XSRF-TOKEN", csrf.getValue()).contentType(MediaType.APPLICATION_JSON)
                .content(corpo(tipo, campo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.nome").value(usuario.getNome()));
    }

    private String corpo(TipoMovimentacao tipo, String campo) {
        return corpo(tipo, campo, 2, "Teste responsavel");
    }

    private String corpo(TipoMovimentacao tipo, String campo, int quantidade, String motivo) {
        return corpo(tipo, campo, quantidade, motivo, LocalDate.now());
    }

    private String corpo(TipoMovimentacao tipo, String campo, int quantidade, String motivo, LocalDate dataMovimentacao) {
        String data = switch (tipo) {
            case ENTRADA -> "dataEntrada";
            case SAIDA -> "dataSaida";
            case PERDA -> "dataPerda";
        };
        return """
                {"quantidade":%d,"%s":"%s","dataValidade":"%s","motivo":"%s",
                 "fkProduto":%d,"fkLote":%d%s}
                """.formatted(quantidade, data, dataMovimentacao, LocalDate.now().plusMonths(6), motivo,
                produto.getId(), lote.getId(), campo);
    }

    private String rota(TipoMovimentacao tipo) {
        return "/movimentacao/" + tipo.name().toLowerCase(java.util.Locale.ROOT);
    }

    private Usuario criarUsuario(TipoUsuario perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario " + UUID.randomUUID());
        usuario.setEmail(UUID.randomUUID() + "@example.com");
        usuario.setSenha("hash-nao-usado-no-teste");
        usuario.setTipoUsuario(perfil);
        usuario.setStatus("ATIVO");
        return usuarioRepository.save(usuario);
    }

    private Cookie autenticar(Usuario usuario) {
        var detalhes = new UsuarioDetalhesDTO(usuario);
        return new Cookie("authToken", jwt.generateToken(
                new UsernamePasswordAuthenticationToken(detalhes, null, detalhes.getAuthorities())));
    }
}
