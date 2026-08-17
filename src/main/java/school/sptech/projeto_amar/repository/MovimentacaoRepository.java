package school.sptech.projeto_amar.repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;

import java.time.LocalDate;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Integer> {
  List<Movimentacao> findByTipoAndProdutoId(TipoMovimentacao tipo, Integer produtoId);

  List<Movimentacao> findByProdutoId(Integer produtoId);

  List<Movimentacao> findByTipo(TipoMovimentacao tipo);

  List<Movimentacao> findTop20ByTipoOrderByDataDescIdDesc(TipoMovimentacao tipo);

  Long countByTipo(TipoMovimentacao tipo);

  Page<Movimentacao> findByTipoOrderByDataDesc(TipoMovimentacao tipo, Pageable pageable);

  Page<Movimentacao> findByTipoInOrderByDataDesc(List<TipoMovimentacao> tipos, Pageable pageable);

  @Query("SELECT COALESCE(SUM(m.quantidade), 0) FROM Movimentacao m WHERE m.tipo = :tipo")
  Integer sumQuantidadeByTipo(@Param("tipo") TipoMovimentacao tipo);

  @Query("""
      SELECT m.produto.nome, COALESCE(SUM(m.quantidade), 0)
      FROM Movimentacao m
      WHERE m.tipo = :tipo
      GROUP BY m.produto.nome
      ORDER BY SUM(m.quantidade) DESC
      """)
  List<Object[]> findTopProdutosPorTipo(@Param("tipo") TipoMovimentacao tipo, Pageable pageable);

  @Query("""
      SELECT COALESCE(SUM(CASE WHEN m.tipo = school.sptech.projeto_amar.domain.enums.TipoMovimentacao.ENTRADA
                              THEN m.quantidade ELSE -m.quantidade END), 0)
      FROM Movimentacao m
      WHERE m.data BETWEEN :dataInicio AND :dataFim
      """)
  Integer sumSaldoByPeriodo(@Param("dataInicio") LocalDate dataInicio,
      @Param("dataFim") LocalDate dataFim);

  
}
