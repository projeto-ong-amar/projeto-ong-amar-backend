package school.sptech.projeto_amar;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import school.sptech.projeto_amar.config.GerenciadorTokenJwt;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.Categoria;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.dto.usuario.UsuarioDetalhesDTO;
import school.sptech.projeto_amar.repository.ProdutoRepository;
import school.sptech.projeto_amar.repository.UsuarioRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.ai.google.genai.api-key=test-key")
@AutoConfigureMockMvc
class ProdutoNomeIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private GerenciadorTokenJwt jwt;
    @Autowired private ProdutoRepository repository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Cookie autenticacao;
    private Cookie csrf;
    private Produto original;

    @BeforeEach
    void setUp() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Administrador");
        usuario.setEmail(UUID.randomUUID() + "@example.com");
        usuario.setSenha("hash-nao-usado-no-teste");
        usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        usuario.setStatus("ATIVO");
        var detalhes = new UsuarioDetalhesDTO(usuarioRepository.save(usuario));
        autenticacao = new Cookie("authToken", jwt.generateToken(
                new UsernamePasswordAuthenticationToken(detalhes, null, detalhes.getAuthorities())));
        csrf = mockMvc.perform(get("/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(csrf);
        original = salvarProduto("ATIVO");
    }

    @Test
    void rejeitaNomeDeOutroProdutoAtivoSemAlterarDados() throws Exception {
        Produto outro = salvarProduto("ATIVO");
        mockMvc.perform(put("/produtos/" + original.getId())
                .cookie(autenticacao, csrf).header("X-XSRF-TOKEN", csrf.getValue())
                .contentType(MediaType.APPLICATION_JSON).content(corpo(outro.getNome())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe um produto com esse nome!"));

        Produto salvo = repository.findById(original.getId()).orElseThrow();
        assertEquals(original.getNome(), salvo.getNome());
        assertEquals("kg", salvo.getUnidadeMedida());
        assertEquals(Categoria.ALIMENTOS, salvo.getCategoria());
        assertEquals(outro.getNome(), repository.findById(outro.getId()).orElseThrow().getNome());
        mockMvc.perform(get("/produtos").cookie(autenticacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + original.getId() + ")].nome")
                        .value(org.hamcrest.Matchers.contains(original.getNome())));
    }

    @Test
    void permiteManterProprioNomeEAlterarOutrosDados() throws Exception {
        atualizarComSucesso(original.getNome());
    }

    @Test
    void permiteNomeNovo() throws Exception {
        atualizarComSucesso("Novo " + UUID.randomUUID());
    }

    @Test
    void permiteReutilizarNomeDeProdutoInativo() throws Exception {
        atualizarComSucesso(salvarProduto("INATIVO").getNome());
    }

    @Test
    void cadastroContinuaRejeitandoNomeAtivoDuplicado() throws Exception {
        mockMvc.perform(post("/produtos")
                .cookie(autenticacao, csrf).header("X-XSRF-TOKEN", csrf.getValue())
                .contentType(MediaType.APPLICATION_JSON).content(corpo(original.getNome())))
                .andExpect(status().isConflict());
    }

    private void atualizarComSucesso(String nome) throws Exception {
        mockMvc.perform(put("/produtos/" + original.getId())
                .cookie(autenticacao, csrf).header("X-XSRF-TOKEN", csrf.getValue())
                .contentType(MediaType.APPLICATION_JSON).content(corpo(nome)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.unidadeMedida").value("UN"));
        Produto salvo = repository.findById(original.getId()).orElseThrow();
        assertEquals(nome, salvo.getNome());
        assertEquals("UN", salvo.getUnidadeMedida());
        assertEquals(Categoria.PRODUTOS_DE_HIGIENE, salvo.getCategoria());
    }

    private Produto salvarProduto(String status) {
        Produto produto = new Produto();
        produto.setNome("Produto " + UUID.randomUUID());
        produto.setUnidadeMedida("kg");
        produto.setCategoria(Categoria.ALIMENTOS);
        produto.setStatus(status);
        return repository.save(produto);
    }

    private String corpo(String nome) {
        return """
                {"nome":"%s","unidadeMedida":"UN","categoria":"PRODUTOS DE HIGIENE"}
                """.formatted(nome);
    }
}
