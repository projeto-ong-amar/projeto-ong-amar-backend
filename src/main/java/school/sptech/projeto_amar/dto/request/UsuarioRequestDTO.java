package school.sptech.projeto_amar.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
        @Schema(description = "Nome do usuário", example = "João Silva")
        @NotBlank(message = "O nome não pode estar vazio.")
        @Size(min = 3, max = 255, message = "O nome deve ter no mínimo 3 e no máximo 255 caracteres.")
        String nome,

        @Schema(description = "Email do usuário", example = "joao@email.com")
        @NotBlank(message = "O email não pode estar vazio.")
        @Email(message = "O email deve ser válido.")
        String email,

        @Schema(
                description = "Senha do usuário (mínimo 8 caracteres, com maiúscula, minúscula, número e caractere especial)",
                example = "Senha@123"
        )
        @NotBlank(message = "A senha não pode estar vazia.")
        @Size(min = 5, max = 25, message = "A senha deve ter no mínimo 5 e no máximo 25 caracteres.")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "A senha deve conter pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial.")
        String senha
) {

}
