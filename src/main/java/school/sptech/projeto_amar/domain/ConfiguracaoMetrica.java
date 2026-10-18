package school.sptech.projeto_amar.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracao_metrica")
public class ConfiguracaoMetrica {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "estoque_baixo", nullable = false)
  private Integer estoqueBaixo;

  @Column(name = "risco_vencimento_dias", nullable = false)
  private Integer riscoVencimentoDias;

  @Column(name = "vencimento_proximo_dias", nullable = false)
  private Integer vencimentoProximoDias;

  @Column(name = "sem_movimentacao_dias", nullable = false)
  private Integer semMovimentacaoDias;

  @Column(name = "periodo_indicadores_dias", nullable = false)
  private Integer periodoIndicadoresDias;

  @Column(name = "data_atualizacao", nullable = false)
  private LocalDateTime dataAtualizacao;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_usuario_atualizacao")
  private Usuario usuarioAtualizacao;

  @PrePersist
  @PreUpdate
  void atualizarData() {
    dataAtualizacao = LocalDateTime.now();
  }

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public Integer getEstoqueBaixo() { return estoqueBaixo; }
  public void setEstoqueBaixo(Integer estoqueBaixo) { this.estoqueBaixo = estoqueBaixo; }
  public Integer getRiscoVencimentoDias() { return riscoVencimentoDias; }
  public void setRiscoVencimentoDias(Integer riscoVencimentoDias) { this.riscoVencimentoDias = riscoVencimentoDias; }
  public Integer getVencimentoProximoDias() { return vencimentoProximoDias; }
  public void setVencimentoProximoDias(Integer vencimentoProximoDias) { this.vencimentoProximoDias = vencimentoProximoDias; }
  public Integer getSemMovimentacaoDias() { return semMovimentacaoDias; }
  public void setSemMovimentacaoDias(Integer semMovimentacaoDias) { this.semMovimentacaoDias = semMovimentacaoDias; }
  public Integer getPeriodoIndicadoresDias() { return periodoIndicadoresDias; }
  public void setPeriodoIndicadoresDias(Integer periodoIndicadoresDias) { this.periodoIndicadoresDias = periodoIndicadoresDias; }
  public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
  public Usuario getUsuarioAtualizacao() { return usuarioAtualizacao; }
  public void setUsuarioAtualizacao(Usuario usuarioAtualizacao) { this.usuarioAtualizacao = usuarioAtualizacao; }
}
