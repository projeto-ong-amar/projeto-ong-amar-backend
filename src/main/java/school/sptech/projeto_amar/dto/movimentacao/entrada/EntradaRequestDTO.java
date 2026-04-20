package school.sptech.projeto_amar.dto.movimentacao.entrada;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Dados para registrar uma entrada de produto no estoque")
public record EntradaRequestDTO(

        @Schema(description = "Quantidade de itens recebidos", example = "50")
        @NotNull @Positive
        Integer quantidade,

        @Schema(description = "Data em que a entrada foi realizada",
                example = "2025-04-10", type = "string", format = "date")
        @NotNull
        LocalDate dataEntrada,

        @Schema(description = "Data de validade do produto (opcional)",
                example = "2026-12-31", type = "string", format = "date")
        LocalDate dataValidade,

        @Schema(description = "Motivo da entrada",
                example = "Doação recebida", minLength = 3, maxLength = 255)
        @NotBlank @Size(min = 3, max = 255)
        String motivo,

        @Schema(description = "ID do produto", example = "1")
        @NotNull @Positive
        Integer fkProduto,

        @Schema(description = "ID do usuário responsável", example = "2")
        @NotNull @Positive
        Integer fkUsuario

) {
}
