package school.sptech.projeto_amar.dto.produto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de retorno de um produto")
public record ProdutoResponseDTO(

        @Schema(description = "Nome do produto", example = "Arroz")
        String nome,

        @Schema(description = "Unidade de medida", example = "kg")
        String unidadeMedida,

        @Schema(description = "Categoria do produto")
        CategoriaProdutoDTO categoria

) {
    @Schema(description = "Dados resumidos da categoria")
    public static class CategoriaProdutoDTO {

        @Schema(description = "Nome da categoria", example = "Alimentos")
        private String nome;

        @Schema(description = "Descrição da categoria",
                example = "Produtos alimentícios em geral")
        private String descricao;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }
    }
}