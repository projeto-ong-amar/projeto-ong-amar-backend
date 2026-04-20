package school.sptech.projeto_amar.dto.categoria;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de uma categoria")
public record CategoriaRequestDTO(

        @Schema(description = "Nome da categoria", example = "Alimentos",
                minLength = 3, maxLength = 255)
        @NotBlank(message = "O nome não pode estar vazio.")
        @Size(min = 3, max = 255)
        String nome,

        @Schema(description = "Descrição da categoria",
                example = "Produtos alimentícios em geral",
                minLength = 3, maxLength = 255)
        @NotBlank(message = "A descrição não pode estar vazia.")
        @Size(min = 3, max = 255)
        String descricao

) {
}
