package school.sptech.projeto_amar.dto.assistente;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
        description = "Resposta bruta retornada pelo Ollama"
)
public record OllamaResponseDTO(

        @Schema(
                description = "Conteúdo textual retornado pelo modelo de IA",
                example = """
                {
                  "titulo":"Risco de ruptura de estoque",
                  "tipo":"alerta",
                  "prioridade":"critica",
                  "response":[
                    "O estoque de leite está zerado",
                    "Arroz possui apenas 2 unidades"
                  ],
                  "acoes_recomendadas":[
                    "Realizar reposição emergencial",
                    "Revisar planejamento de compras"
                  ]
                }
                """
        )
        String response

) {
}