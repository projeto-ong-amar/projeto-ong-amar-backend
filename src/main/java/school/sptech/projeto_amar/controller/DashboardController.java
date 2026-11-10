package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Positive;

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

  @GetMapping("/kpi-vencimento-7-dias")
  @Operation(summary = "Unidades com vencimento em até 7 dias", description = "Soma as unidades em estoque dos produtos ativos com validade de hoje até hoje + 7 dias, inclusive. Usa a data local do backend. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Quantidade de unidades retornada"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<Long> getUnidadesVencendoEm7Dias() {
    return ResponseEntity.ok(dashboardService.getUnidadesVencendoEm7Dias());
  }

  @GetMapping("/kpi-estoque-baixo")
  @Operation(summary = "Unidades de produtos com estoque baixo", description = "Soma as unidades dos produtos ativos cujo estoque total, somando todos os lotes, é menor que o limite informado. Estoque zerado contribui com zero unidades. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Quantidade de unidades retornada"),
      @ApiResponse(responseCode = "400", description = "Limite ausente ou inválido"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<Long> getUnidadesComEstoqueBaixo(
      @Parameter(description = "Limite exclusivo de unidades por produto, maior que zero", example = "10")
      @RequestParam @Positive Integer limite) {
    return ResponseEntity.ok(dashboardService.getUnidadesComEstoqueBaixo(limite));
  }

  @GetMapping("/kpi-doacoes-semana")
  @Operation(summary = "Doações registradas nesta semana", description = "Conta os registros de entrada com data entre a segunda-feira desta semana e hoje, inclusive. Não soma unidades recebidas. Usa a data local do backend. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Quantidade de registros retornada"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<Long> getDoacoesRegistradasNaSemana() {
    return ResponseEntity.ok(dashboardService.getDoacoesRegistradasNaSemana());
  }

  @GetMapping("/kpi-sem-movimentacao-60-dias")
  @Operation(summary = "Unidades sem movimentação nos últimos 60 dias", description = "Soma o estoque atual dos produtos ativos sem entrada, saída ou perda nos últimos 60 dias (hoje e os 59 dias anteriores). Inclui produtos que nunca tiveram movimentação. Usa a data local do backend. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Quantidade de unidades retornada"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<Long> getUnidadesSemMovimentacaoEm60Dias() {
    return ResponseEntity.ok(dashboardService.getUnidadesSemMovimentacaoEm60Dias());
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
  @Operation(summary = "KPI de itens com risco de vencimento", description = "Retorna a quantidade de lotes ativos com quantidade em estoque que vencem entre hoje e hoje + dias de risco configurados, incluindo as duas datas. Usa a mesma regra da listagem /modal-kpi-risco-vencimento. Requer autenticação")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Indicador retornado com sucesso"),
      @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
  })
  public ResponseEntity<Integer> getAlimentosComRiscoDeVencimento() {
    return ResponseEntity.ok(dashboardService.getAlimentosComRiscoDeVencimento());
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