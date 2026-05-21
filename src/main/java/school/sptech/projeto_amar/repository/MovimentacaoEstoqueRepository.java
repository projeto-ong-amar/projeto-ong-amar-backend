package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.sptech.projeto_amar.domain.MovimentacaoEstoque;

import java.util.List;
import java.util.Optional;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Integer> {
	Optional<MovimentacaoEstoque> findFirstByMovimentacaoId(Integer movimentacaoId);
	List<MovimentacaoEstoque> findByMovimentacaoId(Integer movimentacaoId);
}
