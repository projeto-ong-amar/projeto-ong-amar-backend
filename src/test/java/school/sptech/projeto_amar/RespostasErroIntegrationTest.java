package school.sptech.projeto_amar;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.dto.usuario.UsuarioDetalhesDTO;
import school.sptech.projeto_amar.repository.UsuarioRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
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
