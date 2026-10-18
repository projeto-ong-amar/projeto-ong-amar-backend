package school.sptech.projeto_amar.dto.configuracao;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ConfiguracaoMetricaDTO(
    @NotNull @Positive Integer estoqueBaixo,
    @NotNull @Positive Integer riscoVencimentoDias,
    @NotNull @Positive Integer vencimentoProximoDias,
    @NotNull @Positive Integer semMovimentacaoDias,
    @NotNull @Positive Integer periodoIndicadoresDias) {
}
