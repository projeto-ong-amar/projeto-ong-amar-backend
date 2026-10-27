package school.sptech.projeto_amar.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados necessários para solicitar a recuperação de senha")
public record RecuperacaoSenhaRequestDTO(
    @Schema(description = "E-mail da conta para a qual será enviado o link de redefinição", example = "voluntario@ong.org.br", format = "email")
    @NotBlank @Email String email) {
}
