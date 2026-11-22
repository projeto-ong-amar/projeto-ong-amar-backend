package school.sptech.projeto_amar.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.sptech.projeto_amar.domain.ConfiguracaoAlertaDashboard;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.configuracao.ConfiguracaoAlertaDashboardDTO;
import school.sptech.projeto_amar.dto.dashboard.AlertaDashboardDTO;
import school.sptech.projeto_amar.repository.*;

@Service
public class AlertaDashboardService {
  private final ConfiguracaoAlertaDashboardRepository configuracaoRepository;
  private final ConfiguracaoNotificacaoService configuracaoService;
  private final ProdutoLoteRepository loteRepository;
  private final ProdutoRepository produtoRepository;
  private final MovimentacaoRepository movimentacaoRepository;

  public AlertaDashboardService(ConfiguracaoAlertaDashboardRepository configuracaoRepository,
      ConfiguracaoNotificacaoService configuracaoService, ProdutoLoteRepository loteRepository,
      ProdutoRepository produtoRepository, MovimentacaoRepository movimentacaoRepository) {
    this.configuracaoRepository = configuracaoRepository;
    this.configuracaoService = configuracaoService;
    this.loteRepository = loteRepository;
    this.produtoRepository = produtoRepository;
    this.movimentacaoRepository = movimentacaoRepository;
  }

  @Transactional(readOnly = true)
  public ConfiguracaoAlertaDashboardDTO obterConfiguracao() {
    return paraDTO(configuracaoRepository.findById(1).orElseGet(ConfiguracaoAlertaDashboard::new));
  }

  @Transactional
  public ConfiguracaoAlertaDashboardDTO atualizarConfiguracao(ConfiguracaoAlertaDashboardDTO dto) {
    var configuracao = configuracaoRepository.findById(1).orElseGet(ConfiguracaoAlertaDashboard::new);
    configuracao.setVencimentoProximo(dto.vencimentoProximo());
    configuracao.setEstoqueBaixo(dto.estoqueBaixo());
    configuracao.setSemDoacoes(dto.semDoacoes());
    configuracao.setSemMovimentacao(dto.semMovimentacao());
    return paraDTO(configuracaoRepository.save(configuracao));
  }

  @Transactional(readOnly = true)
  public List<AlertaDashboardDTO> listarAlertas() {
    var preferencias = obterConfiguracao();
    var metricas = configuracaoService.obterMetricas();
    var hoje = LocalDate.now();
    List<AlertaDashboardDTO> alertas = new ArrayList<>();
    if (preferencias.vencimentoProximo()) {
      long quantidade = loteRepository.countAlimentosComRiscoDeVencimento(
          hoje, hoje.plusDays(metricas.vencimentoProximoDias()));
      if (quantidade > 0) {
        alertas.add(new AlertaDashboardDTO("perigo",
            "Lotes com vencimento em até " + metricas.vencimentoProximoDias() + " dias: " + quantidade));
      }
    }
    if (preferencias.estoqueBaixo()) {
      long quantidade = produtoRepository.countProdutosComEstoqueBaixo(metricas.estoqueBaixo());
      if (quantidade > 0) {
        alertas.add(new AlertaDashboardDTO("aviso",
            "Produtos com estoque abaixo de " + metricas.estoqueBaixo() + " unidades: " + quantidade));
      }
    }
    if (preferencias.semDoacoes()) {
      var inicio = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
      if (movimentacaoRepository.countByTipoAndDataBetween(TipoMovimentacao.ENTRADA, inicio, hoje) == 0) {
        alertas.add(new AlertaDashboardDTO("info", "Nenhuma doação registrada esta semana"));
      }
    }
    if (preferencias.semMovimentacao()) {
      long quantidade = loteRepository.countProdutosSemMovimentacaoNoPeriodo(
          hoje.minusDays(metricas.semMovimentacaoDias() - 1L), hoje);
      if (quantidade > 0) {
        alertas.add(new AlertaDashboardDTO("neutro",
            "Produtos em estoque sem movimentação nos últimos " + metricas.semMovimentacaoDias()
                + " dias: " + quantidade));
      }
    }
    return alertas;
  }

  private ConfiguracaoAlertaDashboardDTO paraDTO(ConfiguracaoAlertaDashboard configuracao) {
    return new ConfiguracaoAlertaDashboardDTO(configuracao.isVencimentoProximo(),
        configuracao.isEstoqueBaixo(), configuracao.isSemDoacoes(), configuracao.isSemMovimentacao());
  }
}
