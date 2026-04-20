package school.sptech.projeto_amar.dto.produto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProdutoResponseDTO(
        @Schema(example = "Arroz")
        String nome,

        @Schema(example = "kg")
        String unidadeMedida,
        CategoriaProdutoDTO categoria
) {
    public static class CategoriaProdutoDTO {
        @Schema(example = "Alimentos")
        private String nome;

        @Schema(example = "Produtos alimentícios em geral")
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
