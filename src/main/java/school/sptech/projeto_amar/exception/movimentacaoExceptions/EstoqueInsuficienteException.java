package school.sptech.projeto_amar.exception.movimentacaoExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Estoque insuficiente")
public class EstoqueInsuficienteException extends RuntimeException {
    public EstoqueInsuficienteException(Integer estoqueTotal) {
        super("Estoque insuficiente. Disponível: " + estoqueTotal);
    }
}
