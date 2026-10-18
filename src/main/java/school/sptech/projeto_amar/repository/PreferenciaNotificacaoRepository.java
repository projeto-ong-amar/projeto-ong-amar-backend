package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import school.sptech.projeto_amar.domain.PreferenciaNotificacao;

import java.util.List;
import java.util.Optional;

public interface PreferenciaNotificacaoRepository extends JpaRepository<PreferenciaNotificacao, Integer> {
  Optional<PreferenciaNotificacao> findByUsuarioId(Integer usuarioId);

  @Query("SELECT p FROM PreferenciaNotificacao p JOIN FETCH p.usuario u WHERE u.status = 'ATIVO'")
  List<PreferenciaNotificacao> findAllDeUsuariosAtivos();
}
