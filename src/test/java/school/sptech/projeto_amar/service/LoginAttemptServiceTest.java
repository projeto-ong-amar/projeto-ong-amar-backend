package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.LockedException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceTest {
    private static final String IP = "127.0.0.1";
    private static final String EMAIL = "usuario@email.com";

    @Test
    void bloqueiaAposCincoFalhas() {
        LoginAttemptService service = new LoginAttemptService();

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            service.registrarFalha(IP, EMAIL);
        }

        assertThrows(LockedException.class, () -> service.verificarPermitido(IP, EMAIL));
    }

    @Test
    void limpaTentativasAposLoginValido() {
        LoginAttemptService service = new LoginAttemptService();
        service.registrarFalha(IP, EMAIL);

        service.limpar(IP, EMAIL);

        assertDoesNotThrow(() -> service.verificarPermitido(IP, EMAIL));
    }
}
