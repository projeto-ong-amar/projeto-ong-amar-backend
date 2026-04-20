package school.sptech.projeto_amar.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de sessão retornados após o login")
public record UsuarioSessaoDTO(

        @Schema(description = "ID do usuário autenticado", example = "1")
        Integer userId,

        @Schema(description = "Nome do usuário autenticado", example = "João Silva")
        String nome,

        @Schema(description = "Email do usuário autenticado", example = "joao@email.com")
        String email

) {
}
