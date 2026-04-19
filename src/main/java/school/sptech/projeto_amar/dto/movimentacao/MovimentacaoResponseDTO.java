package school.sptech.projeto_amar.dto.movimentacao;

import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;

import java.time.LocalDate;

public record MovimentacaoResponseDTO(
        TipoMovimentacao tipo,
        Integer quantidade,
        LocalDate data,
        String motivo,
        ProdutoResponseMovimentacaoDTO produto,
        UsuarioResponseMovimentacaoDTO usuario
) {
    public static class ProdutoResponseMovimentacaoDTO {
        private String nome;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }
    }

    public static class UsuarioResponseMovimentacaoDTO {
        private String nome;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }
    }
}
