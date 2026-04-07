package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.sptech.projeto_amar.domain.ProdutoLote;

import java.util.List;

public interface ProdutoLoteRepository extends JpaRepository<ProdutoLote, Integer> {
    @Query("SELECT SUM(p.quantidade) FROM ProdutoLote p WHERE p.produto.id = :id")
    Integer sumQuantidadeByProdutoId(@Param("id") Integer id);

    List<ProdutoLote> findByProdutoIdAndQuantidadeGreaterThanOrderByValidadeAsc(
            Integer produtoId, Integer quantidade);
}
