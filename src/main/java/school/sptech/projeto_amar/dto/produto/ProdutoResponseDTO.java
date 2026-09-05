package school.sptech.projeto_amar.dto.produto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Dados de retorno de um produto")
public record ProdutoResponseDTO(

        @Schema(description = "ID do produto", example = "1")
        Integer id,

        @Schema(description = "Nome do produto", example = "Arroz")
        String nome,

        @Schema(description = "Unidade de medida", example = "kg")
        String unidadeMedida,

        @Schema(description = "Data em que o produto foi cadastrado", example = "2026-01-15")
        LocalDate dataCadastro,

        @Schema(description = "Categoria do produto")
        CategoriaProdutoDTO categoria

) {
    @Schema(description = "Dados resumidos da categoria")
    public static class CategoriaProdutoDTO {

        @Schema(description = "ID da categoria", example = "1")
        private Integer id;

        @Schema(description = "Nome da categoria", example = "Alimentos")
        private String nome;

        @Schema(description = "Descrição da categoria",
                example = "Produtos alimentícios em geral")
        private String descricao;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

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