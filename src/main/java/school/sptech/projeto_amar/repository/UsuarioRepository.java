package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
  Boolean existsByEmail(String email);
  Optional<Usuario> findByEmail(String email);

    List<Usuario> findAllByTipoUsuario(TipoUsuario tipoUsuario);
}
