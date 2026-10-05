package school.sptech.projeto_amar.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "EstoqueProdutoAtual",
        description = "Quantidade atual em estoque de um produto ativo"
)
public record EstoqueProdutoAtualResponseDTO(

    @Schema(description = "Nome do produto", example = "Arroz")
    String produto,

    @Schema(description = "Nome da categoria do produto", example = "Grãos")
    String categoria,

    @Schema(description = "Soma das quantidades de todos os lotes do produto", example = "150")
    Long quantidade) {
}