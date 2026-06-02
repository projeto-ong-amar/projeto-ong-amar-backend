package school.sptech.projeto_amar.dto.assistente;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "RelatorioAssistente",
        description = "Relatório gerado pela IA com análise do estoque"
)
public record RelatorioAssistenteDTO(

        @Schema(
                description = "Título do relatório",
                example = "Análise Completa do Estoque"
        )
        String titulo,

        @Schema(
                description = "Tipo do relatório gerado",
                example = "insight"
        )
        String tipo,

        @Schema(
                description = "Nível de prioridade identificado",
                example = "critica"
        )
        String prioridade,

        @Schema(
                description = "Lista de insights encontrados pela IA"
        )
        List<String> response,

        @Schema(
                description = "Lista de ações recomendadas pela IA"
        )
        List<String> acoes_recomendadas

) {
}