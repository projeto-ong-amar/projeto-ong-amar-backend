package school.sptech.projeto_amar.dto.movimentacao.saida;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Dados para registrar uma saída de produto do estoque")
public record SaidaRequestDTO(

        @Schema(description = "Quantidade de itens retirados", example = "10")
        @NotNull @Positive
        Integer quantidade,

        @Schema(description = "Data em que a saída foi realizada",
                example = "2025-04-15", type = "string", format = "date")
        @NotNull
        LocalDate dataSaida,

        @Schema(description = "Motivo da saída",
                example = "Distribuição para famílias", minLength = 3, maxLength = 255)
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
