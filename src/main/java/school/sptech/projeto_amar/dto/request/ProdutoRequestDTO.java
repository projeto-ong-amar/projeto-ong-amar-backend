package school.sptech.projeto_amar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProdutoRequestDTO(
        @NotBlank(message = "O nome não pode estar vazio.")
        @Size(min=3, max=255, message = "O nome deve ter no mínimo 3 e no máximo 255 caracteres.")
        String nome,

        @NotBlank(message = "A unidade de medida não pode estar vazia.")
        @Size(max=255, message = "A unidade de medida deve ter no máximo 255 caracteres.")
        String unidadeMedida,

        @Positive(message = "A fkCategoria deve ser positiva.")
        @NotNull(message = "A fkCategoria não pode ser nula.")
        Integer fkCategoria
) {
}
