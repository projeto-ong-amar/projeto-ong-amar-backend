package school.sptech.projeto_amar.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperacaoSenhaRequestDTO(
    @NotBlank @Email String email) {
}