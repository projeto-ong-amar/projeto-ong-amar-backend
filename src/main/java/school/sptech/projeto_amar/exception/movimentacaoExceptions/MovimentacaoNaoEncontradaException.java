package school.sptech.projeto_amar.exception.movimentacaoExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Movimentação não encontrada")
public class MovimentacaoNaoEncontradaException extends RuntimeException {
    public MovimentacaoNaoEncontradaException(String message) {
        super(message);
    }
}
