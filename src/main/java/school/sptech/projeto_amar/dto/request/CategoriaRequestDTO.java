package school.sptech.projeto_amar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(
        @NotBlank
        @Size(min=3, max = 255)
        String nome,

        @NotBlank
        @Size(min=3, max = 255)
        String descricao
) {
}
