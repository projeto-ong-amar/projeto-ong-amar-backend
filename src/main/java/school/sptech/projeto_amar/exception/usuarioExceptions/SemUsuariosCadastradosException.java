package school.sptech.projeto_amar.exception.usuarioExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Não existem usuarios cadastrados")
public class SemUsuariosCadastradosException extends RuntimeException {

}
