package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.sptech.projeto_amar.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
  Boolean existsByEmail(String email);
}
