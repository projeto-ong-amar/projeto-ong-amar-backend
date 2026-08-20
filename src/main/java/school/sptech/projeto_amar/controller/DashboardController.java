package school.sptech.projeto_amar.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import school.sptech.projeto_amar.dto.dashboard.AlimentoRiscoVencimentoResponseDTO;
import school.sptech.projeto_amar.dto.dashboard.MovimentacaoMensalResponseDTO;
import school.sptech.projeto_amar.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
  private DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  private LocalDate calcDataInicio(LocalDate dataProximaDoacao) {
    return dataProximaDoacao.minusDays(30);
  }

  @GetMapping("/kpi-estoque-atual")
  public ResponseEntity<Integer> getEstoqueAtual(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataProximaDoacao) {

    LocalDate dataInicio = calcDataInicio(dataProximaDoacao);
    LocalDate dataFim = dataProximaDoacao;

    return ResponseEntity.ok(dashboardService.getEstoqueAtual(dataInicio, dataFim));
  }

  @GetMapping("/kpi-risco-vencimento")
  public ResponseEntity<Integer> getAlimentosComRiscoDeVencimento(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataProximaDoacao) {
    LocalDate dataInicio = calcDataInicio(dataProximaDoacao);
    LocalDate dataFim = dataProximaDoacao;
    return ResponseEntity.ok(dashboardService.getAlimentosComRiscoDeVencimento(dataInicio, dataFim));
  }

  @GetMapping("/modal-kpi-risco-vencimento")
  public ResponseEntity<List<AlimentoRiscoVencimentoResponseDTO>> listarAlimentosComRiscoDeVencimento(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataProximaDoacao) {
    LocalDate dataInicio = calcDataInicio(dataProximaDoacao);
    LocalDate dataFim = dataProximaDoacao;

    return ResponseEntity.ok(
        dashboardService.listarAlimentosComRiscoDeVencimento(dataInicio, dataFim));
  }

  @GetMapping("/kpi-doacoes-recebidas")
  public ResponseEntity<Integer> getDoacoesRecebidas(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataProximaDoacao) {
    LocalDate dataInicio = calcDataInicio(dataProximaDoacao);
    LocalDate dataFim = dataProximaDoacao;
    return ResponseEntity.ok(dashboardService.getDoacoesRecebidas(dataInicio, dataFim));
  }

  @GetMapping("/kpi-itens-distribuidos")
  public ResponseEntity<Integer> getItensDistribuidos(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataProximaDoacao) {
    LocalDate dataInicio = calcDataInicio(dataProximaDoacao);
    LocalDate dataFim = dataProximaDoacao;
    return ResponseEntity.ok(dashboardService.getItensDistribuidos(dataInicio, dataFim));
  }

  @GetMapping("/movimentacoes-por-mes")
  public ResponseEntity<List<MovimentacaoMensalResponseDTO>> listarMovimentacoesPorMes() {
    return ResponseEntity.ok(dashboardService.listarMovimentacoesPorMes());
  }

}
