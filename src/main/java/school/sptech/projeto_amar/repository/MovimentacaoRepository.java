package school.sptech.projeto_amar.repository;

import java.awt.print.Pageable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Integer> {
  List<Movimentacao> findByTipoAndProdutoId(TipoMovimentacao tipo, Integer produtoId);
  List<Movimentacao> findByProdutoId(Integer produtoId);
  List<Movimentacao> findByTipo(TipoMovimentacao tipo);
  Page<Movimentacao> findByTipoOrderByDataHoraDesc(TipoMovimentacao tipo, Pageable pageable);
}
