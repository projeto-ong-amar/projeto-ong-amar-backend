package school.sptech.projeto_amar.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProdutoRequestDTO(
        @Schema(description = "Nome do produto", example = "Arroz")
        @NotBlank(message = "O nome não pode estar vazio.")
        @Size(min = 3, max = 255, message = "O nome deve ter no mínimo 3 e no máximo 255 caracteres.")
        String nome,

        @Schema(description = "Unidade de medida do produto", example = "kg")
        @NotBlank(message = "A unidade de medida não pode estar vazia.")
        @Size(max = 255, message = "A unidade de medida deve ter no máximo 255 caracteres.")
        String unidadeMedida,

        @Schema(description = "ID da categoria associada", example = "1")
        @Positive(message = "A fkCategoria deve ser positiva.")
        @NotNull(message = "A fkCategoria não pode ser nula.")
        Integer fkCategoria
) {
}
