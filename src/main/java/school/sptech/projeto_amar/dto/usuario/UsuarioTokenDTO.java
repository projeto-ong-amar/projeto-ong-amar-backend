package school.sptech.projeto_amar.dto.usuario;

public record UsuarioTokenDTO(
        Integer userId,
        String nome,
        String email,
        String token
) {
}
