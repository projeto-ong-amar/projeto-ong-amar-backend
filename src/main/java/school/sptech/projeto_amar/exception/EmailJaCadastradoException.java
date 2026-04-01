package school.sptech.projeto_amar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Este email já foi cadastrado")
public class EmailJaCadastradoException extends RuntimeException{

}
