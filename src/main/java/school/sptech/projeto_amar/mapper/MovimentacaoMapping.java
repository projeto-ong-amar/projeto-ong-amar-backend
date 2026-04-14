package school.sptech.projeto_amar.mapper;

import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.dto.movimentacao.MovimentacaoResponseDTO;

public class MovimentacaoMapping {
    public static MovimentacaoResponseDTO toResponse(Movimentacao m) {
        MovimentacaoResponseDTO.ProdutoResponseMovimentacaoDTO produto =
                new MovimentacaoResponseDTO.ProdutoResponseMovimentacaoDTO();
        produto.setNome(m.getProduto().getNome());

        MovimentacaoResponseDTO.UsuarioResponseMovimentacaoDTO usuario =
                new MovimentacaoResponseDTO.UsuarioResponseMovimentacaoDTO();
        usuario.setNome(m.getUsuario().getNome());

        return new MovimentacaoResponseDTO(m.getTipo(), m.getQuantidade(), m.getData(), m.getMotivo(), produto
                , usuario);
    }
}
