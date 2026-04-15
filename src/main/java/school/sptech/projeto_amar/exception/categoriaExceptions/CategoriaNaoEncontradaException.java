package school.sptech.projeto_amar.exception.categoriaExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Categoria não encontrada")
public class CategoriaNaoEncontradaException extends RuntimeException {
    public CategoriaNaoEncontradaException(Integer id) {
        super("Categoria com o id %d não encontrada".formatted(id));
    }

    public CategoriaNaoEncontradaException() {
    }
}
