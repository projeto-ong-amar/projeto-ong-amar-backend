package school.sptech.projeto_amar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Produto não encontrado")
public class ProdutoNaoEncontradoException extends RuntimeException {

}
