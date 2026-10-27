package school.sptech.projeto_amar.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(
        name = "RedefinicaoSenhaRequest",
        description = "Dados para redefinir a senha usando o token enviado por e-mail"
)
public record RedefinicaoSenhaRequestDTO(

    @Schema(
            description = "Token de uso único recebido no link do e-mail de recuperação. Expira em 15 minutos",
            example = "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank String token,

    @Schema(
            description = "Nova senha. Requer de 8 a 25 caracteres, com pelo menos uma letra maiúscula, "
                    + "uma letra minúscula, um número e um caractere especial (@ $ ! % * ? &)",
            example = "Amar@2026",
            minLength = 8,
            maxLength = 25,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank @Size(min = 8, max = 25) @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$") String novaSenha) {
}
