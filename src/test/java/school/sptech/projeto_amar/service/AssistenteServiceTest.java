package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import school.sptech.projeto_amar.exception.assistenteException.ErroAoChamarIaException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AssistenteServiceTest {

    @InjectMocks
    private AssistenteService service;

    @Test
    @DisplayName("Deve lançar exceção quando Ollama não está disponível (conexão recusada)")
    void deveLancarExcecaoQuandoOllamaIndisponivel() {
        // O Ollama não estará rodando durante os testes, então a chamada HTTP falhará
        assertThrows(ErroAoChamarIaException.class, () -> service.responder("Olá"));
    }

    @Test
    @DisplayName("Deve lançar exceção com mensagem ao falhar comunicação com Ollama")
    void deveLancarExcecaoComMensagem() {
        ErroAoChamarIaException ex = assertThrows(ErroAoChamarIaException.class,
                () -> service.responder("Como cadastrar produto?"));
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("Erro ao chamar IA"));
    }

    @Test
    @DisplayName("Deve lançar exceção para qualquer pergunta quando Ollama indisponível")
    void deveLancarExcecaoParaQualquerPergunta() {
        assertThrows(ErroAoChamarIaException.class,
                () -> service.responder("Qual o estoque disponível?"));
    }

    @Test
    @DisplayName("Deve lançar exceção mesmo com pergunta vazia quando Ollama indisponível")
    void deveLancarExcecaoComPerguntaVazia() {
        assertThrows(ErroAoChamarIaException.class, () -> service.responder(""));
    }
}
