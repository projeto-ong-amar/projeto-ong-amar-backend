package school.sptech.projeto_amar.exception.assistenteException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Essa categoria já existe")
public class ErroAoChamarIaException extends RuntimeException {
    public ErroAoChamarIaException(String message) {
        super(message);
    }
}
