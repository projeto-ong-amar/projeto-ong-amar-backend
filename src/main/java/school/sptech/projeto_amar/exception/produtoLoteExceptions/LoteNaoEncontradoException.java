package school.sptech.projeto_amar.exception.produtoLoteExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Lote do produto não encontrado")
public class LoteNaoEncontradoException extends RuntimeException {

}
