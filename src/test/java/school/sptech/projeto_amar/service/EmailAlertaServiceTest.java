package school.sptech.projeto_amar.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailAlertaServiceTest {
  @Test
  void enviaEmailHtmlComAlternativaEmTexto() throws Exception {
    JavaMailSender mailSender = mock(JavaMailSender.class);
    MimeMessage mensagem = new MimeMessage(Session.getInstance(new Properties()));
    when(mailSender.createMimeMessage()).thenReturn(mensagem);
    EmailAlertaService service = new EmailAlertaService(mailSender, "alertas@gmail.com");

    service.enviar("destino@gmail.com", "Alerta", "Texto simples", "<p>HTML</p>");

    assertEquals("alertas@gmail.com", mensagem.getFrom()[0].toString());
    assertEquals("Alerta", mensagem.getSubject());
    verify(mailSender).send(mensagem);
  }

  @Test
  void naoEnviaQuandoRemetenteNaoEstaConfigurado() {
    EmailAlertaService service = new EmailAlertaService("smtp.gmail.com", 587, "", "", "");

    assertThrows(IllegalStateException.class,
        () -> service.enviar("destino@gmail.com", "Alerta", "Texto", "<p>HTML</p>"));
  }
}
