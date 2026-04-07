package school.sptech.projeto_amar.exception.usuarioExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Usuário não encontrado")
public class UsuarioNaoEncontradoException extends RuntimeException {
}
