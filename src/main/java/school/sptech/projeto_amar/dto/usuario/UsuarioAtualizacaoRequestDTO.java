package school.sptech.projeto_amar.dto.usuario;

import jakarta.validation.constraints.*;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;

public record UsuarioAtualizacaoRequestDTO(
    @NotBlank @Size(min = 3, max = 255) String nome,
    @NotBlank @Email String email,
    @Null(message = "Use /usuarios/me/senha para alterar a senha") String senha,
    @NotNull TipoUsuario tipoUsuario,
    @NotBlank @Size(min = 3, max = 255) String status) {
}
