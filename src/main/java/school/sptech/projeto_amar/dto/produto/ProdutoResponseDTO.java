package school.sptech.projeto_amar.dto.produto;

import io.swagger.v3.oas.annotations.media.Schema;
import school.sptech.projeto_amar.domain.enums.Categoria;

@Schema(description = "Dados de retorno de um produto")
public record ProdutoResponseDTO(
        @Schema(description = "ID do produto", example = "1")
        Integer id,
        @Schema(description = "Nome do produto", example = "Arroz")
        String nome,
        @Schema(description = "Unidade de medida", example = "kg")
        String unidadeMedida,
        @Schema(description = "Categoria do produto", example = "ALIMENTOS")
        Categoria categoria
) {
}
