package school.sptech.projeto_amar.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;

@Schema(description = "Dados para cadastro ou atualização de usuário")
public record UsuarioRequestDTO(

        @Schema(description = "Nome completo do usuário", example = "João Silva",
                minLength = 3, maxLength = 255)
        @NotBlank @Size(min = 3, max = 255)
        String nome,

        @Schema(description = "Email do usuário", example = "joao@email.com")
        @NotBlank @Email
        String email,

        @Schema(description = "Senha (mín. 8 chars, maiúscula, minúscula, número e caractere especial)",
                example = "Senha@123", minLength = 5, maxLength = 25)
        @NotBlank @Size(min = 5, max = 25) @Pattern(regexp = "...")
        String senha,

        @Schema(description = "Tipo/perfil do usuário",
                example = "VOLUNTARIO", allowableValues = {"ADMINISTRADOR", "VOLUNTARIO"})
        @NotNull
        TipoUsuario tipoUsuario,

        @Schema(description = "Status do usuário", example = "Ativo",
                minLength = 3, maxLength = 255)
        @NotBlank @Size(min = 3, max = 255)
        String status

) {
}