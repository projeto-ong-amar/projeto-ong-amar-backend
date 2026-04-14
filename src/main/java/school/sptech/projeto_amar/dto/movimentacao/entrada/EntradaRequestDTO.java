package school.sptech.projeto_amar.dto.movimentacao.entrada;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record EntradaRequestDTO (
    @Positive(message = "A quantidade deve ser positiva")
    @NotNull(message = "A quantidade não pode ser nula")
    Integer quantidade,

    @NotNull(message = "A data de entrada não pode ser nula.")
    LocalDate dataEntrada,

    @FutureOrPresent(message = "A data de validade deve ser no futuro ou no presente!")
    LocalDate dataValidade,

    @NotBlank(message = "O motivo não pode estar vazio.")
    @Size(min=3, max=255, message = "O motivo deve ter no mínimo 3 e no máximo 255 caracteres")
    String motivo,

    @NotNull(message = "A fkProduto não pode ser nula.")
    @Positive(message = "A fkProduto deve ser positiva.")
    Integer fkProduto,

    @NotNull(message = "A fkUsuario não pode ser nula.")
    @Positive(message = "A fkUsuario deve ser positiva.")
    Integer fkUsuario
) {
}
