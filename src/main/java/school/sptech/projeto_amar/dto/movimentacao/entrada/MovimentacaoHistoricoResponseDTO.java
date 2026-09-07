package school.sptech.projeto_amar.dto.movimentacao.entrada;

import java.time.LocalDate;

public record MovimentacaoHistoricoResponseDTO (
        Integer id,
        LocalDate dataHora,
        Integer produtoId,
        String produtoNome,
        Integer quantidade,
        String responsavelNome,
        String motivo
) {
}
