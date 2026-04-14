package school.sptech.projeto_amar.dto.produto;

public record ProdutoResponseDTO(
        String nome,
        String unidadeMedida,
        CategoriaProdutoDTO categoria
) {
    public static class CategoriaProdutoDTO {
        private String nome;
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
