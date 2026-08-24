package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.sptech.projeto_amar.domain.ProdutoLote;

import java.time.LocalDate;
import java.util.List;

public interface ProdutoLoteRepository extends JpaRepository<ProdutoLote, Integer> {
  @Query("SELECT SUM(p.quantidade) FROM ProdutoLote p WHERE p.produto.id = :id")
  Integer sumQuantidadeByProdutoId(@Param("id") Integer id);

  @Query("SELECT COALESCE(SUM(p.quantidade), 0) FROM ProdutoLote p")
  Integer sumQuantidadeTotal();

  @Query("""
      SELECT COUNT(p)
      FROM ProdutoLote p
      WHERE p.validade IS NOT NULL
        AND p.validade < :hoje
        AND p.quantidade > 0
      """)
  Long countLotesVencidos(@Param("hoje") LocalDate hoje);

  @Query("""
      SELECT COUNT(p)
      FROM ProdutoLote p
      WHERE p.validade IS NOT NULL
        AND p.validade BETWEEN :hoje AND :limite
        AND p.quantidade > 0
      """)
  Long countLotesProximosDoVencimento(@Param("hoje") LocalDate hoje, @Param("limite") LocalDate limite);

  @Query(value = """
      SELECT COUNT(*)
      FROM produto_lote
      WHERE quantidade > 0
        AND validade IS NOT NULL
        AND validade >= :dataInicio
        AND validade <= :dataFim
      """, nativeQuery = true)
  Integer countAlimentosComRiscoDeVencimento(
      @Param("dataInicio") LocalDate dataInicio,
      @Param("dataFim") LocalDate dataFim);

  @Query("""
      SELECT p.produto.categoria.nome, COALESCE(SUM(p.quantidade), 0)
      FROM ProdutoLote p
      WHERE p.quantidade > 0
      GROUP BY p.produto.categoria.nome
      ORDER BY p.produto.categoria.nome
      """)
  List<Object[]> sumEstoquePorTipo();

  @Query("""
      SELECT p
      FROM ProdutoLote p
      WHERE p.quantidade > 0
        AND p.validade IS NOT NULL
        AND p.validade >= :dataInicio
        AND p.validade <= :dataFim
      ORDER BY p.validade ASC
      """)
  List<ProdutoLote> findAlimentosComRiscoDeVencimento(
      @Param("dataInicio") LocalDate dataInicio,
      @Param("dataFim") LocalDate dataFim);

  List<ProdutoLote> findByProdutoIdAndQuantidadeGreaterThanOrderByValidadeAsc(
      Integer produtoId, Integer quantidade);
}
