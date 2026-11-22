package school.sptech.projeto_amar.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Senha atual e nova senha do usuário autenticado")
public record AlteracaoSenhaRequestDTO(
    @NotBlank String senhaAtual,
    @NotBlank @Size(min = 8, max = 25)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
    String novaSenha) {
}
