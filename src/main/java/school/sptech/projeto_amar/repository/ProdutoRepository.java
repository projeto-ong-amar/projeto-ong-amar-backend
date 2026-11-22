package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.enums.Categoria;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    @Query("""
        SELECT COUNT(p) FROM Produto p
        WHERE p.status = 'ATIVO'
          AND (SELECT COALESCE(SUM(l.quantidade), 0) FROM ProdutoLote l
               WHERE l.produto = p) < :limite
        """)
    Long countProdutosComEstoqueBaixo(@Param("limite") Integer limite);

    Boolean existsByNomeAndStatus(String nome, String status);

    Boolean existsByNomeAndStatusAndIdNot(String nome, String status, Integer id);

    Long countByStatus(String status);

    List<Produto> findTop30ByOrderByIdDesc();

    List<Produto> findAllByStatus(String status);

    List<Produto> findAllByStatusOrderByNomeAsc(String status);

    List<Produto> findAllByCategoriaAndStatus(Categoria categoria, String status);
}
