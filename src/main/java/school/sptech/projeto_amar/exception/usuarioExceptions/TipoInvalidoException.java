package school.sptech.projeto_amar.exception.usuarioExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Tipo do usuário inválido")
public class TipoInvalidoException extends RuntimeException {

}
