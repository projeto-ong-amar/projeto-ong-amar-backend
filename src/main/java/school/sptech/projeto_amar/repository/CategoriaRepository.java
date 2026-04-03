package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.sptech.projeto_amar.domain.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    Boolean existsByNome(String nome);
}
