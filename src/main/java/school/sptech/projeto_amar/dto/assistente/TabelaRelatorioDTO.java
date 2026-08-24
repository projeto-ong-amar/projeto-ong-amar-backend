package school.sptech.projeto_amar.dto.assistente;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TabelaRelatorioDTO(

        String titulo,

        List<String> colunas,

        List<List<String>> linhas

) {
}
