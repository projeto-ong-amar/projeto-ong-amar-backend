package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import school.sptech.projeto_amar.dto.dashboard.AlimentoRiscoVencimentoResponseDTO;
import school.sptech.projeto_amar.dto.dashboard.EstoqueProdutoAtualResponseDTO;
import school.sptech.projeto_amar.dto.dashboard.EstoquePorTipoResponseDTO;
import school.sptech.projeto_amar.dto.dashboard.EstoqueFechamentoMensalResponseDTO;
import school.sptech.projeto_amar.dto.dashboard.MovimentacaoMensalResponseDTO;
import school.sptech.projeto_amar.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para indicadores e gráficos de estoque")
public class DashboardController {
  private DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  private LocalDate calcDataInicio(LocalDate dataProximaDoacao) {
    return dataProximaDoacao.minusDays(30);
  }

  @GetMapping("/kpi-estoque-atual")
  @Operation(summary = "KPI de estoque atual", description = "Retorna o total de itens em estoque considerando o intervalo de 30 dias que termina na data da próxima doação. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Indicador retornado com sucesso"),
      @ApiResponse(responseCode = "400", description = "Dados inválidos"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<Integer> getEstoqueAtual(
      @Parameter(description = "Data da próxima doação (fim do período analisado)")
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataProximaDoacao) {

    LocalDate dataInicio = calcDataInicio(dataProximaDoacao);
    LocalDate dataFim = dataProximaDoacao;

    return ResponseEntity.ok(dashboardService.getEstoqueAtual(dataInicio, dataFim));
  }

  @GetMapping("/kpi-risco-vencimento")
  @Operation(summary = "KPI de itens com risco de vencimento", description = "Retorna a quantidade de lotes com risco de vencimento considerando o intervalo de 30 dias que termina na data da próxima doação. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Indicador retornado com sucesso"),
      @ApiResponse(responseCode = "400", description = "Dados inválidos"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<Integer> getAlimentosComRiscoDeVencimento(
      @Parameter(description = "Data da próxima doação (fim do período analisado)")
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataProximaDoacao) {
    LocalDate dataInicio = calcDataInicio(dataProximaDoacao);
    LocalDate dataFim = dataProximaDoacao;
    return ResponseEntity.ok(dashboardService.getAlimentosComRiscoDeVencimento(dataInicio, dataFim));
  }

  @GetMapping("/modal-kpi-risco-vencimento")
  @Operation(summary = "Listar itens com risco de vencimento", description = "Retorna os lotes com quantidade em estoque que vencem entre hoje e os próximos 30 dias, incluindo as duas datas, com produto, categoria, quantidade e validade. O período é fixo e usa a data local da máquina que executa o backend. Não requer parâmetros de data. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<List<AlimentoRiscoVencimentoResponseDTO>> listarAlimentosComRiscoDeVencimento() {
    return ResponseEntity.ok(
        dashboardService.listarAlimentosComRiscoDeVencimento());
  }

  @GetMapping("/kpi-doacoes-recebidas")
  @Operation(summary = "KPI de doações recebidas", description = "Retorna a quantidade total de itens recebidos (entradas) no intervalo de 30 dias que termina na data da próxima doação. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Indicador retornado com sucesso"),
      @ApiResponse(responseCode = "400", description = "Dados inválidos"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<Integer> getDoacoesRecebidas(
      @Parameter(description = "Data da próxima doação (fim do período analisado)")
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataProximaDoacao) {
    LocalDate dataInicio = calcDataInicio(dataProximaDoacao);
    LocalDate dataFim = dataProximaDoacao;
    return ResponseEntity.ok(dashboardService.getDoacoesRecebidas(dataInicio, dataFim));
  }

  @GetMapping("/kpi-itens-distribuidos")
  @Operation(summary = "KPI de itens distribuídos", description = "Retorna a quantidade total de itens distribuídos (saídas) no intervalo de 30 dias que termina na data da próxima doação. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Indicador retornado com sucesso"),
      @ApiResponse(responseCode = "400", description = "Dados inválidos"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<Integer> getItensDistribuidos(
      @Parameter(description = "Data da próxima doação (fim do período analisado)")
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataProximaDoacao) {
    LocalDate dataInicio = calcDataInicio(dataProximaDoacao);
    LocalDate dataFim = dataProximaDoacao;
    return ResponseEntity.ok(dashboardService.getItensDistribuidos(dataInicio, dataFim));
  }

  @GetMapping("/movimentacoes-por-mes")
  @Operation(summary = "Movimentações por mês", description = "Retorna o total de entradas e saídas agrupadas por mês no ano corrente, incluindo meses sem movimentação. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<List<MovimentacaoMensalResponseDTO>> listarMovimentacoesPorMes() {
    return ResponseEntity.ok(dashboardService.listarMovimentacoesPorMes());
  }

  @GetMapping("/estoque-por-tipo")
  @Operation(summary = "Estoque por tipo de produto", description = "Retorna a quantidade de itens em estoque agrupada por tipo de produto, com o percentual sobre o estoque total. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<List<EstoquePorTipoResponseDTO>> listarEstoquePorTipo() {
    return ResponseEntity.ok(dashboardService.listarEstoquePorTipo());
  }

  @GetMapping("/estoque-atual-por-produto")
  @Operation(summary = "Estoque atual por produto", description = "Retorna o nome, a categoria e a quantidade atual em estoque de cada produto ativo. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<List<EstoqueProdutoAtualResponseDTO>> listarEstoqueAtualPorProduto() {
    return ResponseEntity.ok(dashboardService.listarEstoqueAtualPorProduto());
  }

  @GetMapping("/estoque-fechamento-por-mes")
  @Operation(summary = "Fechamento de estoque por mês", description = "Retorna o saldo acumulado de fechamento do estoque ao final de cada mês no ano corrente. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<List<EstoqueFechamentoMensalResponseDTO>> listarEstoqueFechamentoPorMes() {
    return ResponseEntity.ok(dashboardService.listarEstoqueFechamentoPorMes());
  }

}