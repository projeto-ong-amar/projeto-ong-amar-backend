package school.sptech.projeto_amar.mapper;

import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.dto.request.UsuarioRequestDTO;
import school.sptech.projeto_amar.dto.response.UsuarioResponseDTO;

public class UsuarioMapping {
  public static UsuarioResponseDTO toResponse(Usuario u) {
    return new UsuarioResponseDTO(u.getNome(), u.getEmail(), u.getTipoUsuario(), u.getStatus());
  }

  public static Usuario toDomain(UsuarioRequestDTO u) {
    Usuario user = new Usuario();
    user.setNome(u.nome());
    user.setEmail(u.email());
    user.setSenha(u.senha());
    user.setTipoUsuario(u.tipoUsuario());
    user.setStatus(u.status());
    return user;
  }
  
}
