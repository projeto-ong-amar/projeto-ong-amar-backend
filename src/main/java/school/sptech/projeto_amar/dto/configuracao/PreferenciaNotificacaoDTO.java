package school.sptech.projeto_amar.dto.configuracao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PreferenciaNotificacaoDTO(
    @NotNull Boolean estoqueBaixo,
    @NotNull Boolean riscoVencimento,
    @NotNull Boolean novasEntradas,
    @NotNull Boolean novasSaidas,
    @Email @Size(max = 255) String emailNotificacao) {
}
