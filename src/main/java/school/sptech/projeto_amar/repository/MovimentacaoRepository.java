package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.sptech.projeto_amar.domain.Movimentacao;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Integer> {
}
