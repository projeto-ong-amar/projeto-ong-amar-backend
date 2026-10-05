package school.sptech.projeto_amar.dto.assistente;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
        name = "TabelaRelatorio",
        description = "Tabela de dados exibida dentro de um relatório gerado pela IA"
)
public record TabelaRelatorioDTO(

        @Schema(description = "Título exibido acima da tabela",
                example = "Produtos com risco de vencimento")
        String titulo,

        @Schema(description = "Cabeçalhos das colunas, na ordem em que aparecem nas linhas",
                example = "[\"Produto\", \"Quantidade\", \"Validade\"]")
        List<String> colunas,

        @Schema(description = "Linhas da tabela. Cada lista interna corresponde a uma linha e deve ter o mesmo "
                + "número de itens de 'colunas'",
                example = "[[\"Leite\", \"24\", \"2026-02-10\"], [\"Arroz\", \"15\", \"2026-02-18\"]]")
        List<List<String>> linhas

) {
}