package school.sptech.projeto_amar.mapper;

import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.dto.usuario.*;

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

  public static Usuario loginToEntity(UsuarioLoginDTO loginDTO) {
    Usuario usuario = new Usuario();
    usuario.setEmail(loginDTO.email());
    usuario.setSenha(loginDTO.senha());
    return usuario;
  }

    public static UsuarioTokenDTO entityAndTokenToDTO(Usuario usuario, String token) {
      return new UsuarioTokenDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), token);
    }

    public static UsuarioSessaoDTO toSessao(UsuarioTokenDTO usuarioTokenDTO) {
      return new UsuarioSessaoDTO(usuarioTokenDTO.userId(), usuarioTokenDTO.nome(), usuarioTokenDTO.email());
    }
}
