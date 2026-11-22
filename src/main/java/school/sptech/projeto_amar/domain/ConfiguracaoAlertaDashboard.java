package school.sptech.projeto_amar.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracao_alerta_dashboard")
public class ConfiguracaoAlertaDashboard {
  // Configuração global única, não uma preferência de e-mail por usuário.
  @Id
  private Integer id = 1;
  @Column(name = "vencimento_proximo", nullable = false)
  private boolean vencimentoProximo = true;
  @Column(name = "estoque_baixo", nullable = false)
  private boolean estoqueBaixo = true;
  @Column(name = "sem_doacoes", nullable = false)
  private boolean semDoacoes = true;
  @Column(name = "sem_movimentacao", nullable = false)
  private boolean semMovimentacao = true;
  @Column(name = "data_atualizacao", nullable = false)
  private LocalDateTime dataAtualizacao;

  @PrePersist
  @PreUpdate
  void atualizarData() { dataAtualizacao = LocalDateTime.now(); }

  public Integer getId() { return id; }
  public boolean isVencimentoProximo() { return vencimentoProximo; }
  public void setVencimentoProximo(boolean valor) { vencimentoProximo = valor; }
  public boolean isEstoqueBaixo() { return estoqueBaixo; }
  public void setEstoqueBaixo(boolean valor) { estoqueBaixo = valor; }
  public boolean isSemDoacoes() { return semDoacoes; }
  public void setSemDoacoes(boolean valor) { semDoacoes = valor; }
  public boolean isSemMovimentacao() { return semMovimentacao; }
  public void setSemMovimentacao(boolean valor) { semMovimentacao = valor; }
}
