package school.sptech.projeto_amar.mapper;

import school.sptech.projeto_amar.domain.ProdutoLote;
import school.sptech.projeto_amar.dto.dashboard.AlimentoRiscoVencimentoResponseDTO;

public class ProdutoLoteMapping {
  public static AlimentoRiscoVencimentoResponseDTO toAlimentoRiscoVencimentoResponse(ProdutoLote lote) {
    return new AlimentoRiscoVencimentoResponseDTO(
        lote.getId(),
        lote.getProduto().getNome(),
        lote.getQuantidade(),
        lote.getValidade());
  }
}