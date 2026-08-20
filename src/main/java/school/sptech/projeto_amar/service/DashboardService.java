package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.dashboard.AlimentoRiscoVencimentoResponseDTO;
import school.sptech.projeto_amar.dto.dashboard.MovimentacaoMensalResponseDTO;
import school.sptech.projeto_amar.mapper.ProdutoLoteMapping;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DashboardService {
  private final MovimentacaoRepository movimentacaoRepository;
  private final ProdutoLoteRepository produtoLoteRepository;

  public DashboardService(MovimentacaoRepository movimentacaoRepository,
      ProdutoLoteRepository produtoLoteRepository) {
    this.movimentacaoRepository = movimentacaoRepository;
    this.produtoLoteRepository = produtoLoteRepository;
  }

  public Integer getEstoqueAtual(LocalDate dataInicio, LocalDate dataFim) {
    if (dataInicio.isAfter(dataFim)) {
      throw new IllegalArgumentException("A data inicial deve ser anterior ou igual à data final");
    }

    return movimentacaoRepository.sumSaldoByPeriodo(dataInicio, dataFim);
  }

  public Integer getAlimentosComRiscoDeVencimento(LocalDate dataInicio, LocalDate dataFim) {
    if (dataInicio.isAfter(dataFim)) {
      throw new IllegalArgumentException("A data inicial deve ser anterior ou igual à data final");
    }

    return produtoLoteRepository.countAlimentosComRiscoDeVencimento(dataInicio, dataFim);
  }

  public Integer getDoacoesRecebidas(LocalDate dataInicio, LocalDate dataFim) {
    validarPeriodo(dataInicio, dataFim);
    return movimentacaoRepository.sumQuantidadeByTipoAndPeriodo(
        TipoMovimentacao.ENTRADA, dataInicio, dataFim);
  }

  public Integer getItensDistribuidos(LocalDate dataInicio, LocalDate dataFim) {
    validarPeriodo(dataInicio, dataFim);
    return movimentacaoRepository.sumQuantidadeByTipoAndPeriodo(
        TipoMovimentacao.SAIDA, dataInicio, dataFim);
  }

    public List<MovimentacaoMensalResponseDTO> listarMovimentacoesPorMes() {
    LocalDate hoje = LocalDate.now();
    LocalDate dataInicio = LocalDate.of(hoje.getYear(), 1, 1);

    Map<Integer, MovimentacaoMensalResponseDTO> movimentacoesPorMes = movimentacaoRepository
      .sumMovimentacoesPorMes(dataInicio, hoje)
        .stream()
        .map(movimentacao -> new MovimentacaoMensalResponseDTO(
            ((Number) movimentacao[0]).intValue(),
            ((Number) movimentacao[1]).intValue(),
            ((Number) movimentacao[2]).longValue(),
            ((Number) movimentacao[3]).longValue()))
      .collect(Collectors.toMap(movimentacao -> movimentacao.mes(), movimentacao -> movimentacao));

    return IntStream.rangeClosed(1, hoje.getMonthValue())
      .mapToObj(mes -> movimentacoesPorMes.getOrDefault(mes,
        new MovimentacaoMensalResponseDTO(hoje.getYear(), mes, 0L, 0L)))
      .toList();
  }

  private void validarPeriodo(LocalDate dataInicio, LocalDate dataFim) {
    if (dataInicio.isAfter(dataFim)) {
      throw new IllegalArgumentException("A data inicial deve ser anterior ou igual à data final");
    }
  }

  public List<AlimentoRiscoVencimentoResponseDTO> listarAlimentosComRiscoDeVencimento(
      LocalDate dataInicio, LocalDate dataFim) {
    if (dataInicio.isAfter(dataFim)) {
      throw new IllegalArgumentException("A data inicial deve ser anterior ou igual à data final");
    }

    return produtoLoteRepository.findAlimentosComRiscoDeVencimento(dataInicio, dataFim)
        .stream()
        .map(ProdutoLoteMapping::toAlimentoRiscoVencimentoResponse)
        .toList();
  }
}
