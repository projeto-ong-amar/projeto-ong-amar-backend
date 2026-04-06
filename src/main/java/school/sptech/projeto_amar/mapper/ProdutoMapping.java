package school.sptech.projeto_amar.mapper;

import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.dto.request.ProdutoRequestDTO;
import school.sptech.projeto_amar.dto.response.ProdutoResponseDTO;

public class ProdutoMapping {
    public static Produto toDomain(ProdutoRequestDTO p) {
        Produto produto = new Produto();
        produto.setNome(p.nome());
        produto.setUnidadeMedida(p.unidadeMedida());

        Categoria categoria = new Categoria();
        categoria.setId(p.fkCategoria());

        produto.setCategoria(categoria);
        return produto;
    }

    public static ProdutoResponseDTO toResponse(Produto p) {
        ProdutoResponseDTO.CategoriaProdutoDTO categoriaProdutoDTO = new ProdutoResponseDTO.CategoriaProdutoDTO();
        categoriaProdutoDTO.setNome(p.getCategoria().getNome());
        categoriaProdutoDTO.setDescricao(p.getCategoria().getDescricao());

        return new ProdutoResponseDTO(p.getNome(), p.getUnidadeMedida(), categoriaProdutoDTO);
    }
}
