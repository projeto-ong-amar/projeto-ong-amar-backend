package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.sptech.projeto_amar.domain.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    Boolean existsByNome(String nome);
}
