package school.sptech.projeto_amar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Essa categoria já existe")
public class CategoriaJaExisteException extends RuntimeException {
}
