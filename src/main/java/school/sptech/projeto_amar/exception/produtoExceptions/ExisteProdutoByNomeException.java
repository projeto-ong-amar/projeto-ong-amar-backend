package school.sptech.projeto_amar.exception.produtoExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Já existe um produto com esse nome!")
public class ExisteProdutoByNomeException extends RuntimeException {

}
