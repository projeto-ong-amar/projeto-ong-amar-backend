package school.sptech.projeto_amar.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
        name = "EstoquePorTipo",
        description = "Quantidade em estoque agrupada por categoria de produto"
)
public record EstoquePorTipoResponseDTO(

    @Schema(description = "Nome da categoria do produto. Despite o nome do campo ser 'tipo', "
            + "o agrupamento é feito pela categoria do produto, não pela unidade de medida",
            example = "Laticínios")
    String tipo,

    @Schema(description = "Soma das quantidades em estoque dos lotes da categoria, considerando apenas lotes com quantidade maior que zero",
            example = "320")
    Long quantidade,

    @Schema(description = "Percentual da categoria sobre o estoque total, com duas casas decimais. "
            + "Retorna 0.00 quando o estoque total é 0", example = "35.50")
    BigDecimal porcentagem) {
}