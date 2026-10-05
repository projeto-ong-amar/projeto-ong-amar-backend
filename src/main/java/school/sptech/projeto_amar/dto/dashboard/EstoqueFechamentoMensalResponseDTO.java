package school.sptech.projeto_amar.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "EstoqueFechamentoMensal",
        description = "Saldo de estoque acumulado ao final de cada mês do ano corrente"
)
public record EstoqueFechamentoMensalResponseDTO(

    @Schema(description = "Ano de referência do fechamento", example = "2026")
    Integer ano,

    @Schema(description = "Mês de referência (1 a 12)", example = "3", minimum = "1", maximum = "12")
    Integer mes,

    @Schema(description = "Saldo de estoque ao final do mês. Entradas somam, saídas e perdas subtraem, "
            + "somando o saldo anterior. Pode ser negativo", example = "450")
    Long estoqueFechamento) {
}