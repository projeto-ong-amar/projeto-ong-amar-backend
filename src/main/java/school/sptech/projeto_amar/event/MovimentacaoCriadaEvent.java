package school.sptech.projeto_amar.event;

import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;

import java.time.LocalDate;

public record MovimentacaoCriadaEvent(
    TipoMovimentacao tipo,
    String produto,
    Integer quantidade,
    LocalDate data,
    String motivo) {
}
