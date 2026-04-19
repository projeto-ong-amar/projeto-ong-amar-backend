package school.sptech.projeto_amar.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;

public record UsuarioResponseDTO(
        @Schema(example = "João Silva")
        String nome,

        @Schema(example = "joao@email.com")
        String email,
        TipoUsuario tipoUsuario,
        String status
) {

}
/// //////////