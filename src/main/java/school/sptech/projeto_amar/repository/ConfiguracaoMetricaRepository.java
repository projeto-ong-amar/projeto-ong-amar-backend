package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.sptech.projeto_amar.domain.ConfiguracaoMetrica;

import java.util.Optional;

public interface ConfiguracaoMetricaRepository extends JpaRepository<ConfiguracaoMetrica, Integer> {
  Optional<ConfiguracaoMetrica> findFirstByOrderByIdAsc();
}
