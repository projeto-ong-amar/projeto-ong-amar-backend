package school.sptech.projeto_amar.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RedefinicaoSenhaRequestDTO(
    @NotBlank String token,

    @NotBlank @Size(min = 8, max = 25) @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$") String novaSenha) {
}