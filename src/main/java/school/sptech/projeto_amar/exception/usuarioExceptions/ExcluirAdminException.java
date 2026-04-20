package school.sptech.projeto_amar.exception.usuarioExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Deve haver pelo menos 1 administrador no sistema")
public class ExcluirAdminException extends RuntimeException {
}
