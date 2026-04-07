package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.sptech.projeto_amar.domain.ProdutoLote;

public interface ProdutoLoteRepository extends JpaRepository<ProdutoLote, Integer> {
}
