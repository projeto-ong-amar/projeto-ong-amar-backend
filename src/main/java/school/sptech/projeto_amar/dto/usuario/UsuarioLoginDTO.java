package school.sptech.projeto_amar.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para autenticação")
public record UsuarioLoginDTO(

        @Schema(description = "Email do usuário", example = "joao@email.com")
        @NotBlank @Email
        String email,

        @Schema(description = "Senha do usuário", example = "Senha@123")
        @NotBlank
        String senha

) {
}
