package school.sptech.projeto_amar.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "preferencia_notificacao", uniqueConstraints = {
    @UniqueConstraint(name = "uk_preferencia_notificacao_usuario", columnNames = "fk_usuario")
})
public class PreferenciaNotificacao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "estoque_baixo", nullable = false)
  private boolean estoqueBaixo = true;

  @Column(name = "risco_vencimento", nullable = false)
  private boolean riscoVencimento = true;

  @Column(name = "novas_entradas", nullable = false)
  private boolean novasEntradas = true;

  @Column(name = "novas_saidas", nullable = false)
  private boolean novasSaidas = true;

  @Column(name = "email_notificacao", length = 255)
  private String emailNotificacao;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "fk_usuario", nullable = false)
  private Usuario usuario;

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public boolean isEstoqueBaixo() { return estoqueBaixo; }
  public void setEstoqueBaixo(boolean estoqueBaixo) { this.estoqueBaixo = estoqueBaixo; }
  public boolean isRiscoVencimento() { return riscoVencimento; }
  public void setRiscoVencimento(boolean riscoVencimento) { this.riscoVencimento = riscoVencimento; }
  public boolean isNovasEntradas() { return novasEntradas; }
  public void setNovasEntradas(boolean novasEntradas) { this.novasEntradas = novasEntradas; }
  public boolean isNovasSaidas() { return novasSaidas; }
  public void setNovasSaidas(boolean novasSaidas) { this.novasSaidas = novasSaidas; }
  public String getEmailNotificacao() { return emailNotificacao; }
  public void setEmailNotificacao(String emailNotificacao) { this.emailNotificacao = emailNotificacao; }
  public Usuario getUsuario() { return usuario; }
  public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
