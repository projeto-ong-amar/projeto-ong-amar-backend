package school.sptech.projeto_amar.dto.movimentacao;

import io.swagger.v3.oas.annotations.media.Schema;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;

import java.time.LocalDate;

@Schema(description = "Dados de retorno de uma movimentação")
public record MovimentacaoResponseDTO(

        @Schema(description = "Tipo da movimentação",
                example = "ENTRADA", allowableValues = {"ENTRADA", "SAIDA", "PERDA"})
        TipoMovimentacao tipo,

        @Schema(description = "Quantidade de itens movimentados", example = "50")
        Integer quantidade,

        @Schema(description = "Data da movimentação",
                example = "2025-04-10", type = "string", format = "date")
        LocalDate data,

        @Schema(description = "Motivo da movimentação", example = "Doação recebida")
        String motivo,

        @Schema(description = "Produto relacionado à movimentação")
        ProdutoResponseMovimentacaoDTO produto,

        @Schema(description = "Usuário responsável pela movimentação")
        UsuarioResponseMovimentacaoDTO usuario

) {
    @Schema(description = "Dados resumidos do produto")
    public static class ProdutoResponseMovimentacaoDTO {
        @Schema(description = "Nome do produto", example = "Arroz")
        private String nome;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }
    }

    @Schema(description = "Dados resumidos do usuário")
    public static class UsuarioResponseMovimentacaoDTO {
        @Schema(description = "Nome do usuário", example = "João Silva")
        private String nome;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }
    }
}