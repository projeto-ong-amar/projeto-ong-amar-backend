package school.sptech.projeto_amar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProdutoRequestDTO(
        @NotBlank
        @Size(min=3, max=255)
        String nome,

        @NotBlank
        @Size(max=255)
        String unidadeMedida,

        @Positive
        @NotNull
        Integer fkCategoria
) {
}
