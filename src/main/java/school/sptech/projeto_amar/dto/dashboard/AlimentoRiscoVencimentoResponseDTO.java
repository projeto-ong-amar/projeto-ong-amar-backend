package school.sptech.projeto_amar.dto.dashboard;

import java.time.LocalDate;

public record AlimentoRiscoVencimentoResponseDTO(
    Integer loteId,
    String produto,
    String categoria,
    Integer quantidade,
    LocalDate validade) {
}