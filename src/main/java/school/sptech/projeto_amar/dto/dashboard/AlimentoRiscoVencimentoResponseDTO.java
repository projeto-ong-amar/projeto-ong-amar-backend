package school.sptech.projeto_amar.dto.dashboard;

import java.time.LocalDate;

public record AlimentoRiscoVencimentoResponseDTO(
    Integer loteId,
    String produto,
    Integer quantidade,
    LocalDate validade) {
}