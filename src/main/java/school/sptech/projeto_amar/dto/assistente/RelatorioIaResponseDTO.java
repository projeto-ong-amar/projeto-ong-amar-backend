package school.sptech.projeto_amar.dto.assistente;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
        name = "RelatorioIaResponse",
        description = "Estrutura de JSON que o assistente de IA deve devolver para cada relatório. "
                + "Uso interno: é a resposta bruta do modelo, antes da validação e da conversão para RelatorioAssistente"
)
public record RelatorioIaResponseDTO(

        @JsonProperty("resumo_executivo")
        @Schema(description = "Resumo executivo do relatório em texto livre",
                example = "O estoque de laticínios está concentrado em dois lotes com validade próxima.")
        String resumoExecutivo,

        @JsonProperty("prioridade")
        @Schema(description = "Nível de prioridade do relatório, sem acentos e em minúsculas. "
                + "Valores fora desta lista fazem o backend rejeitar a resposta da IA e cair no relatório de contingência",
                example = "critica", allowableValues = {"baixa", "media", "alta", "critica"})
        String prioridade,

        @JsonProperty("analise")
        @Schema(description = "Pontos de análise identificados pela IA. Cada item é um achado independente")
        List<String> analise,

        @JsonProperty("acoes_recomendadas")
        @Schema(description = "Ações sugeridas pela IA para tratar os problemas encontrados")
        List<String> acoesRecomendadas,

        @JsonProperty("tabelas")
        @Schema(description = "Tabelas de dados para exibir no relatório. Pode ser nulo quando o relatório não tem tabela")
        List<TabelaRelatorioDTO> tabelas

) {
}