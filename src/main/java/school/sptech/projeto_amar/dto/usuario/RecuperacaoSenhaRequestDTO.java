package school.sptech.projeto_amar.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

<<<<<<< HEAD
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
=======
@Schema(description = "Dados necessários para solicitar a recuperação de senha")
public record RecuperacaoSenhaRequestDTO(
    @Schema(description = "E-mail da conta para a qual será enviado o link de redefinição", example = "voluntario@ong.org.br", format = "email")
>>>>>>> d0e85d4 (docs(swagger): documenta recuperação de senha)
    @NotBlank @Email String email) {
}
