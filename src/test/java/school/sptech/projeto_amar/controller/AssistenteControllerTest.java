package school.sptech.projeto_amar.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import school.sptech.projeto_amar.config.GerenciadorTokenJwt;
import school.sptech.projeto_amar.exception.assistenteException.ErroAoChamarIaException;
import school.sptech.projeto_amar.service.AssistenteService;
import school.sptech.projeto_amar.service.AutenticacaoService;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssistenteController.class)
class AssistenteControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AssistenteService service;
    @MockitoBean
    private AutenticacaoService autenticacaoService;
    @MockitoBean
    private GerenciadorTokenJwt gerenciadorTokenJwt;

    @Test
    @DisplayName("200 - resposta com sucesso")
    @WithMockUser
    void deveRetornar200() throws Exception {
        when(service.responder(anyString())).thenReturn("{\"titulo\":\"Teste\",\"response\":[\"passo1\"]}");
        mockMvc.perform(post("/assistente").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("\"Como cadastrar?\""))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("400 - erro ao chamar IA")
    @WithMockUser
    void deveRetornar400() throws Exception {
        when(service.responder(anyString())).thenThrow(new ErroAoChamarIaException("Erro"));
        mockMvc.perform(post("/assistente").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("\"Pergunta\""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 - body vazio")
    @WithMockUser
    void deveRetornar400BodyVazio() throws Exception {
        mockMvc.perform(post("/assistente").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(""))
                .andExpect(status().isBadRequest());
    }
}
