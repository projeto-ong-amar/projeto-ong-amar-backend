package school.sptech.projeto_amar.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public record ErroCampo(String campo, String mensagem) {}
    public record ErroResposta(int status, String message, List<ErroCampo> erros) {}

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        var erros = exception.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroCampo(erro.getField(), erro.getDefaultMessage()))
                .distinct()
                .sorted(java.util.Comparator.comparing(ErroCampo::campo).thenComparing(ErroCampo::mensagem))
                .toList();
        return new ResponseEntity<>(new ErroResposta(status.value(), "Confira os campos informados", erros),
                headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        return resposta(status, headers,
                "Corpo da requisição inválido. Confira os tipos e formatos dos campos");
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        // Não devolva ProblemDetail, mensagens do parser ou valores recebidos do cliente.
        return resposta(status, headers, mensagem(status));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> acessoNegado(AccessDeniedException exception) {
        return resposta(HttpStatus.FORBIDDEN, HttpHeaders.EMPTY, "Acesso negado");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> erroInesperado(Exception exception) {
        // As razões das exceções de negócio são textos fixos definidos pela aplicação.
        ResponseStatus anotacao = AnnotatedElementUtils.findMergedAnnotation(
                exception.getClass(), ResponseStatus.class);
        if (anotacao != null && anotacao.code().is4xxClientError()) {
            String mensagem = anotacao.reason().isBlank() ? mensagem(anotacao.code()) : anotacao.reason();
            return resposta(anotacao.code(), HttpHeaders.EMPTY, mensagem);
        }
        LOGGER.error("Falha inesperada ao processar solicitação", exception);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, HttpHeaders.EMPTY,
                mensagem(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private ResponseEntity<Object> resposta(HttpStatusCode status, HttpHeaders headers, String mensagem) {
        return new ResponseEntity<>(new ErroResposta(status.value(), mensagem, List.of()), headers, status);
    }

    private String mensagem(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Parâmetro inválido. Confira os valores informados";
            case 401 -> "Autenticação necessária";
            case 403 -> "Acesso negado";
            case 404 -> "Recurso não encontrado";
            case 405 -> "Método não permitido para este recurso";
            case 415 -> "Formato de conteúdo não suportado";
            default -> status.is5xxServerError()
                    ? "Não foi possível concluir a solicitação. Tente novamente mais tarde"
                    : "Não foi possível processar a solicitação";
        };
    }
}
