package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.enums.Categoria;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    Boolean existsByNomeAndStatus(String nome, String status);

    Long countByStatus(String status);

    List<Produto> findTop30ByOrderByIdDesc();

    List<Produto> findAllByStatus(String status);

    List<Produto> findAllByStatusOrderByNomeAsc(String status);

    List<Produto> findAllByCategoriaAndStatus(Categoria categoria, String status);
}
