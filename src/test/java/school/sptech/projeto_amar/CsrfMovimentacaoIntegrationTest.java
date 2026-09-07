package school.sptech.projeto_amar;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import school.sptech.projeto_amar.config.GerenciadorTokenJwt;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.dto.usuario.UsuarioDetalhesDTO;
import school.sptech.projeto_amar.repository.CategoriaRepository;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;
import school.sptech.projeto_amar.repository.UsuarioRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.ai.google.genai.api-key=test-key")
@AutoConfigureMockMvc
class CsrfMovimentacaoIntegrationTest {

    private static final String ENTRADA_JSON = """
            {
              "quantidade": 10,
              "dataEntrada": "2026-09-07",
              "dataValidade": "2027-09-07",
              "motivo": "Doacao recebida",
              "fkProduto": %d,
              "fkUsuario": %d
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GerenciadorTokenJwt gerenciadorTokenJwt;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    private Cookie authCookie;
    private String entradaJson;

    @BeforeEach
    void setUp() {
        Categoria categoria = new Categoria();
        categoria.setNome("Alimentos CSRF");
        categoria.setDescricao("Categoria de teste");
        categoria.setStatus("ATIVO");
        categoria = categoriaRepository.save(categoria);

        Usuario usuario = new Usuario();
        usuario.setNome("Usuario CSRF");
        usuario.setEmail("csrf-" + UUID.randomUUID() + "@example.com");
        usuario.setSenha("senha");
        usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        usuario.setStatus("ATIVO");
        usuario = usuarioRepository.save(usuario);

        Produto produto = new Produto();
        produto.setNome("Produto CSRF");
        produto.setUnidadeMedida("UN");
        produto.setStatus("ATIVO");
        produto.setDataCadastro(LocalDate.now());
        produto.setCategoria(categoria);
        produto = produtoRepository.save(produto);

        UsuarioDetalhesDTO detalhes = new UsuarioDetalhesDTO(usuario);
        String jwt = gerenciadorTokenJwt.generateToken(
                new UsernamePasswordAuthenticationToken(detalhes, null, detalhes.getAuthorities()));

        authCookie = new Cookie("authToken", jwt);
        entradaJson = ENTRADA_JSON.formatted(produto.getId(), usuario.getId());
    }

    @Test
    void csrfEndpointGeraCookieETokenParaRequisicaoAutenticada() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/csrf").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false))
                .andReturn();

        Cookie csrfCookie = ultimoCookieCsrf(resultado);
        mockMvc.perform(get("/csrf").cookie(authCookie, csrfCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void corsPermiteCredenciaisEHeaderCsrfParaOrigemDoVite() throws Exception {
        mockMvc.perform(options("/movimentacao/entrada")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "X-XSRF-TOKEN"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "X-XSRF-TOKEN"));
    }

    @Test
    void postEntradaSemTokenCsrfRetornaForbidden() throws Exception {
        mockMvc.perform(post("/movimentacao/entrada")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entradaJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void postEntradaComJwtETokenCsrfCorrespondenteRetornaCreated() throws Exception {
        Cookie csrfCookie = obterCsrfCookie();

        mockMvc.perform(post("/movimentacao/entrada")
                        .cookie(authCookie, csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entradaJson))
                .andExpect(status().isCreated());
    }

    @Test
    void putEntradaExigeTokenCsrf() throws Exception {
        Integer movimentacaoId = criarEntrada();
        Cookie csrfCookie = obterCsrfCookie();

        mockMvc.perform(put("/movimentacao/entrada/{id}", movimentacaoId)
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entradaJson))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/movimentacao/entrada/{id}", movimentacaoId)
                        .cookie(authCookie, csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entradaJson))
                .andExpect(status().isOk());
    }

    @Test
    void deleteEntradaExigeTokenCsrf() throws Exception {
        Integer movimentacaoId = criarEntrada();
        Cookie csrfCookie = obterCsrfCookie();

        mockMvc.perform(delete("/movimentacao/entrada/{id}", movimentacaoId)
                        .cookie(authCookie))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/movimentacao/entrada/{id}", movimentacaoId)
                        .cookie(authCookie, csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue()))
                .andExpect(status().isNoContent());
    }

    private Cookie obterCsrfCookie() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/csrf").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn()
                ;
        return ultimoCookieCsrf(resultado);
    }

    private Cookie ultimoCookieCsrf(MvcResult resultado) {
        Cookie[] cookies = resultado.getResponse().getCookies();
        for (int indice = cookies.length - 1; indice >= 0; indice--) {
            Cookie cookie = cookies[indice];
            if ("XSRF-TOKEN".equals(cookie.getName()) && !cookie.getValue().isEmpty()) {
                return cookie;
            }
        }
        throw new IllegalStateException("A resposta não definiu um cookie XSRF-TOKEN válido");
    }

    private Integer criarEntrada() throws Exception {
        Cookie csrfCookie = obterCsrfCookie();
        mockMvc.perform(post("/movimentacao/entrada")
                        .cookie(authCookie, csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entradaJson))
                .andExpect(status().isCreated());

        Movimentacao movimentacao = movimentacaoRepository.findAll().getLast();
        return movimentacao.getId();
    }
}
