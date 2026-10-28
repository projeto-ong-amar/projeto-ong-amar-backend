package school.sptech.projeto_amar.dto.produto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import school.sptech.projeto_amar.domain.enums.Categoria;

@Schema(description = "Dados para cadastro de um produto")
public record ProdutoRequestDTO(
        @Schema(description = "Nome do produto", example = "Arroz", minLength = 3, maxLength = 45)
        @NotBlank @Size(min = 3, max = 45)
        String nome,

        @Schema(description = "Unidade de medida do produto", example = "kg", maxLength = 45)
        @Size(max = 45)
        String unidadeMedida,

        @Schema(description = "Categoria fixa do produto", example = "ALIMENTOS",
                allowableValues = {"ALIMENTOS", "PRODUTOS DE HIGIENE"})
        Categoria categoria
) {
}
