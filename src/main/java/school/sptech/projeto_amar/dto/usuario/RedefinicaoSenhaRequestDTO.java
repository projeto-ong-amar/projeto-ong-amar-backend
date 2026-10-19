package school.sptech.projeto_amar.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Token recebido no e-mail e a nova senha da conta")
public record RedefinicaoSenhaRequestDTO(
    @Schema(description = "Token de recuperação enviado por e-mail, válido por 15 minutos e utilizável uma única vez", example = "eyJhbGciOiJIUzI1NiJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String token,

    @Schema(description = "Nova senha com 8 a 25 caracteres, contendo letras maiúsculas e minúsculas, número e caractere especial (@$!%*?&)", example = "NovaSenha1!", minLength = 8, maxLength = 25, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @Size(min = 8, max = 25) @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$") String novaSenha) {
}
