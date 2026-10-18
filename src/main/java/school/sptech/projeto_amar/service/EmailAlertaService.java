package school.sptech.projeto_amar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailAlertaService {
  private final JavaMailSender mailSender;
  private final String remetente;
  private final boolean configurado;

  @Autowired
  public EmailAlertaService(
      @Value("${alert.mail.host:smtp.gmail.com}") String host,
      @Value("${alert.mail.port:587}") int port,
      @Value("${alert.mail.username:}") String usuario,
      @Value("${alert.mail.password:}") String senha,
      @Value("${alert.mail.from:}") String remetente) {
    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(host);
    sender.setPort(port);
    sender.setUsername(usuario);
    sender.setPassword(senha);
    sender.setDefaultEncoding("UTF-8");
    Properties propriedades = sender.getJavaMailProperties();
    propriedades.put("mail.smtp.auth", "true");
    propriedades.put("mail.smtp.starttls.enable", "true");
    propriedades.put("mail.smtp.starttls.required", "true");
    propriedades.put("mail.smtp.connectiontimeout", "5000");
    propriedades.put("mail.smtp.timeout", "10000");
    propriedades.put("mail.smtp.writetimeout", "10000");
    this.mailSender = sender;
    this.remetente = remetente;
    this.configurado = StringUtils.hasText(host) && port > 0
        && StringUtils.hasText(usuario) && StringUtils.hasText(senha)
        && StringUtils.hasText(remetente);
  }

  EmailAlertaService(JavaMailSender mailSender, String remetente) {
    this.mailSender = mailSender;
    this.remetente = remetente;
    this.configurado = StringUtils.hasText(remetente);
  }

  public void enviar(String destinatario, String assunto, String texto, String html) {
    if (!configurado) {
      throw new IllegalStateException(
          "SMTP de alertas não configurado: defina ALERT_MAIL_USERNAME, ALERT_MAIL_PASSWORD e ALERT_MAIL_FROM");
    }

    try {
      MimeMessage mensagem = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");
      helper.setFrom(remetente);
      helper.setTo(destinatario);
      helper.setSubject(assunto);
      helper.setText(texto, html);
      mailSender.send(mensagem);
    } catch (MessagingException exception) {
      throw new IllegalStateException("Não foi possível montar o e-mail de alerta", exception);
    }
  }
}
