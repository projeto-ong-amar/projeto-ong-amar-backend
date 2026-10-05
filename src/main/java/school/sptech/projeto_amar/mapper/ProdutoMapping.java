package school.sptech.projeto_amar.mapper;

import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.dto.produto.ProdutoRequestDTO;
import school.sptech.projeto_amar.dto.produto.ProdutoResponseDTO;

public class ProdutoMapping {
    public static Produto toDomain(ProdutoRequestDTO p) {
        Produto produto = new Produto();
        produto.setNome(p.nome());
        produto.setUnidadeMedida(p.unidadeMedida());
        produto.setCategoria(p.categoria());
        return produto;
    }

    public static ProdutoResponseDTO toResponse(Produto p) {
        return new ProdutoResponseDTO(p.getId(), p.getNome(), p.getUnidadeMedida(), p.getCategoria());
    }
}
