package school.sptech.projeto_amar.mapper;

import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.dto.request.CategoriaRequestDTO;
import school.sptech.projeto_amar.dto.response.CategoriaResponseDTO;

public class CategoriaMapping {
    public static Categoria toDomain(CategoriaRequestDTO c) {
        Categoria retorno = new Categoria();
        retorno.setNome(c.nome());
        retorno.setDescricao(c.descricao());
        return retorno;
    }

    public static CategoriaResponseDTO toResponse(Categoria c) {
        return new CategoriaResponseDTO(c.getNome(), c.getDescricao());
    }
}
