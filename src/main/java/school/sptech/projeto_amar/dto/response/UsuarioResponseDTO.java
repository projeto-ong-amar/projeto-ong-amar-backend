package school.sptech.projeto_amar.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UsuarioResponseDTO(
        @Schema(example = "João Silva")
        String nome,

        @Schema(example = "joao@email.com")
        String email
) {

}
