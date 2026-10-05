package school.sptech.projeto_amar.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(
        name = "AlimentoRiscoVencimento",
        description = "Lote em estoque dentro da janela de risco de vencimento (de hoje até 30 dias à frente)"
)
public record AlimentoRiscoVencimentoResponseDTO(

    @Schema(description = "ID do lote", example = "12")
    Integer loteId,

    @Schema(description = "Nome do produto do lote", example = "Leite integral")
    String produto,

    @Schema(description = "Nome da categoria do produto", example = "Laticínios")
    String categoria,

    @Schema(description = "Quantidade ainda disponível no lote", example = "24")
    Integer quantidade,

    @Schema(description = "Data de validade do lote",
            example = "2026-02-10", type = "string", format = "date")
    LocalDate validade) {
}