package school.sptech.projeto_amar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PerdaRequestDTO (
        @Positive(message = "A quantidade deve ser positiva")
        @NotNull(message = "A quantidade não pode ser nula")
        Integer quantidade,

        @NotNull(message = "A data de perda não pode ser nula.")
        LocalDate dataPerda,

        @NotBlank(message = "O motivo não pode estar vazio.")
        @Size(min=3, max=255, message = "O motivo deve ter no mínimo 3 e no máximo 255 caracteres")
        String motivo,

        @NotNull(message = "A fkProduto não pode ser nula.")
        @Positive(message = "A fkProduto deve ser positiva.")
        Integer fkProduto,

        @NotNull(message = "A fkUsuario não pode ser nula.")
        @Positive(message = "A fkUsuario deve ser positiva.")
        Integer fkUsuario,

        @NotNull(message = "A fkLote não pode ser nula.")
        @Positive(message = "A fkLote deve ser positiva.")
        Integer fkLote
) {

}
