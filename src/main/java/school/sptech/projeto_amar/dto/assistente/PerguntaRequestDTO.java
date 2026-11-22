package school.sptech.projeto_amar.dto.assistente;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Pergunta enviada ao assistente inteligente")
public record PerguntaRequestDTO(

        @Schema(
                description = "Pergunta que será analisada pela IA",
                example = "Quais são os principais riscos do estoque atualmente?"
        )
        String pergunta,

        @Schema(
                description = "Data inicial do período (inclusive). Envie junto com dataFim; omita ambas para todo o histórico.",
                example = "2026-04-01"
        )
        LocalDate dataInicio,

        @Schema(
                description = "Data final do período (inclusive). Envie junto com dataInicio; omita ambas para todo o histórico.",
                example = "2026-04-30"
        )
        LocalDate dataFim

) {
}
