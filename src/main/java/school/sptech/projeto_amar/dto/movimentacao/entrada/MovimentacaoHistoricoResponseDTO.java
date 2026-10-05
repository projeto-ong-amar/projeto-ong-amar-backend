package school.sptech.projeto_amar.dto.movimentacao.entrada;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(
        name = "MovimentacaoHistorico",
        description = "Item do histórico paginado de movimentações de entrada ou saída"
)
public record MovimentacaoHistoricoResponseDTO (
        @Schema(description = "ID da movimentação", example = "45")
        Integer id,

        @Schema(description = "Data em que a movimentação foi registrada",
                example = "2026-01-15", type = "string", format = "date")
        LocalDate dataHora,

        @Schema(description = "ID do produto movimentado", example = "7")
        Integer produtoId,

        @Schema(description = "Nome do produto movimentado", example = "Arroz")
        String produtoNome,

        @Schema(description = "Quantidade movimentada", example = "50")
        Integer quantidade,

        @Schema(description = "Nome do usuário responsável pela movimentação", example = "João Silva")
        String responsavelNome,

        @Schema(description = "Motivo informado no registro da movimentação", example = "Doação recebida")
        String motivo
) {
}