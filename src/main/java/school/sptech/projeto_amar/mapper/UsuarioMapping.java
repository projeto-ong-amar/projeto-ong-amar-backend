package school.sptech.projeto_amar.mapper;

import school.sptech.projeto_amar.domain.Usuario;
import school.sptech.projeto_amar.dto.response.UsuarioResponseDTO;

public class UsuarioMapping {
  public static UsuarioResponseDTO toResponse(Usuario u) {
    return new UsuarioResponseDTO(u.getNome(), u.getEmail());
  }
  
}
