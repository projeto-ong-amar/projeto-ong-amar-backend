package school.sptech.projeto_amar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.sptech.projeto_amar.domain.TokenRecuperacaoSenha;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRecuperacaoSenhaRepository extends JpaRepository<TokenRecuperacaoSenha, Long> {
  void deleteByUsuarioId(Integer usuarioId);

  Optional<TokenRecuperacaoSenha> findByTokenHashAndExpiraEmAfterAndUtilizadoFalse(
      String tokenHash, LocalDateTime agora);
}