package school.sptech.projeto_amar.dto.categoria;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de retorno de uma categoria")
public record CategoriaResponseDTO(

        @Schema(description = "Nome da categoria", example = "Alimentos")
        String nome,

        @Schema(description = "Descrição da categoria",
                example = "Produtos alimentícios em geral")
        String descricao

) {
}
