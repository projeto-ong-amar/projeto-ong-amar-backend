package school.sptech.projeto_amar;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import school.sptech.projeto_amar.config.GerenciadorTokenJwt;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.ProdutoLote;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.Categoria;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.dto.usuario.UsuarioDetalhesDTO;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;
import school.sptech.projeto_amar.repository.UsuarioRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Properties;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.ai.google.genai.api-key=test-key")
@AutoConfigureMockMvc
@Import(RespostasErroIntegrationTest.FalhaController.class)
class RespostasErroIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private GerenciadorTokenJwt jwt;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ProdutoRepository produtos;
    @Autowired private ProdutoLoteRepository lotes;
    @Autowired private MovimentacaoRepository movimentacoes;
    @LocalServerPort private int port;
    private Cookie autenticacao;

    @DynamicPropertySource
    static void configuracaoDeErroReal(DynamicPropertyRegistry registry) throws IOException {
        // application.properties de teste substitui o principal; usar os valores reais de segurança.
        Properties properties = new Properties();
        try (var input = Files.newInputStream(Path.of("src/main/resources/application.properties"))) {
            properties.load(input);
        }
        for (String chave : new String[] {"server.error.include-stacktrace", "server.error.include-exception",
                "server.error.include-message", "server.error.include-binding-errors"}) {
            String valor = properties.getProperty(chave);
            assertEquals(chave.equals("server.error.include-exception") ? "false" : "never", valor,
                    "Configuração de segurança inválida: " + chave);
            registry.add(chave, () -> valor);
        }
    }

    @BeforeEach
    void autenticar() {
        autenticacao = cookie(TipoUsuario.ADMINISTRADOR);
    }

    private Cookie cookie(TipoUsuario tipo) {
        Usuario usuario = new Usuario();
        usuario.setNome("Teste");
        usuario.setEmail(UUID.randomUUID() + "@example.com");
        usuario.setSenha("hash-nao-usado");
        usuario.setStatus("ATIVO");
        usuario.setTipoUsuario(tipo);
        var detalhes = new UsuarioDetalhesDTO(usuarios.save(usuario));
        return new Cookie("authToken", jwt.generateToken(
                new UsernamePasswordAuthenticationToken(detalhes, null, detalhes.getAuthorities())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void produtoComNomeEmBrancoRetornaErroPorCampo(String metodo) throws Exception {
        mockMvc.perform(request(org.springframework.http.HttpMethod.valueOf(metodo),
                        metodo.equals("POST") ? "/produtos" : "/produtos/1")
                        .cookie(autenticacao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"   \",\"unidadeMedida\":\"kg\",\"categoria\":\"ALIMENTOS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Confira os campos informados"))
                .andExpect(jsonPath("$.erros[?(@.campo == 'nome')].mensagem",
                        hasItem("O campo é obrigatório")))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }

    @Test
    void nomeMuitoLongoInformaLimiteSemExporValor() throws Exception {
        mockMvc.perform(post("/produtos").cookie(autenticacao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + "a".repeat(301) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("nome"))
                .andExpect(jsonPath("$.erros[0].mensagem").value("O tamanho deve estar entre 3 e 45 caracteres"))
                .andExpect(content().string(not(containsString("a".repeat(301)))));
    }

    @Test
    void cadastroUsuarioRetornaMensagensAmigaveisSemExporSenha() throws Exception {
        mockMvc.perform(post("/usuarios/cadastrar").cookie(autenticacao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":" ","email":"invalido","senha":"segredo",
                                 "tipoUsuario":"VOLUNTARIO","status":"ATIVO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[?(@.campo == 'nome')].mensagem", hasItem("O campo é obrigatório")))
                .andExpect(jsonPath("$.erros[?(@.campo == 'email')].mensagem", hasItem("Informe um e-mail válido")))
                .andExpect(content().string(not(containsString("segredo"))))
                .andExpect(content().string(not(containsString("regexp"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"quantidade\":99999999999}",
            "{\"dataEntrada\":\"data-invalida\"}",
            "{\"quantidade\":",
            ""
    })
    void jsonInvalidoNaoExpoeParser(String corpo) throws Exception {
        mockMvc.perform(post("/movimentacao/entrada").cookie(autenticacao).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição inválido. Confira os tipos e formatos dos campos"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(content().string(not(containsString("JSON parse"))))
                .andExpect(content().string(not(containsString("99999999999"))));
    }

    @Test
    void idInvalidoRetorna400SemDetalhesTecnicos() throws Exception {
        mockMvc.perform(delete("/produtos/null").cookie(autenticacao).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetro inválido. Confira os valores informados"))
                .andExpect(content().string(not(containsString("java.lang"))));
    }

    @Test
    void rotaInexistenteRetorna404Amigavel() throws Exception {
        mockMvc.perform(get("/rota-inexistente").cookie(autenticacao))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Recurso não encontrado"))
                .andExpect(content().string(not(containsString("No static resource"))));
    }

    @Test
    void preservaMensagemEStatusDeNegocio() throws Exception {
        mockMvc.perform(delete("/produtos/2147483647").cookie(autenticacao).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto não encontrado"));
    }

    @Test
    void erroInesperadoNaoExpoeDetalhesInternos() throws Exception {
        mockMvc.perform(get("/teste/erro-interno").cookie(autenticacao))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Não foi possível concluir a solicitação. Tente novamente mais tarde"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(content().string(not(containsString("segredo-interno"))));
    }

    @ParameterizedTest
    @EnumSource(TipoUsuario.class)
    void estoqueInsuficienteExpoeSaldoSeguroSemAlterarEstoque(TipoUsuario perfil) throws Exception {
        Cookie sessao = cookie(perfil);
        Produto produto = new Produto();
        produto.setNome("Erro " + UUID.randomUUID());
        produto.setCategoria(Categoria.ALIMENTOS);
        produto.setUnidadeMedida("UN");
        produto.setStatus("ATIVO");
        produto = produtos.save(produto);
        ProdutoLote lote = new ProdutoLote();
        lote.setProduto(produto);
        lote.setQuantidade(20);
        lote.setDataEntrada(LocalDate.now());
        lote.setValidade(LocalDate.now().plusMonths(1));
        lote = lotes.save(lote);

        String corpo = """
                {"quantidade":%d,"dataSaida":"%s","motivo":"Distribuição","fkProduto":%d}
                """;
        String rota = "/movimentacao/saida";
        for (boolean editar : new boolean[] {false, true}) {
            if (editar) {
                mockMvc.perform(post(rota).cookie(sessao).with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(corpo.formatted(5, LocalDate.now(), produto.getId())))
                        .andExpect(status().isCreated());
                var original = movimentacoes.findByTipoAndProdutoId(TipoMovimentacao.SAIDA, produto.getId()).getFirst();
                rota += "/" + original.getId();
            }
            mockMvc.perform(request(editar ? org.springframework.http.HttpMethod.PUT : org.springframework.http.HttpMethod.POST, rota)
                            .cookie(sessao).with(csrf()).param("trace", "true").param("message", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpo.formatted(9999, LocalDate.now(), produto.getId())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Estoque insuficiente"))
                    .andExpect(jsonPath("$.erros").isEmpty())
                    // Na edição, inclui a quantidade da saída original que seria devolvida.
                    .andExpect(jsonPath("$.estoqueDisponivel").value(20))
                    .andExpect(jsonPath("$.trace").doesNotExist())
                    .andExpect(jsonPath("$.exception").doesNotExist())
                    .andExpect(content().string(not(containsString("school.sptech"))))
                    .andExpect(content().string(not(containsString("java.lang"))));
            assertEquals(editar ? 15 : 20, lotes.findById(lote.getId()).orElseThrow().getQuantidade());
            var salvas = movimentacoes.findByTipoAndProdutoId(TipoMovimentacao.SAIDA, produto.getId());
            assertEquals(editar ? 1 : 0, salvas.size());
            if (editar) {
                assertEquals(5, salvas.getFirst().getQuantidade());
            }
        }
    }

    @Test
    void permissaoNegadaContinua403() throws Exception {
        mockMvc.perform(get("/usuarios").cookie(cookie(TipoUsuario.VOLUNTARIO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void fallbackDoServidorNaoExpoeTraceNemMensagemMesmoQuandoSolicitados() throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            var response = client.send(HttpRequest.newBuilder(
                            URI.create("http://localhost:" + port + "/teste/fallback?trace=true&message=true"))
                    .header("Cookie", "authToken=" + autenticacao.getValue())
                    .header("Accept", "application/json").GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(400, response.statusCode());
            assertTrue(response.body().contains("\"status\":400"));
            assertFalse(response.body().contains("segredo-interno"));
            assertFalse(response.body().contains("\"trace\""));
            assertFalse(response.body().contains("\"exception\""));
            assertFalse(response.body().contains("\"message\""));
        }
    }

    @RestController
    static class FalhaController {
        @GetMapping("/teste/erro-interno")
        void falhar() {
            throw new IllegalStateException("segredo-interno");
        }

        @GetMapping("/teste/fallback")
        void fallback(HttpServletResponse response) throws IOException {
            response.sendError(400, "segredo-interno");
        }
    }
}
