package school.sptech.projeto_amar.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token JWT gerado após autenticação (uso interno)")
public record UsuarioTokenDTO(

        @Schema(description = "ID do usuário", example = "1")
        Integer userId,

        @Schema(description = "Nome do usuário", example = "João Silva")
        String nome,

        @Schema(description = "Email do usuário", example = "joao@email.com")
        String email,

        @Schema(description = "Perfil de acesso do usuário", example = "ADMINISTRADOR")
        String tipoUsuario,

        @Schema(description = "Token JWT gerado",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token

) {
}
