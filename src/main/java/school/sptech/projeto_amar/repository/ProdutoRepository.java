package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.sptech.projeto_amar.domain.Produto;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    Boolean existsByNomeAndStatus(String nome, String status);

    Long countByStatus(String status);

    List<Produto> findTop30ByOrderByIdDesc();

    List<Produto> findAllByStatus(String status);

    List<Produto> findAllByCategoriaNomeAndStatus(String categoria, String status);
}
