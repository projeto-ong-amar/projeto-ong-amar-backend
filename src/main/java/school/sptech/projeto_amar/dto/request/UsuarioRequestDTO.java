package school.sptech.projeto_amar.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
  @NotBlank(message = "O nome não pode estar vazio.")
  @Size(min=3, max=255, message = "O nome deve ter no mínimo 3 e no máximo 255 caracteres.")
  String nome,

  @NotBlank(message = "O email não pode estar vazio.")
  @Email(message = "O email deve ser válido.")
  String email,

  @NotBlank(message = "A senha não pode estar vazia.")
  @Size(min=5, max=25, message = "A senha deve ter no mínimo 5 e no máximo 25 caracteres.")
  @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "A senha deve conter pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial.")
  String senha
) {

}
