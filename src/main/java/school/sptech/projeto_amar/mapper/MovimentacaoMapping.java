package school.sptech.projeto_amar.mapper;

import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.dto.movimentacao.MovimentacaoResponseDTO;
import school.sptech.projeto_amar.dto.movimentacao.entrada.MovimentacaoHistoricoResponseDTO;

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

    public static MovimentacaoHistoricoResponseDTO toMovimentacaoHistorico(Movimentacao m) {
        return new MovimentacaoHistoricoResponseDTO(
                m.getId(),
                m.getData(),
                m.getProduto().getId(),
                m.getProduto().getNome(),
                m.getQuantidade(),
                m.getUsuario().getNome()
        );
    }
}
