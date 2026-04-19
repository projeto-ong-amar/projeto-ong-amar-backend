package school.sptech.projeto_amar.dto.produto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de um produto")
public record ProdutoRequestDTO(

        @Schema(description = "Nome do produto", example = "Arroz",
                minLength = 3, maxLength = 255)
        @NotBlank @Size(min = 3, max = 255)
        String nome,

        @Schema(description = "Unidade de medida do produto", example = "kg",
                maxLength = 255)
        @NotBlank @Size(max = 255)
        String unidadeMedida,

        @Schema(description = "ID da categoria associada", example = "1")
        @NotNull @Positive
        Integer fkCategoria

) {
}