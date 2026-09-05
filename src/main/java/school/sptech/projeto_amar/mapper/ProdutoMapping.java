package school.sptech.projeto_amar.mapper;

import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.dto.produto.ProdutoRequestDTO;
import school.sptech.projeto_amar.dto.produto.ProdutoResponseDTO;

public class ProdutoMapping {
    public static Produto toDomain(ProdutoRequestDTO p) {
        Produto produto = new Produto();
        produto.setNome(p.nome());
        produto.setUnidadeMedida(p.unidadeMedida());
        produto.setDataCadastro(p.dataCadastro());

        Categoria categoria = new Categoria();
        categoria.setId(p.fkCategoria());

        produto.setCategoria(categoria);
        return produto;
    }

    public static ProdutoResponseDTO toResponse(Produto p) {
        ProdutoResponseDTO.CategoriaProdutoDTO categoriaProdutoDTO = new ProdutoResponseDTO.CategoriaProdutoDTO();
        categoriaProdutoDTO.setId(p.getCategoria().getId());
        categoriaProdutoDTO.setNome(p.getCategoria().getNome());
        categoriaProdutoDTO.setDescricao(p.getCategoria().getDescricao());

        return new ProdutoResponseDTO(p.getId(), p.getNome(), p.getUnidadeMedida(), p.getDataCadastro(), categoriaProdutoDTO);
    }
}
