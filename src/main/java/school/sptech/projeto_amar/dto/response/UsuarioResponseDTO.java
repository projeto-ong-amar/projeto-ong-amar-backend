package school.sptech.projeto_amar.dto.response;

import school.sptech.projeto_amar.domain.enums.TipoUsuario;

public record UsuarioResponseDTO(
  String nome, 
  String email,
  TipoUsuario tipoUsuario,
  String status
) {

}
