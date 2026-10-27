package school.sptech.projeto_amar.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "RecuperacaoSenhaRequest",
        description = "Dados para solicitar o envio do e-mail de recuperação de senha"
)
public record RecuperacaoSenhaRequestDTO(

    @Schema(
            description = "E-mail cadastrado do usuário que deseja redefinir a senha",
            example = "joao@email.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank @Email String email) {
}
