package school.sptech.projeto_amar.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioLoginDTO(
        @NotBlank(message = "O email não pode estar vazio")
        @Email(message = "O email deve ser válido")
        String email,

        @NotBlank(message = "A senha não pode estar vazia")
        String senha
) {
}
