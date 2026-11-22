package school.sptech.projeto_amar.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;
import school.sptech.projeto_amar.service.LoginAttemptService;
import school.sptech.projeto_amar.service.UsuarioService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService service;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private UsuarioController controller;

    private MockMvc mockMvc;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        usuario = new Usuario();
        usuario.setId(42);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setTipoUsuario(TipoUsuario.VOLUNTARIO);
        usuario.setStatus("ATIVO");
        usuario.setSenha("senhaHasheada");
    }

    @Test
    @DisplayName("GET /usuarios deve incluir o id e preservar os campos públicos sem expor a senha")
    void deveListarUsuariosComId() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(usuario));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(42))
                .andExpect(jsonPath("$[0].nome").value("João Silva"))
                .andExpect(jsonPath("$[0].email").value("joao@email.com"))
                .andExpect(jsonPath("$[0].tipoUsuario").value("VOLUNTARIO"))
                .andExpect(jsonPath("$[0].status").value("ATIVO"))
                .andExpect(jsonPath("$[0].senha").doesNotExist());
    }

    @Test
    @DisplayName("GET /usuarios com filtro por tipo também deve incluir o id")
    void deveListarUsuariosPorTipoComId() throws Exception {
        when(service.listarPorTipo("VOLUNTARIO")).thenReturn(List.of(usuario));

        mockMvc.perform(get("/usuarios").param("tipoUsuario", "voluntario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(42));
    }
}
