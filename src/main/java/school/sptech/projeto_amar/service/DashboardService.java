package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;

import java.time.LocalDate;

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
}
