package school.sptech.projeto_amar.dto.movimentacao.perda;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados para registrar uma perda de produto no estoque")
public record PerdaRequestDTO(

        @Schema(description = "Quantidade de itens perdidos", example = "5")
        @NotNull @Positive
        Integer quantidade,

        @Schema(description = "Data em que a perda ocorreu",
                example = "2025-04-12", type = "string", format = "date")
        @NotNull
        LocalDate dataPerda,

        @Schema(description = "Motivo da perda",
                example = "Produto vencido", minLength = 3, maxLength = 255)
        @NotBlank @Size(min = 3, max = 255)
        String motivo,

        @Schema(description = "ID do produto", example = "1")
        @NotNull @Positive
        Integer fkProduto,

        @Schema(description = "ID do usuário responsável", example = "2")
        @NotNull @Positive
        Integer fkUsuario,

        @Schema(description = "ID do lote relacionado à perda", example = "3")
        @NotNull @Positive
        Integer fkLote

) {
}