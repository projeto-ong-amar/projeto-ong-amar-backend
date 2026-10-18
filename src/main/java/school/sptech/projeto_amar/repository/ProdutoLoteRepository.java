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
      SELECT COALESCE(SUM(l.quantidade), 0)
      FROM ProdutoLote l
      WHERE l.produto.status = 'ATIVO'
        AND l.quantidade > 0
        AND l.validade BETWEEN :inicio AND :fim
      """)
  Long sumUnidadesComVencimentoNoPeriodo(@Param("inicio") LocalDate inicio,
      @Param("fim") LocalDate fim);

  @Query("""
      SELECT COALESCE(SUM(l.quantidade), 0)
      FROM ProdutoLote l
      WHERE l.produto.status = 'ATIVO'
        AND l.quantidade > 0
        AND (SELECT COALESCE(SUM(s.quantidade), 0)
             FROM ProdutoLote s WHERE s.produto = l.produto) < :limite
      """)
  Long sumUnidadesComEstoqueBaixo(@Param("limite") Integer limite);

  @Query("""
      SELECT COALESCE(SUM(l.quantidade), 0)
      FROM ProdutoLote l
      WHERE l.produto.status = 'ATIVO'
        AND l.quantidade > 0
        AND NOT EXISTS (
          SELECT m.id FROM Movimentacao m
          WHERE m.produto = l.produto AND m.data BETWEEN :inicio AND :fim
        )
      """)
  Long sumUnidadesSemMovimentacaoNoPeriodo(@Param("inicio") LocalDate inicio,
      @Param("fim") LocalDate fim);

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
      SELECT p.produto.categoria, COALESCE(SUM(p.quantidade), 0)
      FROM ProdutoLote p
      WHERE p.quantidade > 0
      GROUP BY p.produto.categoria
      ORDER BY p.produto.categoria
      """)
  List<Object[]> sumEstoquePorTipo();

  @Query("""
      SELECT p.produto.id, COALESCE(SUM(p.quantidade), 0)
      FROM ProdutoLote p
      GROUP BY p.produto.id
      """)
  List<Object[]> sumQuantidadePorProduto();

  @Query("""
      SELECT p
      FROM ProdutoLote p
      WHERE p.quantidade > 0
        AND p.produto.status = 'ATIVO'
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
