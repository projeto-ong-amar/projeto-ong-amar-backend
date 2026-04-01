package school.sptech.projeto_amar.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
  @NotBlank
  @Size(min=3, max=255)
  String nome,

  @NotBlank
  @Email
  String email,

  @NotBlank
  @Size(min=5, max=25)
  @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
  String senha
) {

}
