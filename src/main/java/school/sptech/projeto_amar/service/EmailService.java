package school.sptech.projeto_amar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  private final MailSender mailSender;
  private final String remetente;

  public EmailService(MailSender mailSender,
      @Value("${app.email.remetente}") String remetente) {
    this.mailSender = mailSender;
    this.remetente = remetente;
  }

  public void enviarRecuperacaoSenha(String destinatario, String nome, String linkRedefinicao) {
    SimpleMailMessage mensagem = new SimpleMailMessage();
    mensagem.setFrom(remetente);
    mensagem.setTo(destinatario);
    mensagem.setSubject("Recuperacao de senha - Projeto Amar");
    mensagem.setText("Ola, " + nome + ".\n\n"
        + "Recebemos uma solicitacao para redefinir sua senha. "
        + "Use o link abaixo em ate 15 minutos:\n"
        + linkRedefinicao + "\n\n"
        + "Se voce nao solicitou a redefinicao, ignore este e-mail.");
    mailSender.send(mensagem);
  }
}