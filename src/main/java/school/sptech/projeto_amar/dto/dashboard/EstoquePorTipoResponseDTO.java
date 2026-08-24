package school.sptech.projeto_amar.dto.dashboard;

import java.math.BigDecimal;

public record EstoquePorTipoResponseDTO(
    String tipo,
    Long quantidade,
    BigDecimal porcentagem) {
}