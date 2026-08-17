package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.dto.dashboard.AlimentoRiscoVencimentoResponseDTO;
import school.sptech.projeto_amar.mapper.ProdutoLoteMapping;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;

import java.time.LocalDate;
import java.util.List;

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
