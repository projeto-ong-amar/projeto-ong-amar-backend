package school.sptech.projeto_amar.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.domain.ProdutoLote;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;

@Service
public class RelatorioMcpToolsService {

  private final MovimentacaoRepository movimentacaoRepository;
  private final ProdutoLoteRepository produtoLoteRepository;

  public RelatorioMcpToolsService(
      MovimentacaoRepository movimentacaoRepository,
      ProdutoLoteRepository produtoLoteRepository) {
    this.movimentacaoRepository = movimentacaoRepository;
    this.produtoLoteRepository = produtoLoteRepository;
  }

  @Tool(description = "Retorna a quantidade de itens que entraram no estoque entre duas datas (inclusive).")
  public Integer consultarItensEntradaPorPeriodo(
      @ToolParam(description = "Data inicial no formato AAAA-MM-DD") LocalDate dataInicio,
      @ToolParam(description = "Data final no formato AAAA-MM-DD") LocalDate dataFim) {
    if (dataInicio == null || dataFim == null) {
      return 0;
    }
    return movimentacaoRepository.sumQuantidadeByTipoAndPeriodo(
        TipoMovimentacao.ENTRADA, dataInicio, dataFim);
  }

  @Tool(description = "Retorna a quantidade de itens que saíram do estoque entre duas datas (inclusive).")
  public Integer consultarItensSaidaPorPeriodo(
      @ToolParam(description = "Data inicial no formato AAAA-MM-DD") LocalDate dataInicio,
      @ToolParam(description = "Data final no formato AAAA-MM-DD") LocalDate dataFim) {
    if (dataInicio == null || dataFim == null) {
      return 0;
    }
    return movimentacaoRepository.sumQuantidadeByTipoAndPeriodo(
        TipoMovimentacao.SAIDA, dataInicio, dataFim);
  }

  @Tool(description = "Retorna a quantidade total de itens atualmente em estoque (soma dos lotes).")
  public Integer consultarItensEmEstoque() {
    return produtoLoteRepository.sumQuantidadeTotal();
  }

  @Tool(description = "Lista os produtos com risco de vencimento em um intervalo de datas, informando produto, quantidade e validade.")
  public String consultarProdutosEmRiscoVencimento(
      @ToolParam(description = "Data inicial do intervalo de vencimento no formato AAAA-MM-DD") LocalDate dataInicio,
      @ToolParam(description = "Data final do intervalo de vencimento no formato AAAA-MM-DD") LocalDate dataFim) {
    if (dataInicio == null || dataFim == null) {
      return "Sem dados.";
    }
    List<ProdutoLote> lotes = produtoLoteRepository.findAlimentosComRiscoDeVencimento(
        dataInicio, dataFim);
    if (lotes.isEmpty()) {
      return "Nenhum produto em risco de vencimento no período.";
    }

    StringBuilder sb = new StringBuilder();
    for (ProdutoLote lote : lotes) {
      sb.append("- Produto: ")
          .append(lote.getProduto() == null ? "-" : lote.getProduto().getNome())
          .append(" | Quantidade: ").append(lote.getQuantidade())
          .append(" | Validade: ").append(lote.getValidade())
          .append("\n");
    }
    return sb.toString().trim();
  }
}
