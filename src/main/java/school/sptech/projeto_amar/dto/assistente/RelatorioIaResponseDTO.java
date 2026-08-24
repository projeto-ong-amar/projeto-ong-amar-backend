package school.sptech.projeto_amar.dto.assistente;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RelatorioIaResponseDTO(

        @JsonProperty("resumo_executivo")
        String resumoExecutivo,

        @JsonProperty("prioridade")
        String prioridade,

        @JsonProperty("analise")
        List<String> analise,

        @JsonProperty("acoes_recomendadas")
        List<String> acoesRecomendadas,

        @JsonProperty("tabelas")
        List<TabelaRelatorioDTO> tabelas

) {
}
