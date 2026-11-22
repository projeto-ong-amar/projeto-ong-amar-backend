package school.sptech.projeto_amar;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import school.sptech.projeto_amar.config.GerenciadorTokenJwt;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.dto.usuario.UsuarioDetalhesDTO;
import school.sptech.projeto_amar.repository.UsuarioRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.ai.google.genai.api-key=test-key")
@AutoConfigureMockMvc
class AlteracaoSenhaIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository repository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private GerenciadorTokenJwt jwt;

    private Usuario usuario;
    private Cookie authCookie;
    private Cookie csrfCookie;

    @BeforeEach
    void setUp() throws Exception {
        usuario = new Usuario();
        usuario.setNome("Usuario Senha");
        usuario.setEmail(UUID.randomUUID() + "@example.com");
        usuario.setSenha(passwordEncoder.encode("Senha@123"));
        usuario.setTipoUsuario(TipoUsuario.VOLUNTARIO);
        usuario.setStatus("ATIVO");
        usuario = repository.save(usuario);
        autenticar();
        csrfCookie = mockMvc.perform(get("/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(csrfCookie);
    }

    private void autenticar() {
        var detalhes = new UsuarioDetalhesDTO(usuario);
        authCookie = new Cookie("authToken", jwt.generateToken(
                new UsernamePasswordAuthenticationToken(detalhes, null, detalhes.getAuthorities())));
    }

    @Test
    void meRetornaIdSemSenha() throws Exception {
        mockMvc.perform(get("/usuarios/me").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuario.getId()))
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void voluntarioAlteraPropriaSenhaEInvalidaSessao() throws Exception {
        alterar("Senha@123", "NovaSenha@1", 204);
        var salvo = repository.findById(usuario.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("NovaSenha@1", salvo.getSenha()));
        assertFalse(passwordEncoder.matches("Senha@123", salvo.getSenha()));
        mockMvc.perform(get("/usuarios/me").cookie(authCookie)).andExpect(status().isUnauthorized());
    }

    @Test
    void rejeitaSenhaAtualErradaSemModificarConta() throws Exception {
        alterar("incorreta", "NovaSenha@1", 400);
        assertEquals(usuario.getSenha(), repository.findById(usuario.getId()).orElseThrow().getSenha());
        mockMvc.perform(get("/usuarios/me").cookie(authCookie)).andExpect(status().isOk());
    }

    @Test
    void validaCamposDaSenha() throws Exception {
        alterar("", "NovaSenha@1", 400);
        alterar("Senha@123", "fraca", 400);
        alterar("Senha@123", "SenhaMuitoLonga@12345678901234567890", 400);
    }

    @Test
    void exigeAutenticacaoECsrf() throws Exception {
        String corpo = "{\"senhaAtual\":\"Senha@123\",\"novaSenha\":\"NovaSenha@1\"}";
        mockMvc.perform(patch("/usuarios/me/senha").contentType(MediaType.APPLICATION_JSON)
                .cookie(csrfCookie).header("X-XSRF-TOKEN", csrfCookie.getValue()).content(corpo))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/usuarios/me/senha").cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorTambemPodeAlterarPropriaSenha() throws Exception {
        usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        usuario = repository.save(usuario);
        autenticar();
        alterar("Senha@123", "NovaSenha@1", 204);
        assertTrue(passwordEncoder.matches("NovaSenha@1",
                repository.findById(usuario.getId()).orElseThrow().getSenha()));
    }

    @Test
    void putSemSenhaPreservaHash() throws Exception {
        usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        usuario = repository.save(usuario);
        autenticar();
        mockMvc.perform(put("/usuarios/" + usuario.getId())
                .cookie(authCookie, csrfCookie).header("X-XSRF-TOKEN", csrfCookie.getValue())
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {"nome":"Novo Nome","email":"novo@example.com",
                     "tipoUsuario":"ADMINISTRADOR","status":"ATIVO"}
                    """)).andExpect(status().isOk());
        assertEquals(usuario.getSenha(), repository.findById(usuario.getId()).orElseThrow().getSenha());
    }

    @Test
    void putAdministrativoRejeitaSenha() throws Exception {
        usuario.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        usuario = repository.save(usuario);
        autenticar();
        mockMvc.perform(put("/usuarios/" + usuario.getId())
                .cookie(authCookie, csrfCookie).header("X-XSRF-TOKEN", csrfCookie.getValue())
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {"nome":"Novo Nome","email":"novo@example.com","senha":"NovaSenha@1",
                     "tipoUsuario":"ADMINISTRADOR","status":"ATIVO"}
                    """)).andExpect(status().isBadRequest());
        assertEquals(usuario.getSenha(), repository.findById(usuario.getId()).orElseThrow().getSenha());
    }

    private void alterar(String atual, String nova, int statusEsperado) throws Exception {
        mockMvc.perform(patch("/usuarios/me/senha")
                .cookie(authCookie, csrfCookie).header("X-XSRF-TOKEN", csrfCookie.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"senhaAtual\":\"" + atual + "\",\"novaSenha\":\"" + nova + "\"}"))
                .andExpect(status().is(statusEsperado));
    }
}
