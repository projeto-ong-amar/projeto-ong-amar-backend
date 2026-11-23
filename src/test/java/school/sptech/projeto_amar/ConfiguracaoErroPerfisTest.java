package school.sptech.projeto_amar;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguracaoErroPerfisTest {
    @ParameterizedTest
    @ValueSource(strings = {"dev", "prod"})
    void perfilRealNaoHabilitaDetalhesInternos(String perfil) {
        // Carrega os arquivos de produção via ConfigData, não application.properties de teste.
        // Não inicializa banco, correio ou serviços externos.
        new ApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withPropertyValues("spring.config.location=file:src/main/resources/",
                        "spring.profiles.active=" + perfil)
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    var environment = context.getEnvironment();
                    assertArrayEquals(new String[] {perfil}, environment.getActiveProfiles());
                    for (String chave : new String[] {"include-stacktrace", "include-message", "include-binding-errors"}) {
                        assertEquals("never", environment.getProperty("server.error." + chave), perfil + ": " + chave);
                    }
                    assertEquals("false", environment.getProperty("server.error.include-exception"), perfil);
                });
    }
}
