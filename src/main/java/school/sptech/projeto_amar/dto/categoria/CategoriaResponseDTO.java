package school.sptech.projeto_amar.dto.categoria;

import io.swagger.v3.oas.annotations.media.Schema;

public record CategoriaResponseDTO(
        @Schema(example = "Alimentos")
        String nome,

        @Schema(example = "Produtos alimentícios em geral")
        String descricao
) {
}
