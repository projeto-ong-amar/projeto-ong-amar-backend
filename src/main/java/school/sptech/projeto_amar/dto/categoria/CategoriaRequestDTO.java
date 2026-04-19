package school.sptech.projeto_amar.dto.categoria;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(
        @Schema(description = "Nome da categoria", example = "Alimentos")
        @NotBlank(message = "O nome não pode estar vazio.")
        @Size(min = 3, max = 255, message = "O nome deve ter no mínimo 3 e no máximo 255 caracteres.")
        String nome,

        @Schema(description = "Descrição da categoria", example = "Produtos alimentícios em geral")
        @NotBlank(message = "A descrição não pode estar vazia.")
        @Size(min = 3, max = 255, message = "A descrição deve ter no mínimo 3 e no máximo 255 caracteres.")
        String descricao
) {
}
