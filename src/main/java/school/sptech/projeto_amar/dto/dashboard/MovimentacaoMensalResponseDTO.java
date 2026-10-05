package school.sptech.projeto_amar.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "MovimentacaoMensal",
        description = "Totais de entrada e saída de itens agregados por mês no ano corrente"
)
public record MovimentacaoMensalResponseDTO(

    @Schema(description = "Ano de referência", example = "2026")
    Integer ano,

    @Schema(description = "Mês de referência (1 a 12)", example = "3", minimum = "1", maximum = "12")
    Integer mes,

    @Schema(description = "Total de itens recebidos (movimentações de ENTRADA) no mês", example = "820")
    Long itensRecebidos,

    @Schema(description = "Total de itens entregues (movimentações de SAIDA) no mês. Perdas não entram nesta contagem",
            example = "640")
    Long itensEntregues) {
}