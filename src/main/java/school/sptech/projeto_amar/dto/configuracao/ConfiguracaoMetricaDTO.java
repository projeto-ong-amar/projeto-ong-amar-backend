package school.sptech.projeto_amar.dto.configuracao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Limites globais utilizados para calcular os indicadores de estoque")
public record ConfiguracaoMetricaDTO(
    @Schema(description = "Quantidade de unidades ou menos para classificar um produto com estoque baixo", example = "10", minimum = "1")
    @NotNull @Positive Integer estoqueBaixo,
    @Schema(description = "Janela em dias para considerar um lote em risco de vencimento", example = "30", minimum = "1")
    @NotNull @Positive Integer riscoVencimentoDias,
    @Schema(description = "Janela em dias para classificar um lote como próximo do vencimento", example = "7", minimum = "1")
    @NotNull @Positive Integer vencimentoProximoDias,
    @Schema(description = "Quantidade de dias sem movimentação para classificar um produto como parado", example = "60", minimum = "1")
    @NotNull @Positive Integer semMovimentacaoDias,
    @Schema(description = "Período em dias considerado nos indicadores", example = "30", minimum = "1")
    @NotNull @Positive Integer periodoIndicadoresDias) {
}
