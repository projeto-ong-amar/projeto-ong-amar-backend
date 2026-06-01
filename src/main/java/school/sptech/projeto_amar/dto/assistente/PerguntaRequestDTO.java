package school.sptech.projeto_amar.dto.assistente;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pergunta enviada ao assistente inteligente")
public record PerguntaRequestDTO(

        @Schema(
                description = "Pergunta que será analisada pela IA",
                example = "Quais são os principais riscos do estoque atualmente?"
        )
        String pergunta

) {
}