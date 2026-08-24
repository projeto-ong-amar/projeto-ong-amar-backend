package school.sptech.projeto_amar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_recuperacao_senha")
public class TokenRecuperacaoSenha {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(name = "expira_em", nullable = false)
  private LocalDateTime expiraEm;

  @Column(nullable = false)
  private boolean utilizado;

  @ManyToOne(optional = false)
  @JoinColumn(name = "fk_usuario", nullable = false)
  private Usuario usuario;

  public Long getId() {
    return id;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public void setTokenHash(String tokenHash) {
    this.tokenHash = tokenHash;
  }

  public LocalDateTime getExpiraEm() {
    return expiraEm;
  }

  public void setExpiraEm(LocalDateTime expiraEm) {
    this.expiraEm = expiraEm;
  }

  public boolean isUtilizado() {
    return utilizado;
  }

  public void setUtilizado(boolean utilizado) {
    this.utilizado = utilizado;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }
}