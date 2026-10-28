package school.sptech.projeto_amar.dto.configuracao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Preferências de notificação do usuário autenticado")
public record PreferenciaNotificacaoDTO(
    @Schema(description = "Receber notificações de estoque baixo", example = "true")
    @NotNull Boolean estoqueBaixo,
    @Schema(description = "Receber notificações de produtos em risco de vencimento", example = "true")
    @NotNull Boolean riscoVencimento,
    @Schema(description = "Receber notificações sobre novas entradas", example = "true")
    @NotNull Boolean novasEntradas,
    @Schema(description = "Receber notificações sobre novas saídas", example = "true")
    @NotNull Boolean novasSaidas,
    @Schema(description = "E-mail para recebimento das notificações; pode ser vazio", example = "contato@ong.org.br", maxLength = 255)
    @Email @Size(max = 255) String emailNotificacao) {
}
