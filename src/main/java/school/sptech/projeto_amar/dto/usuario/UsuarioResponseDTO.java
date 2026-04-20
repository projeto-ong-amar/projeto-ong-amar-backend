package school.sptech.projeto_amar.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;

@Schema(description = "Dados de retorno de um usuário")
public record UsuarioResponseDTO(

        @Schema(description = "Nome completo do usuário", example = "João Silva")
        String nome,

        @Schema(description = "Email do usuário", example = "joao@email.com")
        String email,

        @Schema(description = "Tipo/perfil do usuário", example = "VOLUNTARIO")
        TipoUsuario tipoUsuario,

        @Schema(description = "Status do usuário", example = "Ativo")
        String status

) {
}
